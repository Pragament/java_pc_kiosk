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
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .setProjectId("eschool-dev-4c6b4")
                        .build();

                FirebaseApp.initializeApp(options);
            }
            this.db = FirestoreClient.getFirestore();
        } catch (IOException e) {
            System.err.println("Error initializing Firebase Admin SDK: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase Admin SDK", e);
        }
    }

    public void addUsageLog(String classCode, String appName, String processName, String studentName) {
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

    public void addUsageLog(String classCode, String processName, String studentName) {
        addUsageLog(classCode, processName, processName, studentName);
    }
}
