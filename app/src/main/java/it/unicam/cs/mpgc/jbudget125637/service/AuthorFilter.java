package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;

public class AuthorFilter implements OperationFilter {
    @Override
    public boolean matches(CompleteOperationRow row, FilterCriteria criteria) {
        String author = criteria.getAuthor();
        if (author == null || author.trim().isEmpty()) {
            return true;
        }

        return row.getAuthor().equals(author);
    }
}