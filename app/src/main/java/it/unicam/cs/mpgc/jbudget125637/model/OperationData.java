package it.unicam.cs.mpgc.jbudget125637.model;

import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.util.List;

public class OperationData {
    private final String importo;
    private final String descrizione;
    private final LocalDate data;
    private final String autore;
    private final boolean entrata;
    private final boolean uscita;
    private final boolean ripetizione;
    private final boolean rata;
    private final String frequenza;
    private final String occorrenze;
    private final List<String> selectedTags;

    public OperationData(String importo, String descrizione, LocalDate data, String autore,
                         boolean entrata, boolean uscita, boolean ripetizione, boolean rata,
                         String frequenza, String occorrenze, List<String> selectedTags) {
        this.importo = importo;
        this.descrizione = descrizione;
        this.data = data;
        this.autore = autore;
        this.entrata = entrata;
        this.uscita = uscita;
        this.ripetizione = ripetizione;
        this.rata = rata;
        this.frequenza = frequenza;
        this.occorrenze = occorrenze;
        this.selectedTags = selectedTags;
    }

    public String getImporto() { return importo; }
    public String getDescrizione() { return descrizione; }
    public LocalDate getData() { return data; }
    public String getAutore() { return autore; }
    public boolean isEntrata() { return entrata; }
    public boolean isUscita() { return uscita; }
    public boolean isRipetizione() { return ripetizione; }
    public boolean isRata() { return rata; }
    public String getFrequenza() { return frequenza; }
    public String getOccorrenze() { return occorrenze; }
    public List<String> getSelectedTags() { return selectedTags; }
}