package com.labcontrol.engine;

import com.labcontrol.service.FirestoreService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UsageMonitor {

    private final FirestoreService firestoreService;
    private volatile boolean running = false;

    private static final Set<String> IGNORED_SYSTEM_PROCESSES = new HashSet<>(Arrays.asList(
            "jcmd.exe", "java.exe", "javaw.exe", "javac.exe", "jshell.exe", "jps.exe", "jstat.exe",
            "cmd.exe", "powershell.exe", "pwsh.exe", "conhost.exe",
            "svchost.exe", "runtimebroker.exe", "system", "system idle process",
            "smss.exe", "csrss.exe", "wininit.exe", "services.exe", "lsass.exe", "lsm.exe",
            "winlogon.exe", "explorer.exe", "dwm.exe", "sihost.exe", "taskhostw.exe", "taskhost.exe",
            "shellexperiencehost.exe", "startmenuexperiencehost.exe", "ctfmon.exe", "fontdrvhost.exe",
            "wuauclt.exe", "wuauserv.exe", "dllhost.exe", "rundll32.exe", "regsvr32.exe",
            "applicationframehost.exe", "backgroundtaskhost.exe", "systemsettings.exe", "setting-sync.exe",
            "taskmgr.exe", "tasklist.exe", "taskkill.exe", "audiodg.exe", "spoolsv.exe",
            "smartscreen.exe", "securityhealthservice.exe", "securityhealthsystray.exe",
            "mpcmdrun.exe", "msmpeng.exe", "nisc.exe", "wmiipc.exe", "wmiprvse.exe",
            "mscorsvw.exe", "ngentask.exe", "compattelrunner.exe", "deviceassociationprovider.exe",
            "dashost.exe", "upfc.exe", "usocoreworker.exe", "edgewebview2.exe", "msedgewebview2.exe",
            "gamebar.exe", "gamebarpresenter.exe", "xboxgamebar.exe", "xboxstat.exe",
            "textinputhost.exe", "widgets.exe", "phoneexperiencehost.exe", "lockapp.exe",
            "searchhost.exe", "searchindexer.exe", "searchprotocolhost.exe", "searchfilterhost.exe"
    ));

    private static final Map<String, String> APP_NAME_MAP = new HashMap<>();

    static {
        APP_NAME_MAP.put("notepad.exe", "Notepad");
        APP_NAME_MAP.put("chrome.exe", "Google Chrome");
        APP_NAME_MAP.put("msedge.exe", "Microsoft Edge");
        APP_NAME_MAP.put("winword.exe", "Microsoft Word");
        APP_NAME_MAP.put("excel.exe", "Microsoft Excel");
        APP_NAME_MAP.put("powerpnt.exe", "Microsoft PowerPoint");
        APP_NAME_MAP.put("calc.exe", "Calculator");
        APP_NAME_MAP.put("calculatorapp.exe", "Calculator");
        APP_NAME_MAP.put("mspaint.exe", "Paint");
        APP_NAME_MAP.put("snippingtool.exe", "Snipping Tool");
        APP_NAME_MAP.put("snippingtoolprocess.exe", "Snipping Tool");
        APP_NAME_MAP.put("firefox.exe", "Mozilla Firefox");
        APP_NAME_MAP.put("brave.exe", "Brave Browser");
        APP_NAME_MAP.put("opera.exe", "Opera");
        APP_NAME_MAP.put("vlc.exe", "VLC Media Player");
        APP_NAME_MAP.put("code.exe", "Visual Studio Code");
        APP_NAME_MAP.put("idea64.exe", "IntelliJ IDEA");
        APP_NAME_MAP.put("idea.exe", "IntelliJ IDEA");
        APP_NAME_MAP.put("eclipse.exe", "Eclipse");
        APP_NAME_MAP.put("netbeans.exe", "NetBeans");
        APP_NAME_MAP.put("netbeans64.exe", "NetBeans");
        APP_NAME_MAP.put("teams.exe", "Microsoft Teams");
        APP_NAME_MAP.put("zoom.exe", "Zoom");
        APP_NAME_MAP.put("discord.exe", "Discord");
        APP_NAME_MAP.put("spotify.exe", "Spotify");
        APP_NAME_MAP.put("wordpad.exe", "WordPad");
        APP_NAME_MAP.put("onenote.exe", "Microsoft OneNote");
        APP_NAME_MAP.put("msaccess.exe", "Microsoft Access");
        APP_NAME_MAP.put("photoshop.exe", "Adobe Photoshop");
        APP_NAME_MAP.put("illustrator.exe", "Adobe Illustrator");
        APP_NAME_MAP.put("blender.exe", "Blender");
        APP_NAME_MAP.put("pycharm64.exe", "PyCharm");
        APP_NAME_MAP.put("pycharm.exe", "PyCharm");
        APP_NAME_MAP.put("clion64.exe", "CLion");
        APP_NAME_MAP.put("clion.exe", "CLion");
        APP_NAME_MAP.put("webstorm64.exe", "WebStorm");
        APP_NAME_MAP.put("webstorm.exe", "WebStorm");
        APP_NAME_MAP.put("devenv.exe", "Visual Studio");
        APP_NAME_MAP.put("sublime_text.exe", "Sublime Text");
        APP_NAME_MAP.put("notepad++.exe", "Notepad++");
        APP_NAME_MAP.put("acrord32.exe", "Adobe Acrobat");
        APP_NAME_MAP.put("acrobat.exe", "Adobe Acrobat");
    }

    public UsageMonitor() {
        this.firestoreService = new FirestoreService();
    }

    public UsageMonitor(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    public void start(String classCode, String studentName) {
        if (running) {
            System.out.println("UsageMonitor is already running.");
            return;
        }
        running = true;
        System.out.println("Usage monitoring started...");

        new Thread(() -> {
            Set<String> previousProcesses = new HashSet<>();
            boolean isBaselineEstablished = false;

            while (running) {
                try {
                    Set<String> currentProcesses = getRunningProcesses();

                    if (!isBaselineEstablished) {
                        previousProcesses.addAll(currentProcesses);
                        isBaselineEstablished = true;
                    } else {
                        for (String processName : currentProcesses) {
                            if (!previousProcesses.contains(processName)) {
                                if (isIgnoredProcess(processName)) {
                                    continue;
                                }
                                String appName = getFriendlyAppName(processName);
                                System.out.println("New application opened: " + appName + " (" + processName + ")");
                                firestoreService.addUsageLog(classCode, appName, processName, studentName);
                                System.out.println("Firestore log added: " + appName);
                            }
                        }
                        previousProcesses = currentProcesses;
                    }

                    Thread.sleep(2000);
                } catch (Exception e) {
                    System.err.println("Error in UsageMonitor thread: " + e.getMessage());
                }
            }
        }, "UsageMonitorThread").start();
    }

    public void stop() {
        running = false;
    }

    public static boolean isIgnoredProcess(String processName) {
        if (processName == null) return true;
        return IGNORED_SYSTEM_PROCESSES.contains(processName.toLowerCase());
    }

    public static String getFriendlyAppName(String processName) {
        if (processName == null || processName.trim().isEmpty()) {
            return "Unknown Application";
        }
        String lower = processName.toLowerCase().trim();
        if (APP_NAME_MAP.containsKey(lower)) {
            return APP_NAME_MAP.get(lower);
        }
        String nameWithoutExt = lower.endsWith(".exe") ? lower.substring(0, lower.length() - 4) : lower;
        if (nameWithoutExt.isEmpty()) {
            return processName;
        }
        return Character.toUpperCase(nameWithoutExt.charAt(0)) + nameWithoutExt.substring(1);
    }

    private Set<String> getRunningProcesses() {
        Set<String> processes = new HashSet<>();
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String lowerLine = line.toLowerCase();
                    if (!lowerLine.contains(".exe")) continue;
                    
                    String trimmed = line.trim();
                    int exeIdx = trimmed.toLowerCase().indexOf(".exe");
                    if (exeIdx != -1) {
                        String processName = trimmed.substring(0, exeIdx + 4).toLowerCase();
                        processes.add(processName);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading process list from tasklist: " + e.getMessage());
        }
        return processes;
    }
}
