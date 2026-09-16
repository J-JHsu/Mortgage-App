package edu.cs213.mortgage.model;

import java.util.Objects;

public record Filter(Type type, int value, Integer maximum) {
    public enum Type {
        MSAMD, LOAN_TYPE, LOAN_PURPOSE, PROPERTY_TYPE, APPLICANT_INCOME, OWNER_OCCUPANCY
    }

    public Filter {
        Objects.requireNonNull(type, "Filter type is required");
        if (type == Type.APPLICANT_INCOME) {
            if (maximum == null || value > maximum) {
                throw new IllegalArgumentException("Income requires an ordered minimum and maximum.");
            }
        } else if (maximum != null) {
            throw new IllegalArgumentException("Only income filters accept a range.");
        }
    }

    public Filter(Type type, int value) {
        this(type, value, null);
    }
}
