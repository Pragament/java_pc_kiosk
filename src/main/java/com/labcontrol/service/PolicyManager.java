package com.labcontrol.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.labcontrol.model.Policy;

import java.io.File;

public class PolicyManager {
    private static final String FILE = "policy.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public Policy load() {
        try {
            File f = new File(FILE);
            if (!f.exists()) return new Policy();
            return mapper.readValue(f, Policy.class);
        } catch (Exception e) {
            return new Policy();
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
