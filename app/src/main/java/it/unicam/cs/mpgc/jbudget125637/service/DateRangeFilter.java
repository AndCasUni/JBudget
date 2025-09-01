package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateRangeFilter implements OperationFilter {
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter ITALIAN_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public boolean matches(CompleteOperationRow row, FilterCriteria criteria) {
        try {
            LocalDate rowDate = parseDate(row.getDate());
            LocalDate startDate = criteria.getStartDate();
            LocalDate endDate = criteria.getEndDate();

            if (startDate != null && endDate != null) {
                return !rowDate.isBefore(startDate) && !rowDate.isAfter(endDate);
            } else if (startDate != null) {
                return !rowDate.isBefore(startDate);
            } else if (endDate != null) {
                return !rowDate.isAfter(endDate);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, ISO_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr, ITALIAN_FORMATTER);
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Formato data non valido: " + dateStr, dateStr, 0);
            }
        }
    }
}