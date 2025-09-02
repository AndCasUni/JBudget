package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.OperationData;

import java.util.List;

public class ValidationService {

    /**
     * Valida i dati dell'operazione.
     * @param operationData dati dell'operazione da validare.
     * @return true se i dati sono validi, false altrimenti.
     */
    public boolean validateOperationData(OperationData operationData) {
        if (!validateRequiredFields(operationData)) {
            return false;
        }

        if (!validateAmount(operationData.getImporto())) {
            return false;
        }

        if (operationData.isRipetizione() || operationData.isRata()) {
            if (!validateFrequency(operationData.getFrequenza())) {
                return false;
            }
            if (!validateOccurrences(operationData.getOccorrenze())) {
                return false;
            }
        }

        return validateTags(operationData.getSelectedTags());
    }

    /**
     * Valida che i campi obbligatori non siano null o vuoti.
     * @param operationData dati dell'operazione da validare.
     * @return true se tutti i campi obbligatori sono validi, false altrimenti.
     */
    private boolean validateRequiredFields(OperationData operationData) {
        return operationData.getImporto() != null && !operationData.getImporto().isBlank() &&
                operationData.getDescrizione() != null && !operationData.getDescrizione().isBlank() &&
                operationData.getData() != null &&
                operationData.getAutore() != null && !operationData.getAutore().isBlank() &&
                (operationData.isEntrata() || operationData.isUscita()) &&
                operationData.getSelectedTags() != null && !operationData.getSelectedTags().isEmpty();
    }

    /**
     * Valida che l'importo sia un numero valido.
     * @param amount importo da validare.
     * @return true se l'importo è valido, false altrimenti.
     */
    private boolean validateAmount(String amount) {
        try {
            Double.parseDouble(amount);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida che la frequenza non sia null o vuota.
     * @param frequency frequenza da validare.
     * @return true se la frequenza è valida, false altrimenti.
     */
    private boolean validateFrequency(String frequency) {
        return frequency != null && !frequency.isBlank();
    }

    /**
     * Valida che le occorrenze siano un numero intero positivo.
     * @param occurrences occorrenze da validare.
     * @return true se le occorrenze sono valide, false altrimenti.
     */
    private boolean validateOccurrences(String occurrences) {
        try {
            int occ = Integer.parseInt(occurrences);
            return occ > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida che la lista di tag non sia null, non sia vuota e contenga al massimo 3 tag.
     * @param tags lista di tag da validare.
     * @return true se la lista di tag è valida, false altrimenti.
     */
    private boolean validateTags(List<String> tags) {
        return tags != null && !tags.isEmpty() && tags.size() <= 3;
    }

    /**
     * Valida l'aggiunta di un nuovo tag alla lista esistente.
     * @param tagToAdd tag da aggiungere.
     * @param existingTags lista di tag esistenti.
     * @return true se il tag può essere aggiunto, false altrimenti.
     */
    public boolean validateTagAddition(String tagToAdd, List<String> existingTags) {
        if (tagToAdd == null || tagToAdd.isBlank()) {
            return false;
        }

        if (existingTags.contains(tagToAdd)) {
            return false;
        }

        return existingTags.size() < 3;
    }
}