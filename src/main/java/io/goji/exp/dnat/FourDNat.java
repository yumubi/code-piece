package io.goji.exp.dnat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class FourDNat {
    private static final int RETRY_INTERVAL = 5;
    private static final int TIMEOUT = 10;
    private static final String VERSION = "v0.0.5";
    private static final Logger LOGGER = Logger.getLogger(FourDNat.class.getName());

    public static void main(String[] args) {
        printBanner();
        System.out.println("args is " + Arrays.toString(args));

        if (args.length == 2) {
            switch (args[1]) {
                case "-version", "-v", "-V" -> {
                    System.out.println(VERSION);
                    System.exit(0);
                }
            }
        }

        if (args.length < 4) {
            printHelp();
            System.exit(0);
        }

        switch (args[1]) {
            case "-listen", "-l" -> listener(args[2], args[3]);
            case "-agent", "-a" -> agent(args[2], args[3]);
            case "-forward", "-f" -> forward(args[2], args[3]);
            case "-proxy", "-p" -> proxy(args[2], args[3], args);
            default -> printHelp();
        }
    }

    private static void printHelp() {
        System.out.println("""
            Usage:
                "-forward listenPort targetAddress" example: "-forward 10000 127.0.0.1:22"
                "-listen listenPort0 listenPort1" example: "-listen 10000 10001"
                "-agent targetAddress0 targetAddress1" example: "-agent 127.0.0.1:10000 127.0.0.1:22"
                "-proxy protocol listenAddress" example: "-proxy http 1080", "-proxy https 1080 server.pem server.key", "-proxy socks5 1080"
                "-version"
            """);
    }

    private static void printBanner() {
        System.out.println("""
               _____     .___             __   
              /  |  |  __| _/____ _____ _/  |_ 
             /   |  |_/ __ |/    \\__  \\   __\\
            /    ^   / /_/ |   |  \\/ __ \\|  |  
            \\____   |\\____ |___|  (____  /__|  
                 |__|     \\/    \\/     \\/
            """);
    }

    private static void listener(String listenPort0, String listenPort1) {
        try (ServerSocket serverSocket0 = new ServerSocket(Integer.parseInt(listenPort0));
             ServerSocket serverSocket1 = new ServerSocket(Integer.parseInt(listenPort1))) {

            LOGGER.info(String.format("[*] Listening on ports [%s, %s]", listenPort0, listenPort1));
            while (true) {
                ExecutorService executor = Executors.newCachedThreadPool();
                Socket conn0 = serverSocket0.accept();
                Socket conn1 = serverSocket1.accept();

                executor.execute(() -> mutualCopyIO(conn0, conn1));
                executor.shutdown();
            }
        } catch (IOException e) {
            LOGGER.severe("[x] Listening error: " + e.getMessage());
        }
    }

    private static void forward(String listenPort, String targetAddress) {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(listenPort))) {
            LOGGER.info(String.format("[*] Listening on port [%s], forwarding to [%s]", listenPort, targetAddress));

            while (true) {
                Socket conn0 = serverSocket.accept();
                LOGGER.info("[+] Client connected: " + conn0.getRemoteSocketAddress());

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> handleForward(targetAddress, conn0));
                executor.shutdown();
            }
        } catch (IOException e) {
            LOGGER.severe("[x] Listening error: " + e.getMessage());
        }
    }

    private static void handleForward(String targetAddress, Socket conn0) {
        try (Socket conn1 = new Socket()) {
            conn1.connect(new InetSocketAddress(targetAddress.split(":")[0], Integer.parseInt(targetAddress.split(":")[1])), TIMEOUT * 1000);

            LOGGER.info("[+] Connected to target: " + targetAddress);
            mutualCopyIO(conn0, conn1);
        } catch (IOException e) {
            LOGGER.severe("[x] Forwarding error: " + e.getMessage());
            sendError(conn0, e.getMessage());
        }
    }

    private static void agent(String targetAddress0, String targetAddress1) {
        while (true) {
            try (Socket conn0 = connectToTarget(targetAddress0)) {
                LOGGER.info("[+] Connected to target: " + targetAddress0);

                while (true) {
                    try (Socket conn1 = connectToTarget(targetAddress1)) {
                        LOGGER.info("[+] Connected to target: " + targetAddress1);
                        mutualCopyIO(conn0, conn1);
                        break;
                    } catch (IOException e) {
                        LOGGER.severe("[x] Connection to target1 failed: " + e.getMessage());
                        sleep(RETRY_INTERVAL);
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("[x] Connection to target0 failed: " + e.getMessage());
                sleep(RETRY_INTERVAL);
            }
        }
    }

    private static Socket connectToTarget(String targetAddress) throws IOException {
        String[] target = targetAddress.split(":");
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(target[0], Integer.parseInt(target[1])), TIMEOUT * 1000);
        return socket;
    }

    private static void proxy(String protocol, String listenAddress, String[] args) {
        // Implement HTTP, HTTPS, and SOCKS5 proxy logic here, similar to the Go implementation
    }

    private static void mutualCopyIO(Socket conn0, Socket conn1) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(() -> copyIO(conn0, conn1));
        executor.execute(() -> copyIO(conn1, conn0));
        executor.shutdown();
    }

    private static void copyIO(Socket src, Socket dest) {
        try (InputStream in = src.getInputStream(); OutputStream out = dest.getOutputStream()) {
            in.transferTo(out);
        } catch (IOException e) {
            LOGGER.severe("[x] IO Copy error: " + e.getMessage());
        }
    }

    private static void sendError(Socket socket, String message) {
        try (OutputStream out = socket.getOutputStream()) {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.severe("[x] Sending error to client failed: " + e.getMessage());
        }
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
