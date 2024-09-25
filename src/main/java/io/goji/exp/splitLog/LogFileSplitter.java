package io.goji.exp.splitLog;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LogFileSplitter {

    private static final int LINES_PER_FILE = 10000; // 每个小文件包含的行数
    private static final String OUTPUT_DIR = "split_logs/"; // 输出目录

    public static void main(String[] args) {
        String logFilePath = "path/to/your/large_log_file.log"; // 大日志文件路径

        try {
            splitLogFile(logFilePath);
            System.out.println("Log file split successfully!");
        } catch (IOException e) {
            System.err.println("Error while splitting log file: " + e.getMessage());
        }
    }

    /**
     * 将大日志文件按指定行数切割成多个小文件
     *
     * @param logFilePath 日志文件的路径
     * @throws IOException
     */
    public static void splitLogFile(String logFilePath) throws IOException {
        Path filePath = Paths.get(logFilePath);
        AtomicInteger fileCounter = new AtomicInteger(1);

        // 创建输出目录
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        // 使用 BufferedReader 读取大文件
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            int lineCounter = 0;

            // 初始化第一个小文件的输出流
            BufferedWriter writer = createNewFileWriter(fileCounter.getAndIncrement());

            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
                lineCounter++;

                // 如果行数达到限制，切换到下一个小文件
                if (lineCounter >= LINES_PER_FILE) {
                    writer.close();
                    writer = createNewFileWriter(fileCounter.getAndIncrement());
                    lineCounter = 0;
                }
            }

            // 关闭最后一个小文件的写入流
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * 创建新的小文件的 BufferedWriter
     *
     * @param fileNumber 当前文件编号
     * @return BufferedWriter 新文件的写入流
     * @throws IOException
     */
    private static BufferedWriter createNewFileWriter(int fileNumber) throws IOException {
        String fileName = OUTPUT_DIR + "log_part_" + fileNumber + ".log";
        return Files.newBufferedWriter(Paths.get(fileName));
    }
}
