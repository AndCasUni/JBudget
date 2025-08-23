package it.unicam.cs.mpgc.jbudget125637.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CompleteOperationRow {

    private final StringProperty author;
    private final StringProperty description;
    private final StringProperty date;
    private final DoubleProperty amount;
    private final StringProperty tag1;
    private final StringProperty tag2;
    private final StringProperty tag3;

    public CompleteOperationRow(String author, String description, String date, Double amount, String tag1, String tag2, String tag3) {
        this.author = new SimpleStringProperty(author);
        this.description = new SimpleStringProperty(description);
        this.date = new SimpleStringProperty(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.tag1 = new SimpleStringProperty(tag1);
        this.tag2 = new SimpleStringProperty(tag2);
        this.tag3 = new SimpleStringProperty(tag3);
    }

    public CompleteOperationRow(Operation op) {
        this(op.getAutore(), op.getDesc(), op.getDate(), op.getAmount(),
                op.getTags().size() > 0 ? op.getTags().get(0) : "",
                op.getTags().size() > 1 ? op.getTags().get(1) : "",
                op.getTags().size() > 2 ? op.getTags().get(2) : "");
    }

    // Getter tradizionali
    public String getAuthor() { return author.get(); }
    public String getDescription() { return description.get(); }
    public String getDate() { return date.get(); }
    public Double getAmount() { return amount.get(); }
    public String getTag1() { return tag1.get(); }
    public String getTag2() { return tag2.get(); }
    public String getTag3() { return tag3.get(); }

    // Property getter per TableView
    public StringProperty authorProperty() { return author; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty dateProperty() { return date; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty tag1Property() { return tag1; }
    public StringProperty tag2Property() { return tag2; }
    public StringProperty tag3Property() { return tag3; }
}
