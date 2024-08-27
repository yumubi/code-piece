package io.goji.exp.httpdownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpDownloader {
	private boolean resumable;
	private URL url;
	//文件保存的本地临时地址
	private File localFile;
	//存放每一段的起止位置
	private int[] endPoint;
	private Object waiting = new Object();
	private AtomicInteger downloadedBytes = new AtomicInteger(0);
	private AtomicInteger aliveThreads = new AtomicInteger(0);
	private boolean multithreaded = true;
	private int fileSize = 0;
	private int THREAD_NUM = 5;
	private int TIME_OUT = 5000;
	private final int MIN_SIZE = 2 << 20;

	public static void main(String[] args) throws IOException {
		String url = "https://mirrors.cloud.tencent.com/gradle/gradle-8.6-all.zip";
		String localPath = STR."D:\{File.separator}\{url.substring(url.lastIndexOf('/') + 1)}";
		new HttpDownloader(url, localPath, 10, 5000).get();
	}

	public HttpDownloader(String Url, String localPath) throws MalformedURLException {
		this.url = new URL(Url);
		this.localFile = new File(localPath);
	}

	public HttpDownloader(String Url, String localPath,
			int threadNum, int timeout) throws MalformedURLException {
		this(Url, localPath);
		this.THREAD_NUM = threadNum;
		this.TIME_OUT = timeout;
	}

	//开始下载文件
	public void get() throws IOException {
		long startTime = System.currentTimeMillis();

		resumable = supportResumeDownload();
		if (!resumable || THREAD_NUM == 1|| fileSize < MIN_SIZE){
			multithreaded = false;
		}
		//单线程下载
		if (!multithreaded) {
			new DownloadThread(0, 0, fileSize - 1).start();;
		}
		else {
			endPoint = new int[THREAD_NUM + 1];
			int block = fileSize / THREAD_NUM;
			for (int i = 0; i < THREAD_NUM; i++) {
				//记录每一段的起始位置
				//则结束位置为后一个起始位置-1
				endPoint[i] = block * i;
			}
			//最后一个，存放文件尾位置
			endPoint[THREAD_NUM] = fileSize;
			for (int i = 0; i < THREAD_NUM; i++) {
				new DownloadThread(i, endPoint[i], endPoint[i + 1] - 1).start();
			}
		}

		//监测下载速度及下载状态，下载完成时通知主线程
		startDownloadMonitor();

		//等待 downloadMonitor 通知下载完成
		try {
			synchronized(waiting) {
				waiting.wait();
			}
		} catch (InterruptedException e) {
			System.err.println("Download interrupted.");
		}

		//所有线程都下载完成，进行文件合并
		cleanTempFile();

		long timeElapsed = System.currentTimeMillis() - startTime;
		System.out.println("* File successfully downloaded.");
		//计算下载时间，和下载速度
		System.out.printf("* Time used: %.3f s, Average speed: %d KB/s%n",
				timeElapsed / 1000.0, downloadedBytes.get() / timeElapsed);
	}

	//检测目标文件是否支持断点续传，以决定是否开启多线程下载文件的不同部分
	public boolean supportResumeDownload() throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestProperty("Range", "bytes=0-");
		int resCode;
		while (true) {
			try {
				con.connect();
				//获取要下载文件的大小，为之后分段做准备
				fileSize = con.getContentLength();
				resCode = con.getResponseCode();
				con.disconnect();
				break;
			} catch (ConnectException e) {
				System.out.println("Retry to connect due to connection problem.");
			}
		}
		if (resCode == 206) {
			//状态码为206，表明支持断点续传
			System.out.println("* Support resume download");
			return true;
		} else {
			System.out.println("* Doesn't support resume download");
			return false;
		}
	}

	//监测下载速度及下载状态，下载完成时通知主线程
	public void startDownloadMonitor() {
		Thread downloadMonitor = new Thread(() -> {
			int prev = 0;
			//现在下载到的字节数
			int curr = 0;
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}

				curr = downloadedBytes.get();
				System.out.printf("Speed: %d KB/s, Downloaded: %d KB (%.2f%%), Threads: %d%n",
						(curr - prev) >> 10, curr >> 10, curr / (float) fileSize * 100, aliveThreads.get());
				prev = curr;

				if (aliveThreads.get() == 0) {
					synchronized (waiting) {
						waiting.notifyAll();
					}
				}
			}
		});

		//设置为守护线程
		downloadMonitor.setDaemon(true);
		downloadMonitor.start();
	}

	//对临时文件进行合并或重命名
	public void cleanTempFile() throws IOException {
		if (multithreaded) {
			merge();
			System.out.println("* Temp file merged.");
		} else {
			Files.move(Paths.get(STR."\{localFile.getAbsolutePath()}.0.tmp"),
					Paths.get(localFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	//合并多线程下载产生的多个临时文件
	public void merge() {
		try (OutputStream out = new FileOutputStream(localFile)) {
			byte[] buffer = new byte[1024];
			int size;
			for (int i = 0; i < THREAD_NUM; i++) {
				String tmpFile = STR."\{localFile.getAbsolutePath()}.\{i}.tmp";
				InputStream in = new FileInputStream(tmpFile);
				while ((size = in.read(buffer)) != -1) {
					out.write(buffer, 0, size);
				}
				in.close();
				Files.delete(Paths.get(tmpFile));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//一个下载线程负责下载文件的某一部分，如果失败则自动重试，直到下载完成
	class DownloadThread extends Thread {
		private int id;
		private int start;
		private int end;
		private OutputStream out;

		public DownloadThread(int id, int start, int end) {
			this.id = id;
			this.start = start;
			this.end = end;
			aliveThreads.incrementAndGet();
		}

		//保证文件的该部分数据下载完成
		@Override
		public void run() {
			boolean success = false;
			while (true) {
                try {
                    success = download();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                if (success) {
					System.out.println(STR."* Downloaded part \{id + 1}");
					break;
				} else {

					System.out.println(STR."Retry to download part \{id + 1}");
				}
			}
			aliveThreads.decrementAndGet();
		}

		//下载文件指定范围的部分
//		public boolean download() {
//			try {
//
//				HttpURLConnection con = (HttpURLConnection) url.openConnection();
//				con.setRequestProperty("Range", String.format("bytes=%d-%d", start, end));
//				con.setConnectTimeout(TIME_OUT);
//				con.setReadTimeout(TIME_OUT);
//				con.connect();
//				int partSize = con.getHeaderFieldInt("Content-Length", -1);
//				if (partSize != end - start + 1) {
//					return false;
//				}
//				if (out == null) {
//					//设置文件片段的保存路径
//					out = new FileOutputStream(STR."\{localFile.getAbsolutePath()}.\{id}.tmp");
//				}
//				try (InputStream in = con.getInputStream()) {
//					byte[] buffer = new byte[1024];
//					int size;
//					while (start <= end && (size = in.read(buffer)) > 0) {
//						start += size;
//						downloadedBytes.addAndGet(size);
//						out.write(buffer, 0, size);
//						out.flush();
//					}
//					con.disconnect();
//					//没有下载完，即存在部分文件数据缺失
//					if (start <= end) {
//						return false;
//					} else {
//						out.close();
//					}
//				}
//			} catch(SocketTimeoutException e) {
//				System.out.println(STR."Part \{id + 1} Reading timeout.");
//				return false;
//			} catch (IOException e) {
//				System.out.println(STR."Part \{id + 1} encountered error.");
//				return false;
//			}
//			return true;
//		}
//	}

		public boolean download() throws URISyntaxException {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(url.toURI())
					.header("Range", String.format("bytes=%d-%d", start, end))
					.timeout(java.time.Duration.ofMillis(TIME_OUT))
					.build();

			try {
				HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

				int partSize =
						Math.toIntExact(response.headers().firstValueAsLong("Content-Length").orElse(-1L));
				if (partSize != end - start + 1) {
					return false;
				}

				if (out == null) {
					// Set the path for saving the file fragment
					out = new FileOutputStream(STR."\{localFile.getAbsolutePath()}.\{id}.tmp");
				}

				try (InputStream in = response.body()) {
					byte[] buffer = new byte[1024];
					int size;
					while (start <= end && (size = in.read(buffer)) > 0) {
						start += size;
						downloadedBytes.addAndGet(size);
						out.write(buffer, 0, size);
						out.flush();
					}
				} finally {
					// If the download is incomplete, i.e., some file data is missing
					if (start <= end) {
						return false;
					} else {
						out.close();
					}
				}
			} catch (IOException | InterruptedException e) {
				System.out.println(STR."\{STR}Part {id + 1} encountered error: \{e.getMessage()}");
				return false;
			}
			return true;
		}
	}



}
