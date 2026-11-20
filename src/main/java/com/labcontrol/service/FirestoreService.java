package com.labcontrol.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FirestoreService {

    private final Firestore db;

    public FirestoreService() {
        Firestore tempDb = null;
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId("eschool-dev-4c6b4")
                        .build();

                FirebaseApp.initializeApp(options);
            }
            tempDb = FirestoreClient.getFirestore();
        } catch (Exception e) {
            System.err.println("Firebase Admin SDK offline / credentials missing: " + e.getMessage());
        }
        this.db = tempDb;
    }

    public void addUsageLog(String classCode, String appName, String processName, String studentName) {
        if (db == null) {
            return;
        }
        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("appName", appName);
            docData.put("processName", processName);
            docData.put("studentLog", "Opened " + appName);
            docData.put("studentName", studentName);
            docData.put("timestamp", System.currentTimeMillis());

            DocumentReference docRef = db.collection("classrooms")
                    .document(classCode)
                    .collection("usageLogs")
                    .document();

            docRef.set(docData).get();
        } catch (Exception e) {
            System.err.println("Error writing log to Firestore: " + e.getMessage());
        }
    }

    public void logActivity(String pcName, String appName, String processName, String action) {
        if (db == null) {
            return;
        }
        try {
            Map<String, Object> docData = new HashMap<>();
            docData.put("pcName", pcName);
            docData.put("appName", appName);
            docData.put("processName", processName);
            docData.put("action", action);
            docData.put("timestamp", System.currentTimeMillis());

            db.collection("pcActivityLogs").document().set(docData);
            System.out.println("[Firestore] Activity logged: " + pcName + " | " + appName + " | " + action);
        } catch (Exception e) {
            System.err.println("Error writing activity log to Firestore: " + e.getMessage());
        }
    }

    public void addUsageLog(String classCode, String processName, String studentName) {
        addUsageLog(classCode, processName, processName, studentName);
    }
}
