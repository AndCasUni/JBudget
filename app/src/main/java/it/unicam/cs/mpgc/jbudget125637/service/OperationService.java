package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.OperationRow;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OperationService {
    private final OperationXmlRepository repository;

    public OperationService() {
        this.repository = new OperationXmlRepository();
    }

    /**
     * Restituisce tutte le operazioni salvate
     * @return Lista di tutte le operazioni
     */
    public List<Operation> getAllOperations() {
        return repository.read();
    }

    /**
     * restituisce le ultime 'count' operazioni in ordine decrescente di inserimento
     * @param count Numero di operazioni recenti da restituire
     * @return Lista osservabile di OperationRow contenente le operazioni recenti
     */
    public ObservableList<OperationRow> getRecentOperations(int count) {
        List<Operation> operations = getAllOperations();
        ObservableList<OperationRow> recentOperations = FXCollections.observableArrayList();

        if (operations.isEmpty()) {
            return recentOperations;
        }

        int startIndex = Math.max(0, operations.size() - count);

        for (int i = operations.size() - 1; i >= startIndex; i--) {
            Operation op = operations.get(i);
            recentOperations.add(new OperationRow(op.autore(), op.date(), op.getAmount()));
        }

        return recentOperations;
    }

    /**
     * Salva una lista di nuove operazioni
     * @param newOperations Lista di nuove operazioni da salvare
     */
    public void saveOperations(List<Operation> newOperations) {
        repository.appendOperations(newOperations);
    }

    /**
     * Trova l'ID massimo tra le operazioni esistenti
     * @param operations Lista di operazioni
     * @return ID massimo come Stringa
     */
    public String getMaxOperationId(List<Operation> operations) {
        if (operations.isEmpty()) {
            return "0";
        }
        return operations.stream()
                .map(Operation::getId)
                .max((id1, id2) -> {
                    try {
                        return Integer.compare(Integer.parseInt(id1), Integer.parseInt(id2));
                    } catch (NumberFormatException e) {
                        return id1.compareTo(id2);
                    }
                })
                .orElse("0");
    }

    /**
     * Trova il prossimo ID disponibile
     * @return Prossimo ID disponibile come Stringa
     */
    public String getNextAvailableId() {
        List<Operation> operations = getAllOperations();
        if (operations.isEmpty()) {
            return "1";
        }

        int maxId = operations.stream()
                .mapToInt(op -> {
                    try {
                        return Integer.parseInt(op.getId());
                    } catch (NumberFormatException e) {
                        return 0; // Ignora ID non numerici
                    }
                })
                .max()
                .orElse(0);

        return String.valueOf(maxId + 1);
    }

    /**
     * Metodo per ottenere tutti gli ID esistenti
     * @return Set di ID esistenti
     */
    public Set<String> getAllExistingIds() {
        return getAllOperations().stream()
                .map(Operation::getId)
                .collect(Collectors.toSet());
    }
}