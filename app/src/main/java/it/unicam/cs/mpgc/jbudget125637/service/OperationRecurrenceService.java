package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.OperationData;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationRecurrenceService {

    /**
     * Calcola le date di ricorrenza a partire da una data di inizio per il numero di ricorrenze e intevallo specificati.
     * Se l'operazione non è ricorrente, restituisce solo la data di inizio.
     * @param operationData I dati dell'operazione che includono le informazioni sulla ricorrenza.
     * @param startDate La data di inizio per la prima operazione.
     * @return Una lista di date in formato stringa rappresentanti le date di ricorsione.
     */
    public List<String> calculateRecurrenceDates(OperationData operationData, LocalDate startDate) {
        List<String> dates = new ArrayList<>();
        if (startDate != null) {
            dates.add(startDate.toString());

            if (operationData.isRipetizione() || operationData.isRata()) {
                int occurrences = Integer.parseInt(operationData.getOccorrenze());
                int interval = getIntervalDays(operationData.getFrequenza());

                for (int i = 1; i < occurrences; i++) {
                    LocalDate nextDate = startDate.plusDays(i * interval);
                    dates.add(nextDate.toString());
                }
            }
        }
        return dates;
    }

    /**
     * Genera una lista di operazioni basate sui dati forniti, importo, date e tag.
     * Assicura che ogni operazione abbia un ID unico.
     * @param operationData I dati dell'operazione.
     * @param amount L'importo dell'operazione.
     * @param dates Le date per le operazioni.
     * @param tags I tag associati alle operazioni.
     * @param operationService Il servizio per gestire le operazioni esistenti.
     * @return Una lista di operazioni generate.
     */
    public List<Operation> generateOperations(OperationData operationData,
                                              double amount, List<String> dates,
                                              List<Tags> tags, OperationService operationService) {
        List<Operation> operations = new ArrayList<>();
        int numOperations = dates.size();

        Set<String> existingIds = operationService.getAllExistingIds();
        int nextId = Integer.parseInt(operationService.getNextAvailableId());

        for (int i = 0; i < numOperations; i++) {
            double operationAmount = operationData.isRata() ? amount / numOperations : amount;

            while (existingIds.contains(String.valueOf(nextId))) {
                nextId++;
            }

            String uniqueId = String.valueOf(nextId);
            existingIds.add(uniqueId);

            Operation operation = new Operation(
                    uniqueId,
                    operationData.getAutore(),
                    operationData.getDescrizione(),
                    operationAmount,
                    dates.get(i),
                    tags
            );

            operations.add(operation);
            nextId++;
        }

        return operations;
    }

    /**
     * Restituisce il numero di giorni corrispondenti alla frequenza specificata.
     * Se la frequenza non è riconosciuta, restituisce 0.
     * @param frequency La frequenza come stringa.
     * @return Il numero di giorni corrispondenti alla frequenza.
     */
    private int getIntervalDays(String frequency) {
        if (frequency == null) return 0;

        switch (frequency) {
            case "Giornaliera": return 1;
            case "Settimanale": return 7;
            case "Mensile": return 31;
            case "Annuale": return 365;
            default: return 0;
        }
    }
}