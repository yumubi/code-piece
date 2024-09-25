package io.goji.exp.splitLog;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

public class Chainsaw {
    // https://yufanonsoftware.me/posts/chainsaw-cut-large-log-file-into-small-pieces
    private static final String VERSION = "1.0.1";
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static String outputDir = "cut/";
    private static boolean dryRun = false;

    // todo 文件读取bug
    public static void main(String[] args) {
        // Parse command line arguments
        for(String arg : args) {
            System.out.println(arg);
        }
        Map<String, String> arguments = parseArguments(args);

        // Set up filters and options
        int notBeforeDate = Integer.parseInt(arguments.getOrDefault("not-before", "0"));
        int notAfterDate = Integer.parseInt(arguments.getOrDefault("not-after", "0"));
        int chunkSize = Integer.parseInt(arguments.getOrDefault("chunk-size", "50000"));
        String logfile = arguments.get("file");
        outputDir = arguments.getOrDefault("output", "cut/");
        dryRun = Boolean.parseBoolean(arguments.getOrDefault("dry-run", "false"));

        if (dryRun) {
            System.out.println("Running in dry-run mode, no file will be written");
        }

        // Ensure output directory exists
        new File(outputDir).mkdirs();

        try {
            processLogFile(logfile, notBeforeDate, notAfterDate, chunkSize);
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processLogFile(String logfile, int notBeforeDate, int notAfterDate, int chunkSize) throws IOException {
        Pattern datePattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}");
        List<String> logBuffer = new ArrayList<>(chunkSize);
        String cursorTimestamp = "";
        int chunkCounter = 0;
        int countTotal = 0, countDropped = 0, countPassed = 0, countProcessed = 0;
        boolean haveStarted = false, passLogMessageByDate = false, notAfterFilterSkipped = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(logfile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                countTotal++;
                Matcher matcher = datePattern.matcher(line);
                boolean isNewLogLine = matcher.find();

                if (!haveStarted) {
                    if (isNewLogLine) {
                        haveStarted = true;
                        cursorTimestamp = matcher.group();
                    } else {
                        countDropped++;
                        continue;
                    }
                }

                if (passLogMessageByDate) {
                    if (isNewLogLine) {
                        int logTimestampInt = Integer.parseInt(matcher.group().replace("-", ""));
                        if (isWithinDateRange(logTimestampInt, notBeforeDate, notAfterDate)) {
                            passLogMessageByDate = false;
                        } else {
                            countPassed++;
                            continue;
                        }
                    } else {
                        countPassed++;
                        continue;
                    }
                } else {
                    if (isNewLogLine) {
                        int logTimestampInt = Integer.parseInt(matcher.group().replace("-", ""));
                        if (logTimestampInt < notBeforeDate) {
                            passLogMessageByDate = true;
                            countPassed++;
                            continue;
                        } else if (logTimestampInt > notAfterDate && notAfterDate != 0) {
                            notAfterFilterSkipped = true;
                            break;
                        }
                    }
                }

                countProcessed++;
                if (!isNewLogLine) {
                    logBuffer.add(line);
                    continue;
                }

                String messageTimestamp = matcher.group();
                if (cursorTimestamp.equals(messageTimestamp)) {
                    if (logBuffer.size() > chunkSize) {
                        saveLog(logBuffer, cursorTimestamp, true, chunkCounter);
                        chunkCounter++;
                        logBuffer = new ArrayList<>(chunkSize);
                    }
                } else {
                    saveLog(logBuffer, cursorTimestamp, chunkCounter != 0, chunkCounter);
                    chunkCounter = 0;
                    logBuffer = new ArrayList<>(chunkSize);
                    cursorTimestamp = messageTimestamp;
                }
                logBuffer.add(line);
            }
        }

        if (haveStarted) {
            saveLog(logBuffer, cursorTimestamp, chunkCounter != 0, chunkCounter);
        }

        System.out.println();
        if (notAfterFilterSkipped) {
            System.out.println("Not after filter used, lines after specified date skipped");
            System.out.printf("%d lines dropped%n", countDropped);
            System.out.printf("%d lines saved%n", countProcessed);
        } else {
            System.out.printf("%d lines dropped%n", countDropped);
            System.out.printf("%d lines saved%n", countProcessed);
            System.out.printf("%d lines passed%n", countPassed);
            System.out.printf("%d lines in given file%n", countTotal);
        }
    }

    private static void saveLog(List<String> logBuffer, String timestamp, boolean chunked, int chunkNumber) {
        if (logBuffer.isEmpty()) {
            return;
        }

        String filename = chunked
                ? Paths.get(outputDir, String.format("%s.%d.log", timestamp, chunkNumber)).toString()
                : Paths.get(outputDir, String.format("%s.log", timestamp)).toString();

        executor.submit(() -> {
            if (!dryRun) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                    for (String line : logBuffer) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("%s saved (%d lines)%n", filename, logBuffer.size());
        });
    }

    private static boolean isWithinDateRange(int date, int notBeforeDate, int notAfterDate) {
        return (notBeforeDate == 0 || date >= notBeforeDate) && (notAfterDate == 0 || date <= notAfterDate);
    }

    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--not-before":
                case "--not-after":
                case "--chunk-size":
                case "-f":
                case "--file":
                case "-o":
                case "--output":
                    if (i + 1 < args.length) {
                        arguments.put(args[i].replaceAll("^-+", ""), args[++i]);
                    }
                    break;
                case "--dry-run":
                    arguments.put("dry-run", "true");
                    break;
            }
        }
        return arguments;
    }
}
