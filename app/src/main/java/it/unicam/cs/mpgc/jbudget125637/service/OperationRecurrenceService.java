package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.OperationData;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OperationRecurrenceService {

    public List<String> calculateRecurrenceDates(OperationData operationData, LocalDate startDate) {
        List<String> dates = new ArrayList<>();
        dates.add(startDate.toString());

        int occurrences = Integer.parseInt(operationData.getOccorrenze());
        int interval = getIntervalDays(operationData.getFrequenza());

        for (int i = 1; i < occurrences; i++) {
            LocalDate nextDate = startDate.plusDays(i * interval);
            dates.add(nextDate.toString());
        }

        return dates;
    }

    public List<Operation> generateOperations(OperationData operationData,
                                              double amount, List<String> dates,
                                              List<Tags> tags, String baseId) {
        List<Operation> operations = new ArrayList<>();
        int numOperations = dates.size();

        for (int i = 0; i < numOperations; i++) {
            double operationAmount = operationData.isRata() ? amount / numOperations : amount;

            Operation operation = new Operation(
                    String.valueOf(Integer.parseInt(baseId) + i),
                    operationData.getAutore(),
                    operationData.getDescrizione(),
                    operationAmount,
                    dates.get(i),
                    tags
            );

            operations.add(operation);
        }

        return operations;
    }

    private int getIntervalDays(String frequency) {
        switch (frequency) {
            case "Giornaliera": return 1;
            case "Settimanale": return 7;
            case "Mensile": return 31;
            case "Annuale": return 365;
            default: throw new IllegalArgumentException("Frequenza non valida: " + frequency);
        }
    }
}