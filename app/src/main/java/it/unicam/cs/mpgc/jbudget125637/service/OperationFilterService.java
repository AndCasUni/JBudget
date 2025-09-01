package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.*;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class OperationFilterService {
    private final OperationXmlRepository operationRepository;
    private final List<OperationFilter> filters;

    public OperationFilterService(TagService tagService) {
        this.operationRepository = new OperationXmlRepository();
        this.filters = new ArrayList<>();

        this.filters.add(new AuthorFilter());
        this.filters.add(new IncomeExpenseFilter());
        this.filters.add(new DateRangeFilter());
        this.filters.add(new TagFilter(tagService));
    }

    public ObservableList<CompleteOperationRow> applyFilters(ObservableList<CompleteOperationRow> data,
                                                             FilterCriteria criteria) {
        ObservableList<CompleteOperationRow> filteredData = FXCollections.observableArrayList();

        for (CompleteOperationRow row : data) {
            if (matchesAllFilters(row, criteria)) {
                filteredData.add(row);
            }
        }

        return filteredData;
    }

    private boolean matchesAllFilters(CompleteOperationRow row, FilterCriteria criteria) {
        for (OperationFilter filter : filters) {
            if (!filter.matches(row, criteria)) {
                return false;
            }
        }
        return true;
    }

    public List<CompleteOperationRow> loadAllOperations() {
        List<Operation> operations = operationRepository.read();
        List<CompleteOperationRow> result = new ArrayList<>();

        for (Operation op : operations) {
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                tags.add(i < op.getTags().size() ? op.getTags().get(i).description() : "");
            }

            result.add(new CompleteOperationRow(
                    op.getAutore(), op.getDesc(), op.getDate(),
                    op.getAmount(), tags.get(0), tags.get(1), tags.get(2)
            ));
        }

        return result;
    }

    // Metodo per aggiungere filtri (estensibilità)
    public void addFilter(OperationFilter filter) {
        filters.add(filter);
    }

    // Metodo per rimuovere filtri
    public void removeFilter(Class<? extends OperationFilter> filterClass) {
        filters.removeIf(filter -> filter.getClass().equals(filterClass));
    }
}