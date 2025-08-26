    package it.unicam.cs.mpgc.jbudget125637.controller;

    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.control.*;

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
                tabAggiungi.setContent(FXMLLoader.load(getClass().getResource("/grafica/Family_budget_add.fxml")));
                tabRevisione.setContent(FXMLLoader.load(getClass().getResource("/grafica/Family_budget_rev.fxml")));
                tabConfronto.setContent(FXMLLoader.load(getClass().getResource("/grafica/Family_budget_confronto.fxml")));
                tabImpostazioni.setContent(FXMLLoader.load(getClass().getResource("/grafica/Family_budget_impostazioni.fxml")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


