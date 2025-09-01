package it.unicam.cs.mpgc.jbudget125637.service;

import it.unicam.cs.mpgc.jbudget125637.model.OperationData;

import java.util.List;

public class ValidationService {

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

    private boolean validateRequiredFields(OperationData operationData) {
        return operationData.getImporto() != null && !operationData.getImporto().isBlank() &&
                operationData.getDescrizione() != null && !operationData.getDescrizione().isBlank() &&
                operationData.getData() != null &&
                operationData.getAutore() != null && !operationData.getAutore().isBlank() &&
                (operationData.isEntrata() || operationData.isUscita()) &&
                operationData.getSelectedTags() != null && !operationData.getSelectedTags().isEmpty();
    }

    private boolean validateAmount(String amount) {
        try {
            Double.parseDouble(amount);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateFrequency(String frequency) {
        return frequency != null && !frequency.isBlank();
    }

    private boolean validateOccurrences(String occurrences) {
        try {
            int occ = Integer.parseInt(occurrences);
            return occ > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean validateTags(List<String> tags) {
        return tags != null && !tags.isEmpty() && tags.size() <= 3;
    }

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