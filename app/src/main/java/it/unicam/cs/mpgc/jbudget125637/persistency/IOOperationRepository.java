package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;

import java.util.List;

public interface IOOperationRepository {
    List<CompleteOperationRow> loadAll();
    void save(Operation op);
    void update(Operation op);
    void delete(Operation op);
}
