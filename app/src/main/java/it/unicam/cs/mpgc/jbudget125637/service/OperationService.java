package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.OperationRow;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class OperationService {
    private final OperationXmlRepository repository;

    public OperationService() {
        this.repository = new OperationXmlRepository();
    }

    public List<Operation> getAllOperations() {
        return repository.read();
    }

    public ObservableList<OperationRow> getRecentOperations(int count) {
        List<Operation> operations = getAllOperations();
        ObservableList<OperationRow> recentOperations = FXCollections.observableArrayList();

        if (operations.isEmpty()) {
            return recentOperations;
        }

        for (int i = Math.max(0, operations.size() - count); i < operations.size(); i++) {
            Operation op = operations.get(i);
            recentOperations.add(new OperationRow(op.autore(), op.date(), op.getAmount()));
        }

        return recentOperations;
    }

    public void saveOperations(List<Operation> operations) {
        repository.save(operations);
    }

    public String getMaxOperationId(List<Operation> operations) {
        if (operations.isEmpty()) {
            return "0";
        }
        return operations.stream()
                .map(Operation::getId)
                .max(String::compareTo)
                .orElse("0");
    }
}