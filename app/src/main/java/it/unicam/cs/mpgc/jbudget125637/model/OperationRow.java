package it.unicam.cs.mpgc.jbudget125637.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class OperationRow {
    private final SimpleStringProperty author;
    private final SimpleStringProperty date;
    private final SimpleDoubleProperty amount;

    public OperationRow(String author, String date, Double amount) {
        this.author = new SimpleStringProperty(author);
        this.date = new SimpleStringProperty(date);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public String getAuthor() { return author.get(); }
    public String getDate() { return date.get(); }
    public Double getAmount() { return amount.get(); }
}