package it.unicam.cs.mpgc.jbudget125637.model;

public record Tags(int id, String description) {

    public Tags {
        if (id < 0 || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("ID must be non-negative and description must be non-empty");
        }
    }



    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tags tags = (Tags) o;
        return id == tags.id && description.equals(tags.description);
    }

    @Override
    public int hashCode() {
        return 31 * id + description.hashCode();
    }
}
