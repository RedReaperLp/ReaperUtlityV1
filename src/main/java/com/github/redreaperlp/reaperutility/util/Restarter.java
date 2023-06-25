package com.github.redreaperlp.reaperutility.util;

import com.github.redreaperlp.reaperutility.Main;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class Restarter implements Runnable {
    private static final String EXIT_COMMAND = "exit";
    private static final String RESTART_COMMAND = "restart";

    private static String oldHash = "";

    public Restarter() {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String[] line = reader.readLine().split(" ");
                    if (line.length == 0) {
                        continue;
                    }
                    switch (line[0]) {
                        case EXIT_COMMAND:
                            Color.YELLOW.printWarning("Exiting Restart Manager");
                            System.exit(0);
                            break;
                        case RESTART_COMMAND:
                            if (line.length == 2) {
                                if (line[1].equals("normal")) {
                                    restart(false);
                                } else if (line[1].equals("debug")) {
                                    restart(true);
                                } else {
                                    Color.RED.printError("Unknown Command");
                                }
                                break;
                            }
                            restart(Main.debug);
                        default:
                            Color.RED.printError("Unknown Command");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void run() {
        oldHash = digest();
        try {
            while (true) {
                TimeUnit.SECONDS.sleep(2);
                String newHash = digest();
                if (!oldHash.equals(newHash)) {
                    new Color.Print("Found new Version", Color.ORANGE)
                            .addLine("Waiting for build to finish...", Color.ORANGE)
                            .printWarning();
                    while (true) {
                        try {
                            oldHash = newHash;
                            TimeUnit.MILLISECONDS.sleep(2000);
                            newHash = digest();
                            if (oldHash.equals(newHash)) {
                                restart(Main.debug);
                                break;
                            }
                        } catch (InterruptedException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void restart(boolean debug) throws IOException, InterruptedException {
        new Color.Print("Restarting...", Color.GREEN).printWarning();
        String batchFileName = debug ? "startD.bat" : "start.bat";
        String pathToBatchFile = new File(batchFileName).getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder(pathToBatchFile);
        pb.start();
        System.exit(0);
    }


    public static String digest() {
        File file = new File(System.getProperty("sun.java.command").split(" ")[0]);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream stream = new FileInputStream(file);
            byte[] bytesArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = stream.read(bytesArray)) > 0) {
                md.update(bytesArray, 0, bytesCount);
            }
            stream.close();

            byte[] bytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Program was Specified Wrong, please check \"programConfig.yaml\"");
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}