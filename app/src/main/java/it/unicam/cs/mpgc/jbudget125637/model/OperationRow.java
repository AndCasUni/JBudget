package it.unicam.cs.mpgc.jbudget125637.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OperationRow {
    private final SimpleStringProperty author;
    private final SimpleStringProperty date;
    private final SimpleDoubleProperty amount;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OperationRow(String author, String date, Double amount) {
        this.author = new SimpleStringProperty(author);

        // converto da yyyy-MM-dd → dd/MM/yyyy
        LocalDate parsedDate = LocalDate.parse(date, INPUT_FORMAT);
        String formattedDate = parsedDate.format(OUTPUT_FORMAT);

        this.date = new SimpleStringProperty(formattedDate);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public String getAuthor() { return author.get(); }
    public String getDate() { return date.get(); }
    public Double getAmount() { return amount.get(); }
}
