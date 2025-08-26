package it.unicam.cs.mpgc.jbudget125637.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public record Operation(String id,
                        String autore,
                        String desc,
                        double amount,
                        String date,
                        List<Tags> tags) {

    public Operation {
        if (id == null || desc == null ||  date == null) {
            throw new IllegalArgumentException("All fields must be non-null");
        }

    }

    public boolean isPlanned() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate opDate = LocalDate.parse(this.date, formatter);
            LocalDate today = LocalDate.now();
            return opDate.isAfter(today); // true se la data è futura
        } catch (DateTimeParseException e) {
            System.err.println("Formato data non valido: " + this.date);
            return false; // considera non programmata se la data non è valida
        }
    }

    public String getId() {
        return id;
    }
    public String getAutore() {
        return autore;
    }
    public String getDesc() {
        return desc;
    }
    public double getAmount() {
        return amount;
    }
    public String getDate() {
        return date;
    }
    public List<Tags> getTags() {
        return tags;
    }
    @Override
    public String toString() {
        return "Operation{" +
                "id='" + id + '\'' +
                ", desc='" + desc + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return Double.compare(operation.amount, amount) == 0 &&
                id.equals(operation.id) &&
                desc.equals(operation.desc) &&
                date.equals(operation.date);
    }
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id.hashCode();
        result = 31 * result + desc.hashCode();
        temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + date.hashCode();
        return result;
    }

}
