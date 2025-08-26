package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Currency;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Properties;

public class ImpoController {
    @FXML
    private Button impo_reset;
    @FXML
    private Button impo_cancella;
    @FXML
    private Button impo_mod;
    @FXML
    private Button impo_cambioval;
    @FXML
    private ComboBox impo_valuta;

    private boolean darkMode; // false = chiaro, true = scuro
    private String valuta;
    List<Currency> codes;
    OperationXmlRepository repo = new OperationXmlRepository();

/**

 */
    public void initialize() {
        loadSettings();
        codes = loadCurrencyCodes();
        for (Currency c : codes) {
            impo_valuta.getItems().add(c.code());
        }


        try {
            impo_reset.setOnAction(event -> resetFields());
            impo_cancella.setOnAction(event -> cancelData());
            impo_mod.setOnAction(event -> modifySettings(false));
            impo_cambioval.setOnAction(event -> changeValue(false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**

     */
    private void changeValue(boolean reset) {
        String selectedCurrency = impo_valuta.getValue() != null
                ? impo_valuta.getValue().toString()
                : "EUR";
        List<Operation> scrittura;

        if (reset) {
            impo_valuta.setValue("EUR");
            scrittura = transactionsToEuro();
            repo.save(scrittura);
            valuta = "EUR";
            saveSettings();
            return;
        }

        if (selectedCurrency.equals(valuta)) {
            saveSettings();
            return;
        }

        scrittura = transactionsToEuro();

        if ("EUR".equals(selectedCurrency)) {
            repo.save(scrittura);
            valuta = "EUR";

        } else {

            double fromEuro = 0;
            for (Currency c : codes) {
                if (c.code().equals(selectedCurrency)) {
                    fromEuro = c.fromEuro();
                    break;
                }
            }
            List<Operation> converted = new ArrayList<>();
            for (Operation op : scrittura) {
                double amount = Math.round((op.getAmount() * fromEuro) * 100.0) / 100.0;
                converted.add(new Operation(op.getId(), op.getAutore(), op.getDesc(), amount, op.getDate(), op.getTags()));
            }
            repo.save(converted);
            valuta = selectedCurrency;
        }


        impo_valuta.setValue(valuta);
        saveSettings();
    }

    /**

     */
    public List<Operation> transactionsToEuro() {
        List<Operation> operations = repo.read();

        double toEuro = 0;
        for (Currency c : codes) {
            if (c.code().equals(valuta)) {
                toEuro = c.toEuro();
                break;
            }
        }
        List<Operation> converted = new ArrayList<>();
        for (Operation op : operations) {
            double euroAmount = Math.round((op.getAmount() * toEuro) * 100.0) / 100.0;
            converted.add(new Operation(op.getId(), op.getAutore(), op.getDesc(), euroAmount, op.getDate(), op.getTags()));

        }

        return converted;
    }

    /**

     */
    private void cancelData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma cancellazione");
        alert.setHeaderText(null);
        alert.setContentText("Sei sicuro di voler cancellare?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            repo.delete(true);
        }
    }

    /**

     */
    private void modifySettings(boolean reset) {
        Scene scene = impo_mod.getScene();

        if (darkMode || reset) {
            // Passa al tema chiaro
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/tema_chiaro.css").toExternalForm());
            darkMode = false;
        } else {
            // Passa al tema scuro
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/tema_scuro.css").toExternalForm());
            darkMode = true;
        }
        saveSettings();
    }

    /**

     */
    private List<Currency> loadCurrencyCodes() {
        List<Currency> currencies = new ArrayList<>();
        try (FileInputStream in = new FileInputStream("app/data/currency.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            NodeList list = doc.getElementsByTagName("currency");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String code = el.getAttribute("code");
                double toEuro = Double.parseDouble(el.getElementsByTagName("to_EUR").item(0).getTextContent());
                double fromEuro = Double.parseDouble(el.getElementsByTagName("from_EUR").item(0).getTextContent());
                currencies.add(new Currency(code, toEuro, fromEuro));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currencies;
    }

    /**

     */
    private File getSettingsFile() {
        File configDir = new File("app/config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "settings.properties");
    }

    /**

     */
    private void saveSettings() {
        Properties props = new Properties();
        props.setProperty("currency", impo_valuta.getValue() != null ? impo_valuta.getValue().toString() : "EUR");
        props.setProperty("darkMode", Boolean.toString(darkMode));
        try (FileOutputStream out = new FileOutputStream(getSettingsFile())) {
            props.store(out, "Impostazioni utente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**

     */
    private void loadSettings() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(getSettingsFile())) {
            props.load(in);
            String currency = props.getProperty("currency", "EUR");
            impo_valuta.setValue(currency);
            valuta = currency;
            darkMode = Boolean.parseBoolean(props.getProperty("darkMode", "false"));
        } catch (Exception e) {
            // File non trovato: usa i default
            darkMode = false;
            valuta = "EUR";
        }
        Platform.runLater(this::applyTheme);

    }

    /**

     */
    private void applyTheme() {
        Scene scene = impo_mod.getScene();
        if (scene == null) return; // Evita NullPointerException
        scene.getStylesheets().clear();
        if (darkMode) {
            scene.getStylesheets().add(getClass().getResource("/css/tema_scuro.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/css/tema_chiaro.css").toExternalForm());
        }
    }

    /**

     */
    private void resetFields() {
        modifySettings(true);
        changeValue(true);
    }
}