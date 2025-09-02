package it.unicam.cs.mpgc.jbudget125637.persistency;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;

import java.util.List;

/**
 * Interfaccia generica per la gestione delle operazioni di I/O su repository di operazioni.
 * Definisce i metodi per leggere, salvare e cancellare operazioni.
 * @param <T> Tipo di operazione gestita (es. Operation, Tags)
 */
public interface IOOperationRepository<T> {
    List<T> read();
    void save(List<T> items);
    void delete(String id);
    void delete(boolean all);

}
