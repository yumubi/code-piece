package io.goji.jav.ipc;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

import static java.lang.StringTemplate.STR;

public class SharedDir {


    private static final String sharedResource = "D:\\SharedDir";


    public static void main(String[] args) throws IOException, InterruptedException {

        NamedPipe();
    }


    public static void WatchService() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault()
                .newWatchService();

        Path path = Path.of(sharedResource);
        path.register(watchService, StandardWatchEventKinds.OVERFLOW,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;

        while ((key = watchService.take()) != null) {
            for(WatchEvent<?> event : key.pollEvents()) {
                System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
            }
            key.reset();
        }
    }


    public static void NamedPipe() throws IOException {
        

        BufferedReader reader = new BufferedReader(new FileReader(sharedResource + File.separator + "named-pipe.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(STR."Read line: \{line}");
        }
    }

}
