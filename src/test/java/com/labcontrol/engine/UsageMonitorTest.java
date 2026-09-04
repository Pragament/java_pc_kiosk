package com.labcontrol.engine;

import junit.framework.TestCase;

public class UsageMonitorTest extends TestCase {

    public void testIsIgnoredProcess() {
        assertTrue(UsageMonitor.isIgnoredProcess("jcmd.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("conhost.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("svchost.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("RuntimeBroker.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("tasklist.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("cmd.exe"));
        assertTrue(UsageMonitor.isIgnoredProcess("powershell.exe"));

        assertFalse(UsageMonitor.isIgnoredProcess("notepad.exe"));
        assertFalse(UsageMonitor.isIgnoredProcess("chrome.exe"));
        assertFalse(UsageMonitor.isIgnoredProcess("msedge.exe"));
        assertFalse(UsageMonitor.isIgnoredProcess("WINWORD.EXE"));
    }

    public void testGetFriendlyAppName() {
        assertEquals("Notepad", UsageMonitor.getFriendlyAppName("notepad.exe"));
        assertEquals("Google Chrome", UsageMonitor.getFriendlyAppName("chrome.exe"));
        assertEquals("Microsoft Edge", UsageMonitor.getFriendlyAppName("msedge.exe"));
        assertEquals("Microsoft Word", UsageMonitor.getFriendlyAppName("WINWORD.EXE"));
        assertEquals("Microsoft Excel", UsageMonitor.getFriendlyAppName("EXCEL.EXE"));
        assertEquals("Microsoft PowerPoint", UsageMonitor.getFriendlyAppName("POWERPNT.EXE"));
        assertEquals("Calculator", UsageMonitor.getFriendlyAppName("calc.exe"));
        assertEquals("Paint", UsageMonitor.getFriendlyAppName("mspaint.exe"));
        assertEquals("Snipping Tool", UsageMonitor.getFriendlyAppName("snippingtool.exe"));

        assertEquals("Customapp", UsageMonitor.getFriendlyAppName("customapp.exe"));
    }
}
