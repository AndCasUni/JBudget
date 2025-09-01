package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;

public class IncomeExpenseFilter implements OperationFilter {
    @Override
    public boolean matches(CompleteOperationRow row, FilterCriteria criteria) {
        boolean includeIncomes = criteria.includeIncomes();
        boolean includeExpenses = criteria.includeExpenses();

        if (includeIncomes && includeExpenses) return true;
        if (includeIncomes) return row.getAmount() > 0;
        if (includeExpenses) return row.getAmount() < 0;
        return true;
    }
}