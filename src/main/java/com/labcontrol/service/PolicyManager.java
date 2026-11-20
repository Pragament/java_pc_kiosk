package com.labcontrol.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labcontrol.model.Policy;

import java.io.File;

public class PolicyManager {
    private static final String FILE = "policy.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public Policy createDefaultPolicy() {
        Policy policy = new Policy();
        policy.mode = "LOCKED";
        policy.blockedApps.add("msedge.exe");
        save(policy);
        return policy;
    }

    public Policy load() {
        try {
            File f = new File(FILE);
            if (!f.exists()) {
                return createDefaultPolicy();
            }
            Policy policy = mapper.readValue(f, Policy.class);
            if (policy.blockedApps == null) {
                policy.blockedApps = new java.util.ArrayList<>();
            }
            if (!policy.blockedApps.contains("msedge.exe")) {
                policy.blockedApps.add("msedge.exe");
            }
            if ("OPEN".equals(policy.mode)) {
                policy.mode = "LOCKED";
            }
            return policy;
        } catch (Exception e) {
            return createDefaultPolicy();
        }
    }

    public void save(Policy policy) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE), policy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
