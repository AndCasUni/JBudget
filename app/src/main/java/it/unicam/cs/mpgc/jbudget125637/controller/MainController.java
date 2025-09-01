package it.unicam.cs.mpgc.jbudget125637.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

public class MainController {

    @FXML
    private TabPane tabPane;
    @FXML private Tab tabAggiungi;
    @FXML private Tab tabRevisione;
    @FXML private Tab tabConfronto;
    @FXML private Tab tabImpostazioni;

    @FXML
    public void initialize() {
        try {
            loadTabContent(tabAggiungi, "/grafica/Family_budget_add.fxml");
            loadTabContent(tabRevisione, "/grafica/Family_budget_rev.fxml");
            loadTabContent(tabConfronto, "/grafica/Family_budget_confronto.fxml");
            loadTabContent(tabImpostazioni, "/grafica/Family_budget_impostazioni.fxml");
        } catch (Exception e) {
            showErrorAlert("Errore di caricamento", "Impossibile caricare le interfacce: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTabContent(Tab tab, String fxmlPath) {
        try {
            tab.setContent(FXMLLoader.load(getClass().getResource(fxmlPath)));
        } catch (Exception e) {
            tab.setContent(new Label("Errore nel caricamento: " + fxmlPath));
            System.err.println("Errore nel caricamento di " + fxmlPath + ": " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}