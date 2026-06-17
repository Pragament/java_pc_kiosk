package com.labcontrol.cli;

import com.labcontrol.engine.ProcessMonitor;
import com.labcontrol.model.Policy;
import com.labcontrol.service.PolicyManager;

import java.util.Scanner;

public class CommandLineApp {

    public void start() {

        Scanner sc = new Scanner(System.in);

        System.out.println("Lab Control CLI Started");

        PolicyManager pm = new PolicyManager();
        Policy policy = pm.load();

        ProcessMonitor monitor = new ProcessMonitor();

        while (true) {

            System.out.print("> ");
            String input = sc.nextLine();

            String[] parts = input.trim().split(" ");
            String cmd = parts[0];

            switch (cmd) {

                case "exit":
                    return;

                case "block-app":
                    policy.blockedApps.add(parts[1]);
                    pm.save(policy);
                    System.out.println("Blocked app: " + parts[1]);
                    break;

                case "block-site":
                    policy.blockedSites.add(parts[1]);
                    pm.save(policy);
                    System.out.println("Blocked site: " + parts[1]);
                    break;

                case "set-mode":
                    policy.mode = parts[1];
                    pm.save(policy);
                    System.out.println("Mode set: " + parts[1]);
                    break;

                case "status":
                    System.out.println("Mode: " + policy.mode);
                    System.out.println("Blocked Apps: " + policy.blockedApps);
                    System.out.println("Blocked Sites: " + policy.blockedSites);
                    break;

                case "start":
                    monitor.start(policy);
                    System.out.println("Monitoring started...");
                    break;

                default:
                    System.out.println("Unknown command");
            }
        }
    }
}