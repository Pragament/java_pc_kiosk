package com.labcontrol.engine;

import com.labcontrol.model.Policy;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessMonitor {

    public void start(Policy policy) {

        new Thread(() -> {

            while (true) {
                try {

                    Process process = Runtime.getRuntime().exec("tasklist");

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    );

                    String line;

                    while ((line = reader.readLine()) != null) {

                        String lowerLine = line.toLowerCase();

                        // skip headers
                        System.out.println(line);
                        if (!lowerLine.contains(".exe")) continue;
                        String processName = line.split("\\s+")[0].toLowerCase();

                        boolean isBlocked = policy.blockedApps.stream()
                                .anyMatch(app -> processName.contains(app.toLowerCase()));

                        if (isBlocked) {
                            Runtime.getRuntime().exec("taskkill /F /IM " + processName);
                        }
                    }

                    Thread.sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}