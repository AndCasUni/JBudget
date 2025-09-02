package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
import it.unicam.cs.mpgc.jbudget125637.model.FilterCriteria;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class RevisionController {
    @FXML private ComboBox<String> rev_autore;
    @FXML private CheckBox rev_entrate;
    @FXML private CheckBox rev_uscite;
    @FXML private DatePicker rev_dadata;
    @FXML private DatePicker rev_adata;
    @FXML private ToggleButton rev_casa;
    @FXML private ToggleButton rev_lavoro;
    @FXML private ToggleButton rev_hobby;
    @FXML private ToggleButton rev_sport;
    @FXML private ToggleButton rev_cibo;
    @FXML private ToggleButton rev_salute;

    @FXML private TableView<CompleteOperationRow> rev_lista;
    @FXML private TableColumn<CompleteOperationRow, String> rev_colautore;
    @FXML private TableColumn<CompleteOperationRow, String> rev_coldata;
    @FXML private TableColumn<CompleteOperationRow, String> rev_coldesc;
    @FXML private TableColumn<CompleteOperationRow, Double> rev_colimpo;
    @FXML private TableColumn<CompleteOperationRow, String> rev_coltag1;
    @FXML private TableColumn<CompleteOperationRow, String> rev_coltag2;
    @FXML private TableColumn<CompleteOperationRow, String> rev_coltag3;

    private final OperationService operationService;
    private final TagService tagService;
    private final AuthorService authorService;
    private ObservableList<CompleteOperationRow> data = FXCollections.observableArrayList();

    public RevisionController() {
        this.operationService = new OperationService();
        this.tagService = new TagService();
        this.authorService = new AuthorService();
    }

    @FXML
    public void initialize() {
        RefreshService.registerRevisioneController(this);
        setupTableColumns();
        loadAuthors();
        rev_lista.setItems(null);
    }

    private void setupTableColumns() {
        rev_colautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        rev_coldesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        rev_coldata.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        rev_colimpo.setCellValueFactory(new PropertyValueFactory<>("amount"));
        rev_coltag1.setCellValueFactory(new PropertyValueFactory<>("tag1"));
        rev_coltag2.setCellValueFactory(new PropertyValueFactory<>("tag2"));
        rev_coltag3.setCellValueFactory(new PropertyValueFactory<>("tag3"));
    }

    /**
     * Metodo chiamato quando si preme il pulsante "Cerca".
     * Carica le operazioni, costruisce i criteri di filtro e applica i filtri alla tabella.
     */
    @FXML
    public void cerca() {
        loadOperations();
        FilterCriteria criteria = buildFilterCriteria();
        ObservableList<CompleteOperationRow> filteredData = applyFilters(data, criteria);
        rev_lista.setItems(filteredData);
    }

    private FilterCriteria buildFilterCriteria() {
        return new FilterCriteria(
                rev_autore.getValue(),
                rev_entrate.isSelected(),
                rev_uscite.isSelected(),
                rev_dadata.getValue(),
                rev_adata.getValue(),
                getSelectedTags()
        );
    }

    /**
     * Raccoglie i tag selezionati dai ToggleButton.
     * Utilizza una mappa per associare ogni ToggleButton al suo testo (tag).
     */
    private List<String> getSelectedTags() {
        List<String> selectedTags = new ArrayList<>();
        Map<ToggleButton, String> tagButtons = createTagButtonMap();

        tagButtons.forEach((button, tag) -> {
            if (button.isSelected()) selectedTags.add(tag);
        });

        return selectedTags;
    }
    /**
     * Crea una mappa che associa ogni ToggleButton al suo testo (tag).
     * Questo aiuta a gestire i tag in modo più dinamico.
     */
    private Map<ToggleButton, String> createTagButtonMap() {
        Map<ToggleButton, String> map = new HashMap<>();
        map.put(rev_casa, rev_casa.getText());
        map.put(rev_lavoro, rev_lavoro.getText());
        map.put(rev_hobby, rev_hobby.getText());
        map.put(rev_sport, rev_sport.getText());
        map.put(rev_cibo, rev_cibo.getText());
        map.put(rev_salute, rev_salute.getText());
        return map;
    }

    /**
     * Carica tutte le operazioni dal servizio e le converte in CompleteOperationRow.
     * I dati caricati vengono memorizzati nella lista osservabile 'data'.
     */
    private void loadOperations() {
        List<CompleteOperationRow> operations = convertToCompleteOperationRows(
                operationService.getAllOperations()
        );
        data.clear();
        data.addAll(operations);
    }

    /**
     * Converte una lista di Operation in una lista di CompleteOperationRow.
     * Ogni operazione può avere fino a 3 tag; se ne ha meno, i tag mancanti vengono riempiti con stringhe vuote.
     */
    private List<CompleteOperationRow> convertToCompleteOperationRows(List<Operation> operations) {
        List<CompleteOperationRow> result = new ArrayList<>();
        for (Operation op : operations) {
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                tags.add(i < op.getTags().size() ? op.getTags().get(i).description() : "");
            }

            result.add(new CompleteOperationRow(
                    op.getAutore(), op.getDesc(), op.getDate(),
                    op.getAmount(), tags.get(0), tags.get(1), tags.get(2)
            ));
        }
        return result;
    }

    /**
     * Applica i filtri specificati nei criteri di filtro ai dati forniti.
     * Restituisce una nuova lista osservabile contenente solo le righe che soddisfano tutti i criteri di filtro.
     */
    private ObservableList<CompleteOperationRow> applyFilters(ObservableList<CompleteOperationRow> data,
                                                              FilterCriteria criteria) {
        ObservableList<CompleteOperationRow> filteredData = FXCollections.observableArrayList();
        Set<String> expandedTags = expandTags(criteria.getSelectedTags());

        for (CompleteOperationRow row : data) {
            if (matchesAllFilters(row, criteria, expandedTags)) {
                filteredData.add(row);
            }
        }

        return filteredData;
    }

    /**
     * Verifica se una riga soddisfa tutti i criteri di filtro specificati.
     * Controlla autore, tipo di operazione (entrata/uscita), intervallo di date e tag.
     */
    private boolean matchesAllFilters(CompleteOperationRow row, FilterCriteria criteria,
                                      Set<String> expandedTags) {
        return matchesAuthor(row, criteria.getAuthor()) &&
                matchesIncomeExpense(row, criteria.includeIncomes(), criteria.includeExpenses()) &&
                matchesDateRange(row, criteria.getStartDate(), criteria.getEndDate()) &&
                matchesTags(row, expandedTags);
    }
    /** Metodi di supporto per il filtraggio */
    private boolean matchesAuthor(CompleteOperationRow row, String author) {
        return author == null || author.isEmpty() || row.getAuthor().equals(author);
    }

    private boolean matchesIncomeExpense(CompleteOperationRow row, boolean includeIncomes,
                                         boolean includeExpenses) {
        if (includeIncomes && includeExpenses) return true;
        if (includeIncomes) return row.getAmount() > 0;
        if (includeExpenses) return row.getAmount() < 0;
        return true;
    }

    private boolean matchesDateRange(CompleteOperationRow row, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDate rowDate = parseDate(row.getDate());

            if (startDate != null && endDate != null) {
                return !rowDate.isBefore(startDate) && !rowDate.isAfter(endDate);
            } else if (startDate != null) {
                return !rowDate.isBefore(startDate);
            } else if (endDate != null) {
                return !rowDate.isAfter(endDate);
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean matchesTags(CompleteOperationRow row, Set<String> expandedTags) {
        if (expandedTags.isEmpty()) return true;

        return expandedTags.stream().anyMatch(tag ->
                tag.equals(row.getTag1()) || tag.equals(row.getTag2()) || tag.equals(row.getTag3())
        );
    }

    /**
     * Espande i tag selezionati includendo anche i loro tag figli.
     * Utilizza il servizio TagService per ottenere i tag figli.
     */
    private Set<String> expandTags(List<String> selectedTags) {
        Set<String> expandedTags = new HashSet<>();
        for (String tag : selectedTags) {
            expandedTags.add(tag);
            expandedTags.addAll(tagService.getChildTags(tag));
        }
        return expandedTags;
    }

    /** Metodo di supporto per il parsing delle date in diversi formati */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Formato data non valido: " + dateStr, dateStr, 0);
            }
        }
    }

    private void loadAuthors() {
        List<String> authors = authorService.getAllAuthorNames();

        rev_autore.getItems().clear();
        rev_autore.getItems().add("");
        rev_autore.getItems().addAll(authors);

        rev_autore.setValue("");
    }

    @FXML
    public void clearFilters() {
        rev_autore.setValue("");
        rev_entrate.setSelected(false);
        rev_uscite.setSelected(false);
        rev_dadata.setValue(null);
        rev_adata.setValue(null);

        rev_casa.setSelected(false);
        rev_lavoro.setSelected(false);
        rev_hobby.setSelected(false);
        rev_sport.setSelected(false);
        rev_cibo.setSelected(false);
        rev_salute.setSelected(false);

        loadOperations();
        rev_lista.setItems(data);
    }
}