package com.labcontrol;

import com.labcontrol.cli.CommandLineApp;
import com.labcontrol.engine.ProcessMonitor;
import com.labcontrol.model.Policy;
import com.labcontrol.service.PolicyManager;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            new CommandLineApp().start();
            return;
        }

        System.out.println("Lab Control Student Agent Starting Background Service...");
        PolicyManager pm = new PolicyManager();
        Policy policy = pm.load();

        System.out.println("Loaded Policy Mode: " + policy.mode);
        System.out.println("Blocked Apps: " + policy.blockedApps);

        ProcessMonitor monitor = new ProcessMonitor();
        monitor.start(policy);

        System.out.println("Process monitoring active in background.");

        try {
            while (true) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            System.out.println("Student Agent background thread interrupted.");
        }
    }
}
