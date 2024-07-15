package io.goji.jav.fileIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class CommonIOTask {

    public CommonIOTask() throws MalformedURLException, URISyntaxException {
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
//        //Reading and writing text files
//        Path path = Path.of("src/main/resources/logback.xml");
//        String content = Files.readString(path);
//        System.out.println(content);
//        System.out.println("=====================================");
//        //Read the file as a sequence of lines
//        List<String> lines = Files.readAllLines(path);
//        lines.forEach(System.out::println);
//
//        System.out.println("=====================================");
//
//        //Read big files
////        Path bigFile = Paths.get("D:\\QQFiles\\3432771497\\nt_qq\\nt_data\\Pic", "1715525343473_deleteQQThumbRecord.txt");
////        try (Stream<String> bigFileLines = Files.lines(bigFile)) {
////            bigFileLines.forEach(System.out::println);
////        }
//
//        System.out.println("=====================================");
//
//        // read words, separated by non-letters with Scanner
//        Stream<String> tokens = new Scanner(path).useDelimiter("\\PL+").tokens();
//        tokens.forEach(System.out::println);


        //Write string to file
//        Path writeFiles = Paths.get("src/main/resources/writeSample.txt");
//        String writeStr = "I' m writing\npoetry is cool\n\ntest";
//        Files.writeString(writeFiles, writeStr);


        //Write a list of lines
//        Path writeLinesPath = Path.of("src/main/resources/lines.txt");
//        List<String> writeLines = new ArrayList<>(){{
//            add("a");
//            add("a");
//            add("a");
//            add("a");
//            add("a");
//            add("b");
//            add("\n");
//            add("c");
//            add("c");
//        }};
//
//        Files.write(writeLinesPath, writeLines);


        //PrintWriter
//        Path printWriter = Path.of("src/main/resources/printWriter.txt");
//        var writer = new PrintWriter(printWriter.toFile());
//        writer.printf(Locale.CHINESE, "Hello, %s, next year you'll be %d years old!%n", "萝莉", 13 + 1);
//        writer.close(); // Flushes the stream

//        Path bufferWriterPath = Path.of("src/main/resources/bufferWriter.txt");
//        var BufferWriter = Files.newBufferedWriter(bufferWriterPath);
//        BufferWriter.write("something..."); // Does not write a line separator
//        BufferWriter.newLine();
//        BufferWriter.close(); // Flushes the stream


//        try(HttpClient client = HttpClient.newBuilder().build()) {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://horstmann.com/index.html"))
//                    .GET()
//                    .build();
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            String result = response.body();
//            System.out.println(result);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }


//        Path netIOPath = Path.of("src/main/resources/netIO.html");
//        InputStream in = new URI("https://horstmann.com/index.html").toURL().openStream();
//        byte[] bytes = in.readAllBytes();
//        String result = new String(bytes);
//    //    System.out.println(result);
//        OutputStream out = java.nio.file.Files.newOutputStream(netIOPath);
//        in.transferTo(out);
//        while (in.read() != -1) {
//            System.out.println("reading...");
//            System.out.println(in.read());
//            out.write(in.read());
//        }
//        in.close();
//        out.close();
//        out.flush();


//    URL url = new URI("https://dog.ceo/api/breeds/image/random").toURL();
//    Map<String, Object> result = JSON.std.mapFrom(url);
//    URL url = new URI(result.get("message").toString()).toURL();
//    BufferedImage img = javax.imageio.ImageIO.read(url);


        //Two Files.newDirectoryStream(Path) methods yields DirectoryStream instances,
        // which can be used in enhanced for loops. There is no advantage over using Files.list.


//        try (Stream<Path> entries = Files.walk(Paths.get("src/main/resources"))) {
//            List<Path> htmlFiles = entries.filter(p -> p.toString().endsWith("html")).toList();
//            htmlFiles.forEach(System.out::println);
//        }


//        try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:file:/tmp/zipfs.zip"), Map.of("create", "true"))) {
//            Path zipPath = fs.getPath("/zipfs");
//            Files.createDirectory(zipPath);
//            Path file = zipPath.resolve("file.txt");
//            Files.writeString(file, "Hello, World!");


//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try (Stream<Path> entries = Files.walk(fs.getPath("/"))) {
//            List<Path> filesInZip = entries.filter(Files::isRegularFile).toList();
//        }
//        String contents = Files.readString(fs.getPath("/LICENSE"));


    }


}
