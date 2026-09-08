package com.labcontrol.engine;

import com.labcontrol.model.Policy;
import com.labcontrol.service.FirestoreService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ProcessMonitor {

    private final FirestoreService firestoreService;
    private final String pcName;

    public ProcessMonitor() {
        this("PC-1", new FirestoreService());
    }

    public ProcessMonitor(String pcName, FirestoreService firestoreService) {
        this.pcName = pcName;
        this.firestoreService = firestoreService;
    }

    public void start(Policy policy) {

        new Thread(() -> {

            Set<String> previousProcesses = new HashSet<>();
            boolean isBaselineEstablished = false;

            while (true) {
                try {
                    Set<String> currentProcesses = new HashSet<>();
                    Process process = Runtime.getRuntime().exec("tasklist");

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    )) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String lowerLine = line.toLowerCase();
                            if (!lowerLine.contains(".exe")) continue;
                            String processName = line.split("\\s+")[0].toLowerCase();
                            currentProcesses.add(processName);
                        }
                    }

                    if (!isBaselineEstablished) {
                        previousProcesses.addAll(currentProcesses);
                        isBaselineEstablished = true;
                    } else {
                        for (String processName : currentProcesses) {
                            if (!previousProcesses.contains(processName)) {
                                if (UsageMonitor.isIgnoredProcess(processName)) {
                                    continue;
                                }

                                String appName = UsageMonitor.getFriendlyAppName(processName);
                                boolean isBlocked = policy.blockedApps.stream()
                                        .anyMatch(app -> processName.contains(app.toLowerCase()) || app.toLowerCase().contains(processName.replace(".exe", "")));

                                if (isBlocked) {
                                    System.out.println("[BLOCKED] " + appName + " (" + processName + ") on " + pcName);
                                    firestoreService.logActivity(pcName, appName, processName, "BLOCKED");
                                    Runtime.getRuntime().exec("taskkill /F /IM " + processName);
                                } else {
                                    System.out.println("[OPENED] " + appName + " (" + processName + ") on " + pcName);
                                    firestoreService.logActivity(pcName, appName, processName, "OPENED");
                                }
                            } else {
                                boolean isBlocked = policy.blockedApps.stream()
                                        .anyMatch(app -> processName.contains(app.toLowerCase()) || app.toLowerCase().contains(processName.replace(".exe", "")));
                                if (isBlocked) {
                                    Runtime.getRuntime().exec("taskkill /F /IM " + processName);
                                }
                            }
                        }
                        previousProcesses = currentProcesses;
                    }

                    Thread.sleep(2000);

                } catch (Exception e) {
                    System.err.println("Error in ProcessMonitor loop: " + e.getMessage());
                }
            }

        }, "ProcessMonitorThread").start();
    }
}