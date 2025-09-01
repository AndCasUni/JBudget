package it.unicam.cs.mpgc.jbudget125637.service;

import javafx.scene.control.*;

public class FormCleaner {

    public static void cleanAll(TextField importo, TextField descrizione, DatePicker data,
                                ComboBox<String> autore, RadioButton entrata, RadioButton uscita,
                                CheckBox ripetizione, CheckBox rata, ChoiceBox<String> frequenza,
                                TextField occorrenze, ListView<String> tags, ToggleGroup mainTag,
                                ComboBox<String> sottocat) {
        importo.clear();
        descrizione.clear();
        data.setValue(null);
        autore.getSelectionModel().clearSelection();
        entrata.setSelected(false);
        uscita.setSelected(false);
        ripetizione.setSelected(false);
        rata.setSelected(false);
        frequenza.getSelectionModel().clearSelection();
        occorrenze.clear();
        tags.getItems().clear();
        mainTag.selectToggle(null);
        sottocat.getSelectionModel().clearSelection();
    }
}