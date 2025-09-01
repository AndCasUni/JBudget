package it.unicam.cs.mpgc.jbudget125637.service;

import java.time.LocalDate;
import java.util.List;

public class FilterCriteria {
    private final String author;
    private final boolean includeIncomes;
    private final boolean includeExpenses;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<String> selectedTags;

    public FilterCriteria(String author, boolean includeIncomes, boolean includeExpenses,
                          LocalDate startDate, LocalDate endDate, List<String> selectedTags) {
        this.author = author;
        this.includeIncomes = includeIncomes;
        this.includeExpenses = includeExpenses;
        this.startDate = startDate;
        this.endDate = endDate;
        this.selectedTags = selectedTags;
    }

    public String getAuthor() { return author; }
    public boolean includeIncomes() { return includeIncomes; }
    public boolean includeExpenses() { return includeExpenses; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<String> getSelectedTags() { return selectedTags; }
}