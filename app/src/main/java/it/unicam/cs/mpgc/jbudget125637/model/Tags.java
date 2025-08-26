package it.unicam.cs.mpgc.jbudget125637.model;

public record Tags(String id, String description, boolean isParent) {

    public Tags {
    }

    public Tags( String id, String description)
    {
        this(id, description, false);
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
        return description.hashCode();
    }
}
