package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Currency;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.OperationRow;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.persistency.SettingsManager;
import it.unicam.cs.mpgc.jbudget125637.service.OperationService;
import it.unicam.cs.mpgc.jbudget125637.service.RefreshService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class ImpoController {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final String LIGHT_THEME_CSS = "/css/tema_chiaro.css";
    private static final String DARK_THEME_CSS = "/css/tema_scuro.css";

    @FXML
    private Button impo_reset;
    @FXML
    private Button impo_cancella;
    @FXML
    private Button impo_mod;
    @FXML
    private Button impo_cambioval;
    @FXML
    private ComboBox<String> impo_valuta;

    private final OperationXmlRepository operationRepository = new OperationXmlRepository();
    private final SettingsManager settingsManager = new SettingsManager();
    private List<Currency> availableCurrencies;
    private String currentCurrency;
    private boolean darkMode;

    @FXML
    public void initialize() {
        loadSettings();
        loadAvailableCurrencies();
        initializeCurrencyComboBox();
        setupEventHandlers();

        Platform.runLater(this::applyCurrentTheme);
    }

    private void loadSettings() {
        currentCurrency = settingsManager.getCurrency();
        if (currentCurrency == null || currentCurrency.isEmpty()) {
            currentCurrency = DEFAULT_CURRENCY;
        }

        darkMode = settingsManager.isDarkMode();
    }

    private void loadAvailableCurrencies() {
        availableCurrencies = settingsManager.loadCurrencyCodes();
    }

    private void initializeCurrencyComboBox() {
        availableCurrencies.forEach(currency -> impo_valuta.getItems().add(currency.code()));
        impo_valuta.setValue(currentCurrency);
    }

    private void setupEventHandlers() {
        impo_reset.setOnAction(event -> resetToDefaults());
        impo_cancella.setOnAction(event -> deleteAllDataWithConfirmation());
        impo_mod.setOnAction(event -> toggleTheme());
        impo_cambioval.setOnAction(event -> convertCurrencyToSelected());
    }


    /**
     * converte tutte le operazioni nella valuta selezionata e aggiorna l'impostazione corrente.
     * Se la valuta selezionata è già quella corrente, mostra un messaggio informativo.
     * In caso di errore durante la conversione, mostra un messaggio di errore e ripristina la selezione precedente.
     */
    private void convertCurrencyToSelected() {
        String selectedCurrency = impo_valuta.getValue();

        if (selectedCurrency.equals(currentCurrency)) {
            showInformationDialog("Informazione", "La valuta selezionata è già quella corrente.");
            return;
        }

        try {
            List<Operation> operations = operationRepository.read();
            List<Operation> convertedOperations = convertOperations(operations, currentCurrency, selectedCurrency);

            operationRepository.save(convertedOperations);

            showInformationDialog("Operazione completata",
                    String.format("Valuta cambiata da %s a %s",
                            currentCurrency, selectedCurrency));

            currentCurrency = selectedCurrency;
            settingsManager.saveCurrency(currentCurrency);
            refreshAddTab();

        } catch (Exception e) {
            showErrorDialog("Errore durante la conversione della valuta");
            impo_valuta.setValue(currentCurrency);
        }
    }

    /**
     *  Aggiorna le tab "Aggiungi" e "Confronto" per riflettere le modifiche apportate ai dati.
     * */
    private void refreshAddTab() {
        RefreshService.refreshAddTab();
        RefreshService.refreshConfrontoTab();
    }

    /**
     * Converte una lista di operazioni da una valuta a un'altra utilizzando il tasso di conversione.
     *
     * @param operations   La lista di operazioni da convertire.
     * @param fromCurrency La valuta di origine.
     * @param toCurrency   La valuta di destinazione.
     * @return Una nuova lista di operazioni con gli importi convertiti.
     */
    private List<Operation> convertOperations(List<Operation> operations, String fromCurrency, String toCurrency) {
        double conversionRate = getConversionRate(fromCurrency, toCurrency);

        return operations.stream()
                .map(op -> convertOperation(op, conversionRate))
                .toList();
    }

    /**
     * Calcola il tasso di conversione tra due valute.
     * Se le valute sono uguali, il tasso di conversione è 1.0.
     * Altrimenti, il tasso viene calcolato utilizzando i tassi di conversione verso e da Euro.
     *
     * @param fromCurrency La valuta di origine.
     * @param toCurrency   La valuta di destinazione.
     * @return Il tasso di conversione tra le due valute.
     */
    private double getConversionRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        double toEuroRate = findCurrency(fromCurrency).map(Currency::toEuro).orElse(1.0);
        double fromEuroRate = findCurrency(toCurrency).map(Currency::fromEuro).orElse(1.0);

        return toEuroRate * fromEuroRate;
    }

    /**
     * Converte un'operazione in una nuova operazione con l'importo convertito.
     *
     * @param operation      L'operazione da convertire.
     * @param conversionRate Il tasso di conversione da applicare all'importo.
     * @return Una nuova operazione con l'importo convertito.
     */
    private Operation convertOperation(Operation operation, double conversionRate) {
        double convertedAmount = Math.round(operation.getAmount() * conversionRate * 100.0) / 100.0;

        return new Operation(
                operation.getId(),
                operation.getAutore(),
                operation.getDesc(),
                convertedAmount,
                operation.getDate(),
                operation.getTags()
        );
    }

    private Optional<Currency> findCurrency(String currencyCode) {
        return availableCurrencies.stream()
                .filter(currency -> currency.code().equals(currencyCode))
                .findFirst();
    }

    /**
     * Mostra una finestra di conferma prima di cancellare tutti i dati.
     * Se l'utente conferma, chiama il metodo deleteAllData() per eseguire la cancellazione.
     */
    private void deleteAllDataWithConfirmation() {
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Conferma cancellazione");
        confirmationDialog.setHeaderText(null);
        confirmationDialog.setContentText("Sei sicuro di voler cancellare tutti i dati?");

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteAllData();
            RefreshService.refreshAll();
        }
    }

    private void deleteAllData() {
        try {
            operationRepository.delete(true);
            showInformationDialog("Operazione completata",
                    "Tutti i dati sono stati cancellati.");
        } catch (Exception e) {
            showErrorDialog("Errore durante la cancellazione dei dati");
        }
    }

    /**
     * Inverte il tema attuale tra chiaro e scuro, applica il nuovo tema alla scena
     * e salva la preferenza utilizzando SettingsManager. Mostra una finestra di
     * informazione per confermare il cambiamento.
     */
    private void toggleTheme() {
        darkMode = !darkMode;
        applyCurrentTheme();
        settingsManager.saveDarkMode(darkMode);

        String themeName = darkMode ? "scura" : "chiara";
        showInformationDialog("Tema cambiato",
                String.format("Tema impostato su modalità %s.", themeName));
    }

    /**
     * Applica il tema corrente (chiaro o scuro) alla scena principale
     * rimuovendo eventuali fogli di stile esistenti e aggiungendo il
     * foglio di stile appropriato in base alla modalità attuale.
     * Gestisce eventuali errori di caricamento del file CSS.
     */
    private void applyCurrentTheme() {
        Scene scene = impo_mod.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            String themeCss = darkMode ? DARK_THEME_CSS : LIGHT_THEME_CSS;

            try {
                URL cssUrl = getClass().getResource(themeCss);
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());

                } else {
                    System.err.println("File CSS non trovato: " + themeCss);
                }
            } catch (Exception e) {
                System.err.println("Errore nel caricamento CSS: " + e.getMessage());
            }
        }
    }

    private void resetToDefaults() {
        resetThemeWithConfirmation();
        resetCurrencyWithConfirmation();
        refreshAddTab();
    }

    private void resetThemeWithConfirmation() {
        if (darkMode) {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Conferma reset tema");
            confirmationDialog.setHeaderText(null);
            confirmationDialog.setContentText("Ripristinare il tema chiaro?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                resetTheme();
            }
        }
    }

    private void resetCurrencyWithConfirmation() {
        if (!DEFAULT_CURRENCY.equals(currentCurrency)) {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Conferma reset valuta");
            confirmationDialog.setHeaderText(null);
            confirmationDialog.setContentText(String.format(
                    "Ripristinare la valuta predefinita (%s) e convertire tutte le transazioni?",
                    DEFAULT_CURRENCY
            ));

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                resetCurrency();
            } else {
                impo_valuta.setValue(currentCurrency);
            }
        }
    }

    private void resetTheme() {
        darkMode = false;
        applyCurrentTheme();
        settingsManager.saveDarkMode(false);
        showInformationDialog("Tema reimpostato", "Tema ripristinato alla modalità chiara.");
    }

    /**
     * Reimposta la valuta predefinita convertendo tutte le operazioni esistenti
     * nella valuta predefinita. Aggiorna l'impostazione corrente e salva la nuova
     * valuta utilizzando SettingsManager. Mostra una finestra di informazione per
     * confermare il cambiamento o un messaggio di errore in caso di fallimento.
     */
    private void resetCurrency() {
        try {
            List<Operation> operations = operationRepository.read();
            List<Operation> convertedOperations = convertOperations(operations, currentCurrency, DEFAULT_CURRENCY);

            operationRepository.save(convertedOperations);
            currentCurrency = DEFAULT_CURRENCY;
            impo_valuta.setValue(DEFAULT_CURRENCY);
            settingsManager.saveCurrency(DEFAULT_CURRENCY);

            showInformationDialog("Valuta reimpostata",
                    String.format("Tutte le transazioni convertite in %s.", DEFAULT_CURRENCY));

        } catch (Exception e) {
            showErrorDialog("Errore durante il ripristino della valuta");
            impo_valuta.setValue(currentCurrency);
        }
    }

    private void showInformationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}