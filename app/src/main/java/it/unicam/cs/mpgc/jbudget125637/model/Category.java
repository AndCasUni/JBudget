package it.unicam.cs.mpgc.jbudget125637.model;

public record Category(int id, int parentId, String name) {

    public Category(int id, String name) {
        this(id, 0, name);
    }

    public Category(int id, int parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
