package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;

public interface OperationFilter {
    boolean matches(CompleteOperationRow row, FilterCriteria criteria);
}