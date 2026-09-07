package com.ir.formgenerator.model;

import java.util.Map;


public class FormRecord {

    private final Map<String, String> fields;

    public FormRecord(Map<String, String> fields) {
        this.fields = Map.copyOf(fields);
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public String getField(String name) {
        return fields.getOrDefault(name, "");
    }
}
