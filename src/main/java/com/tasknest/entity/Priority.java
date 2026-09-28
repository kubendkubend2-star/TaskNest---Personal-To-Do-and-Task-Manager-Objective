package com.tasknest.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.tasknest.exception.InvalidPriorityException;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH;

    @JsonCreator
    public static Priority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (Priority priority : Priority.values()) {
            if (priority.name().equalsIgnoreCase(value.trim())) {
                return priority;
            }
        }
        throw new InvalidPriorityException("Invalid priority. Allowed values: LOW, MEDIUM, HIGH");
    }
}
