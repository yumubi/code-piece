package io.goji.jav.fileIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class Main {
    public static void main(String[] args) throws URISyntaxException {

//        var p = Path.of("/Users/venkat/Downloads/records.ser");
//        var p2 = Path.of(new URI("file:///Users/venkat/Downloads/records.ser"));
//        System.out.println(p2.equals(p));
//
//        File f = p.toFile();
//        System.out.println(f.isDirectory());
//
//
//        Path p3 = f.toPath();
//        System.out.println(p3.equals(p));
//
//
//        var inputFile = new File("input");
//        try (var in = new FileInputStream(inputFile)) {
//            Files.copy(in, Paths.get("output"));
//        } catch(IOException ex) {
//            ex.printStackTrace();
//        }


//
//
//
//        boolean shutdown = false;
//
//        try {
//            var watcher = FileSystems.getDefault().newWatchService();
//            var dir = Path.of("/Users/ben");
//
//            var registered = dir.register(watcher, ENTRY_MODIFY);
//
//            while (!shutdown) {
//                WatchKey key = null;
//                try {
//                    key = watcher.take();
//                    for (WatchEvent<?> event : key.pollEvents()) {
//                        if (event == null) {
//                            continue;
//                        }
//                        if (event.kind() == ENTRY_MODIFY) {
//                            System.out.println("Home dir changed!");
//                        }
//                    }
//                    key.reset();
//                } catch (InterruptedException e) {
//                    // Log interruption
//                    shutdown = true;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//



        FileInputStream fis = getSomeStream();
        boolean fileOK = true;

        try (FileChannel fchan = fis.getChannel()) {
            var buffy =
                    ByteBuffer.allocateDirect(16 * 1024 * 1024);

            while (fchan.read(buffy) !=
                    -1 || buffy.position() > 0 || fileOK) {
                fileOK = computeChecksum(buffy);
                buffy.compact();
            }
        } catch (IOException e) {
            System.out.println("Exception in I/O");



        }


        


    }

    private static boolean computeChecksum(ByteBuffer buffy) {
        return true;
    }

    private static FileInputStream getSomeStream() {
        return null;
    }


}
