package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;

import java.util.List;

public interface IOOperationRepository<T> {
    List<T> read();
    void save(List<T> items);
    void delete(String id);
    void delete(boolean all);

}
