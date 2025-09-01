package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.*;
import it.unicam.cs.mpgc.jbudget125637.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class AddController {

    // UI Components
    @FXML private TextField add_imp;
    @FXML private TextField add_desc;
    @FXML private DatePicker add_data;
    @FXML private ComboBox<String> add_autore;
    @FXML private RadioButton add_entrata;
    @FXML private RadioButton add_out;
    @FXML private CheckBox add_rep;
    @FXML private CheckBox add_rata;
    @FXML private ChoiceBox<String> add_freq;
    @FXML private TextField add_occ;
    @FXML private TableView<OperationRow> add_recenti;
    @FXML private TableColumn<OperationRow, String> add_recautore;
    @FXML private TableColumn<OperationRow, String> add_recdata;
    @FXML private TableColumn<OperationRow, Double> add_recimporto;
    @FXML private ComboBox<String> add_sottocat;
    @FXML private ListView<String> add_tags;
    @FXML private ToggleGroup main_tag;

    // Services
    private final OperationService operationService;
    private final TagService tagService;
    private final AuthorService authorService;
    private final ValidationService validationService;
    private final OperationRecurrenceService recurrenceService;

    private List<Operation> operations;
    private String maxId;

    public AddController() {
        this.operationService = new OperationService();
        this.tagService = new TagService();
        this.authorService = new AuthorService();
        this.validationService = new ValidationService();
        this.recurrenceService = new OperationRecurrenceService();
    }

    @FXML
    public void initialize() {
        initializeServices();
        setupTableColumns();
        setupFrequencyOptions();
        setupEventListeners();
        loadInitialData();
    }

    private void initializeServices() {
        operations = operationService.getAllOperations();
    }

    private void setupTableColumns() {
        add_recautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        add_recdata.setCellValueFactory(new PropertyValueFactory<>("date"));
        add_recimporto.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void setupFrequencyOptions() {
        add_freq.setItems(FXCollections.observableArrayList(
                "Giornaliera", "Settimanale", "Mensile", "Annuale"
        ));
    }

    private void setupEventListeners() {
        setupCategorySelectionListener();
        setupRepetitionRateListeners();
        setupFrequencyVisibility();
    }

    private void setupCategorySelectionListener() {
        main_tag.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                loadSottocat(((ToggleButton) newToggle).getText());
            } else {
                loadSottocat(null);
            }
        });
    }

    private void setupRepetitionRateListeners() {
        add_rep.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                add_rata.setSelected(false);
            }
            updateFrequencyVisibility();
        });

        add_rata.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                add_rep.setSelected(false);
            }
            updateFrequencyVisibility();
        });
    }

    private void setupFrequencyVisibility() {
        add_freq.setVisible(false);
        add_occ.setVisible(false);
    }

    private void loadInitialData() {
        refreshRecentOperationsList();
        loadSottocat(null);
        loadAuthors();
    }

    @FXML
    public void addInsert() {
        try {
            OperationData operationData = collectOperationData();

            if (!validationService.validateOperationData(operationData)) {
                showValidationError();
                return;
            }

            List<Operation> operationsToAdd = createOperations(operationData);
            operationService.saveOperations(operationsToAdd);

            showSuccessMessage();
            cleanAll();
            refreshRecentOperationsList();

        } catch (Exception e) {
            showError("Errore durante l'inserimento: " + e.getMessage());
        }
    }

    private OperationData collectOperationData() {
        return new OperationData(
                add_imp.getText(),
                add_desc.getText(),
                add_data.getValue(),
                (String) add_autore.getValue(),
                add_entrata.isSelected(),
                add_out.isSelected(),
                add_rep.isSelected(),
                add_rata.isSelected(),
                (String) add_freq.getValue(),
                add_occ.getText(),
                add_tags.getItems()
        );
    }

    private List<Operation> createOperations(OperationData operationData) {
        double amount = calculateAmount(operationData);
        List<String> dates = calculateDates(operationData);
        List<Tags> tags = resolveTags(operationData.getSelectedTags());

        return recurrenceService.generateOperations(
                operationData, amount, dates, tags, getNextOperationId()
        );
    }

    private double calculateAmount(OperationData operationData) {
        double amount = Double.parseDouble(operationData.getImporto());
        if (operationData.isUscita()) {
            amount = -amount;
        }
        return amount;
    }

    private List<String> calculateDates(OperationData operationData) {
        if (operationData.isRipetizione() || operationData.isRata()) {
            return recurrenceService.calculateRecurrenceDates(
                    operationData, add_data.getValue()
            );
        }
        List<String> dates = new ArrayList<>();
        dates.add(add_data.getValue().toString());
        return dates;
    }

    private List<Tags> resolveTags(List<String> selectedTags) {
        List<Tags> allTags = tagService.getAllTags();
        List<Tags> resolvedTags = new ArrayList<>();

        for (String selectedTag : selectedTags) {
            allTags.stream()
                    .filter(tag -> tag.description().equalsIgnoreCase(selectedTag))
                    .findFirst()
                    .ifPresent(resolvedTags::add);
        }

        return resolvedTags;
    }

    private String getNextOperationId() {
        return String.valueOf(Integer.parseInt(maxId) + 1);
    }

    @FXML
    public void cleanAll() {
        FormCleaner.cleanAll(
                add_imp, add_desc, add_data, add_autore,
                add_entrata, add_out, add_rep, add_rata,
                add_freq, add_occ, add_tags, main_tag, add_sottocat
        );
        updateFrequencyVisibility();
    }

    public void refreshRecentOperationsList() {
        operations = operationService.getAllOperations();
        ObservableList<OperationRow> recentOperations =
                operationService.getRecentOperations(10);

        add_recenti.setItems(recentOperations);
        updateMaxId();
    }

    private void updateMaxId() {
        maxId = operationService.getMaxOperationId(operations);
    }

    @FXML
    public void addTag() {
        String tagToAdd = determineTagToAdd();

        if (!validationService.validateTagAddition(tagToAdd, add_tags.getItems())) {
            return;
        }

        add_tags.getItems().add(tagToAdd);
    }

    private String determineTagToAdd() {
        String sottocategoria = add_sottocat.getValue();
        if (sottocategoria != null && !sottocategoria.isBlank()) {
            return sottocategoria;
        }

        Toggle selectedToggle = main_tag.getSelectedToggle();
        if (selectedToggle != null) {
            return ((ToggleButton) selectedToggle).getText();
        }

        return null;
    }

    @FXML
    public void delTag() {
        String selected = add_tags.getSelectionModel().getSelectedItem();
        if (selected != null) {
            add_tags.getItems().remove(selected);
        }
    }

    private void updateFrequencyVisibility() {
        boolean shouldShow = add_rep.isSelected() || add_rata.isSelected();
        add_freq.setVisible(shouldShow);
        add_occ.setVisible(shouldShow);
    }

    @FXML
    public void loadSottocat(String category) {
        List<String> subcategories = tagService.getSubcategoriesByCategory(category);
        add_sottocat.getItems().setAll(subcategories);
    }

    private void loadAuthors() {
        List<String> authors = authorService.getAllAuthorNames();
        add_autore.getItems().addAll(authors);
    }

    private void showValidationError() {
        showError("Tutti i campi obbligatori devono essere compilati correttamente.");
    }

    private void showSuccessMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText("Dati inseriti con successo.");
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}