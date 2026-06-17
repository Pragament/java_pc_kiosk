# Lab Control System

A Java-based system designed to manage and control application usage in school/college computer labs. It provides real-time monitoring and blocking of unauthorized applications using a policy-driven architecture and CLI interface.

---

## Features Implemented

### CLI-Based Control System
- Interactive command-line interface for managing lab restrictions
- Supports adding blocked applications and websites
- Displays current system status

### Policy Management
- Uses JSON-based storage (`policy.json`) for persisting configurations
- Managed using Jackson library
- Stores:
  - Blocked Applications
  - Blocked Websites
  - System Mode (LAB / OPEN)

### Process Monitoring Engine
- Continuously monitors running system processes
- Detects blocked applications in real time
- Terminates matching processes using system commands

### Blocklist Approach
- Uses blocklist instead of whitelist for simplicity
- Easier to manage in dynamic lab environments

---

## Tech Stack

- Java 17+
- Maven
- Jackson (JSON processing)
- Windows system commands (tasklist, taskkill)

---

## CLI Commands

-block-app <app_name>
-block-site <domain>
-set-mode LAB
-status
-start
-exit


### Example Usage
-block-app calculator.exe
-block-app chrome.exe
-block-site facebook.com
-start


---

## Working Flow

1. User defines restrictions using CLI commands
2. Configuration is stored in `policy.json`
3. Process monitoring engine starts execution
4. System continuously checks running processes
5. If a blocked application is detected, it is terminated immediately

---

## Current Limitations

- Website blocking is not yet active (only stored in configuration)
- Relies on basic process listing (`tasklist`) for detection
- Requires administrator privileges for full functionality
- CLI-based system (no graphical interface)

---

## Future Enhancements

### Website Blocking System
- Implement hosts file-based blocking
- Activate `blockedSites` functionality

### Exam Mode
- Strict system lockdown
- Only approved applications allowed

### GUI Dashboard
- JavaFX-based interface for admin control
- Visual policy management system

### Improved Process Engine
- PID-based monitoring instead of text parsing
- Better accuracy and reliability
- Cross-platform support improvements

---

## How to Run

1. Build the project: java -jar target/lab-control.jar

OR run `Main.java` directly from IDE.

---

## Note

This project is currently focused on implementing Lab Mode using a blocklist-based system for application control. Website filtering and Exam Mode will be added in future updates.
   
