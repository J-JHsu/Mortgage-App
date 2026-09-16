package edu.cs213.mortgage.model;

import java.util.Objects;
import java.math.BigDecimal;

public record Filter(Type type, BigDecimal value, BigDecimal maximum) {
    public enum Type {
        MSAMD, LOAN_TYPE, LOAN_PURPOSE, PROPERTY_TYPE, APPLICANT_INCOME, OWNER_OCCUPANCY,
        COUNTY, INCOME_LOAN_RATIO, TRACT_INCOME
    }

    public Filter {
        Objects.requireNonNull(type, "Filter type is required");
        if (type == Type.APPLICANT_INCOME) {
            if (value == null || maximum == null || value.compareTo(maximum) > 0) {
                throw new IllegalArgumentException("Income requires an ordered minimum and maximum.");
            }
        } else if (type == Type.INCOME_LOAN_RATIO || type == Type.TRACT_INCOME) {
            if ((value == null && maximum == null)
                    || (value != null && value.signum() < 0)
                    || (maximum != null && maximum.signum() < 0)
                    || (value != null && maximum != null && value.compareTo(maximum) > 0)) {
                throw new IllegalArgumentException("Invalid numeric range.");
            }
        } else if (value == null || maximum != null) {
            throw new IllegalArgumentException("Only income filters accept a range.");
        }
    }

    public Filter(Type type, int value) {
        this(type, BigDecimal.valueOf(value), null);
    }

    public Filter(Type type, int value, Integer maximum) {
        this(type, BigDecimal.valueOf(value), maximum == null ? null : BigDecimal.valueOf(maximum));
    }
}
