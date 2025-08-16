package it.unicam.cs.mpgc.jbudget125637.model;

public record Author(int id, String name) {

    @Override
    public String toString() {
        return name;
    }
}
