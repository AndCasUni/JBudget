package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
import it.unicam.cs.mpgc.jbudget125637.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    private final OperationFilterService filterService;
    private final AuthorService authorService;
    private final ObservableList<CompleteOperationRow> data = FXCollections.observableArrayList();

    public RevisionController() {
        TagService tagService = new TagService();
        this.filterService = new OperationFilterService(tagService);
        this.authorService = new AuthorService();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAuthors();
        loadOperations();
        rev_lista.setItems(null);
    }

    private void setupTableColumns() {
        rev_colautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        rev_coldesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        rev_coldata.setCellValueFactory(new PropertyValueFactory<>("date"));
        rev_colimpo.setCellValueFactory(new PropertyValueFactory<>("amount"));
        rev_coltag1.setCellValueFactory(new PropertyValueFactory<>("tag1"));
        rev_coltag2.setCellValueFactory(new PropertyValueFactory<>("tag2"));
        rev_coltag3.setCellValueFactory(new PropertyValueFactory<>("tag3"));
    }

    @FXML
    public void cerca() {
        FilterCriteria criteria = buildFilterCriteria();
        ObservableList<CompleteOperationRow> filteredData = filterService.applyFilters(data, criteria);
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

    private List<String> getSelectedTags() {
        List<String> selectedTags = new ArrayList<>();
        Map<ToggleButton, String> tagButtons = createTagButtonMap();

        tagButtons.forEach((button, tag) -> {
            if (button.isSelected()) selectedTags.add(tag);
        });

        return selectedTags;
    }

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

    private void loadOperations() {
        List<CompleteOperationRow> operations = filterService.loadAllOperations();
        data.clear();
        data.addAll(operations);
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

        rev_lista.setItems(data);
    }
}