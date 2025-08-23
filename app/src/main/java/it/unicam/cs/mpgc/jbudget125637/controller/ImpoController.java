package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Currency;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
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


    /**
     * Inizializza la schermata delle impostazioni.
     * - Carica le impostazioni salvate.
     * - Carica i codici valuta da {@code currency.xml}.
     * - Popola la ComboBox con i codici valuta disponibili.
     * - Imposta gli handler per i pulsanti (reset, cancella, modifica tema, cambio valuta).
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
     * Converte tutte le transazioni in una valuta selezionata.
     * - Se {@code reset = true}, riporta tutte le transazioni in Euro.
     * - Altrimenti converte da Euro alla valuta selezionata nella ComboBox.
     * - Salva il risultato in {@code operations.xml}.
     *
     * @param reset se {@code true}, riporta sempre le transazioni in Euro.
     */
    private void changeValue(boolean reset) {
        String selectedCurrency = impo_valuta.getValue().toString();
        List<Operation> scrittura;

        if (reset) {
             scrittura = transactionsToEuro();
            saveOperationsToXml(scrittura, "app/data/operations.xml");
            impo_valuta.setValue("EUR");
        }
        else {
            if( impo_valuta.getValue().equals(valuta)) {
                return;
            }
            scrittura = transactionsToEuro();
            if (impo_valuta.getValue().equals("EUR")) {
                saveOperationsToXml(scrittura, "app/data/operations.xml");
                return;
            } else {
                double fromEuro = 0;
                for ( Currency c : codes) {
                    if (c.code().equals(impo_valuta.getValue().toString())) {
                        fromEuro = c.fromEuro();
                        break;
                    }
                }
                List<Operation> converted = new ArrayList<>();
                for (Operation op : scrittura) {
                    double euroAmount = Math.round((op.getAmount() * fromEuro) * 100.0) / 100.0;
                    converted.add(new Operation(op.getId(), op.getAutore(), op.getDesc(), euroAmount, op.getDate(), op.getTags()));
                }
                //leggi i valori delle transazioni da operations.xml
                //leggi il valore dell'attributo FROMEUR di currency.xml per la valuta salvata nella variabile selectedCurrency
                //converti tutti i valori da euro in selectedCurrency
                //salva tutte le transazioni in operations.xml
                saveOperationsToXml(converted, "app/data/operations.xml");

            }
        }
        saveSettings();

    }

    /**
     * Converte tutte le transazioni correnti in Euro.
     * - Legge i dati da {@code operations.xml}.
     * - Usa il tasso {@code to_EUR} della valuta attuale.
     *
     * @return lista di transazioni convertite in Euro.
     */
    public List<Operation> transactionsToEuro() {
        //fai tornare tutte le transazioni ad euro
        // leggi tutte le transazioni da operations.xml
        List<Operation> operations = loadOperations();
        double toEuro = 0;
        for ( Currency c : codes) {
            if (c.code().equals(valuta)) {
                toEuro = c.toEuro();
                break;
            }
        }
        List<Operation> converted = new ArrayList<>();
        for (Operation op : operations) {
            double euroAmount = Math.round((op.getAmount() * toEuro) * 100.0) / 100.0;
                converted.add(new Operation(op.getId(),op.getAutore(), op.getDesc(), euroAmount, op.getDate(), op.getTags()));

        }
        // leggi il valore dell'attributo TOEUR di currency.xml per la valuta salvata nella variabile valuta
        // converti tutti i valori da selectedCurrency in euro
        // salva le transazioni convertite in operations.
        return converted;
    }

    /**
     * Salva una lista di operazioni in un file XML.
     *
     * @param operations lista di operazioni da salvare.
     * @param filePath percorso del file XML di destinazione.
     */
    public void saveOperationsToXml(List<Operation> operations, String filePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            Element root = doc.createElement("operations");
            doc.appendChild(root);

            for (Operation op : operations) {
                Element opElem = doc.createElement("operation");
                opElem.setAttribute("id", op.getId());
                opElem.setAttribute("desc", op.getDesc());
                opElem.setAttribute("amount", String.valueOf(op.getAmount()));
                opElem.setAttribute("date", op.getDate());
                root.appendChild(opElem);
            }

            // Scrittura su file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            FileOutputStream fos = new FileOutputStream(filePath);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Carica le operazioni dal file {@code operations.xml}.
     *
     * @return lista di operazioni presenti nel file XML.
     */
    public List<Operation> loadOperations() {
        List<Operation> operations = new ArrayList<>();
        try (FileInputStream in = new FileInputStream("app/data/operations.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            NodeList list = doc.getElementsByTagName("operation");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String id = el.getElementsByTagName("id").item(0).getTextContent();
                String author = el.getElementsByTagName("author").item(0).getTextContent();
                String description = el.getElementsByTagName("description").item(0).getTextContent();
                String date = el.getElementsByTagName("date").item(0).getTextContent();
                String amountStr = el.getElementsByTagName("amount").item(0).getTextContent();
                double amount = amountStr.isEmpty() ? 0.0 : Double.parseDouble(amountStr);
                List<String> tags = new ArrayList<>();
                NodeList readTags = el.getElementsByTagName("tags");
                for (int k = 0; k < readTags.getLength(); k++) {
                    Element tag = (Element) readTags.item(k);
                        NodeList children = tag.getElementsByTagName("tag");
                        for (int j = 0; j < children.getLength(); j++) {
                            Element sub = (Element) children.item(j);
                            tags.add(sub.getTextContent());
                        }
                        break;
                }
                operations.add(new Operation(id, author, description, amount, date,tags));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return operations;
    }

    /**
     * Cancella i dati delle operazioni.
     * - Mostra una finestra di conferma.
     * - Se confermato, sposta il file {@code operations.xml} in {@code operations_backup.xml}.
     * - Crea un nuovo file {@code operations.xml} vuoto e valido.
     */
    private void cancelData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma cancellazione");
        alert.setHeaderText(null);
        alert.setContentText("Sei sicuro di voler cancellare?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                File file = new File("app/data/operations.xml");
                System.out.println("Percorso atteso: " + file.getAbsolutePath());
                File backup = new File("app/data/operations_backup.xml");
                System.out.println("File originale: " + file.getAbsolutePath() + " - exists: " + file.exists());
                System.out.println("File backup: " + backup.getAbsolutePath() + " - exists: " + backup.exists());

                if (file.exists()) {
                    // Se esiste già un backup, lo elimina
                    System.out.println("Backup già esistente, lo elimino.");

                    if (backup.exists()) backup.delete();
                    // Rinomina il file originale in backup
                    if (!file.renameTo(backup)) {
                        System.out.println("Impossibile rinominare il file per il backup.");

                        throw new Exception("Impossibile rinominare il file per il backup.");
                    }
                    else {
                        System.out.println("File rinominato correttamente.");
                    }
                }else {
                    System.out.println("Il file originale non esiste.");
                }
                // Crea un nuovo file vuoto valido
                String emptyXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><operations></operations>";
                try (FileOutputStream out = new FileOutputStream(file, false)) {
                    out.write(emptyXml.getBytes("UTF-8"));
                    System.out.println("Nuovo file vuoto creato.");

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Modifica il tema dell’applicazione (chiaro/scuro).
     * - Se {@code reset = true}, imposta sempre il tema chiaro.
     * - Altrimenti alterna tra tema chiaro e scuro.
     *
     * @param reset se {@code true}, forza il tema chiaro.
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
     * Carica i codici delle valute dal file {@code currency.xml}.
     *
     * @return lista di oggetti {@link Currency} con codice, toEuro e fromEuro.
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
     * Restituisce il file delle impostazioni utente ({@code settings.properties}).
     * Se la cartella {@code app/config} non esiste, viene creata.
     *
     * @return file delle impostazioni.
     */
    private File getSettingsFile() {
        File configDir = new File("app/config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "settings.properties");
    }

    /**
     * Salva le impostazioni utente correnti:
     * - Valuta selezionata.
     * - Tema attivo (chiaro/scuro).
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
     * Carica le impostazioni utente da {@code settings.properties}.
     * Se il file non esiste, imposta valori di default:
     * - Valuta = EUR
     * - Tema = chiaro
     *
     * Al termine applica il tema corrente alla scena.
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
     * Applica il tema corrente alla scena (chiaro o scuro).
     * Se la scena non è disponibile, non fa nulla.
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
     * Reimposta i campi delle impostazioni:
     * - Riporta il tema a chiaro.
     * - Riporta tutte le transazioni in Euro.
     */
    private void resetFields() {
        modifySettings(true);
        changeValue(true);
    }
}