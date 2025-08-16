package it.unicam.cs.mpgc.jbudget125637.model;

public record Currency(String code, double toEuro, double fromEuro) {

    public Currency {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Currency code must be non-null and non-empty");
        }
        if (toEuro <= 0 || fromEuro <= 0) {
            throw new IllegalArgumentException("Conversion rates must be positive");
        }
    }

    public String getCode() {
        return code;
    }

    public double getToEuro() {
        return toEuro;
    }

    public double getFromEuro() {
        return fromEuro;
    }
}
