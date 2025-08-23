package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class AddController {

    @FXML
    private TextField add_imp;
    @FXML
    private TextField add_desc;
    @FXML
    private DatePicker add_data;
    @FXML
    private ComboBox add_autore;
    @FXML
    private RadioButton add_entrata;
    @FXML
    private RadioButton add_out;
    @FXML
    private CheckBox add_rep;
    @FXML
    private CheckBox add_rata;
    @FXML
    private ChoiceBox add_freq;
    @FXML
    private TextField add_occ;
    @FXML
    private TableView<OperationRow> add_recenti;
    @FXML
    private TableColumn<OperationRow, String> add_recautore;
    @FXML
    private TableColumn<OperationRow, String> add_recdata;
    @FXML
    private TableColumn<OperationRow, Double> add_recimporto;
    @FXML
    private ToggleButton add_cibo;
    @FXML
    private ToggleButton add_casa;
    @FXML
    private ToggleButton add_lavoro;
    @FXML
    private ToggleButton add_sport;
    @FXML
    private ToggleButton add_hobby;
    @FXML
    private ToggleButton add_salute;
    @FXML
    private ComboBox add_sottocat;
    @FXML
    private ListView add_tags;
    @FXML
    private Button add_addtag;
    @FXML
    private Button add_deltag;
    @FXML
    private Button add_insert;
    @FXML
    private ToggleGroup main_tag;

    String lastId;

    /**
     * Inizializza la vista e i componenti della form.
     * - Configura le colonne della tabella delle operazioni recenti.
     * - Carica frequenze, sottocategorie e autori.
     * - Imposta i listener per la selezione di categorie e checkBox (ricorrenza/rata).
     */
    public void initialize() {
        add_recautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        add_recdata.setCellValueFactory(new PropertyValueFactory<>("date"));
        add_recimporto.setCellValueFactory(new PropertyValueFactory<>("amount"));
        //caricare ripetizioni ( mensile 31 , settimanale 7, annuale 365 , giornaliera 1 )
        add_freq.setItems(FXCollections.observableArrayList("Giornaliera", "Settimanale", "Mensile", "Annuale"));

        loadSottocat(null);
        //add_sottocat se nessuno toggle button è selezionato contiene tutte le sottocategorie altrimenti solo le sottocategorie della categoria( togglebutton) selezionata

        main_tag.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                loadSottocat(((ToggleButton) newToggle).getText());
            } else {
                loadSottocat(null);
            }
        });

        add_freq.setVisible(false);
        add_occ.setVisible(false);
        // leggere autori dal file
        loadAutore();

        //caricare recenti nella lista add_recenti
        loadRecenti();

        add_rep.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                add_rata.setSelected(false);
            }
        });

        // Quando checkBox2 viene selezionata, deseleziona checkBox1
        add_rata.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                add_rep.setSelected(false);
            }
        });
    }

    ;

    /**
     * Inserisce una nuova operazione leggendo i dati dai campi della form.
     * Esegue i seguenti controlli:
     * - Tutti i campi obbligatori devono essere compilati.
     * - L'importo deve essere un numero valido.
     * - Se è un'uscita, l'importo viene reso negativo.
     * - Se si tratta di rata o ricorrenza, genera più inserimenti con date calcolate.
     *
     * In caso di errore, mostra un alert con il messaggio opportuno.
     * In caso di successo, inserisce i dati in XML, mostra conferma e ricarica la lista recenti.
     */
    public void addInsert() {

        //controllare che tutti i campi siano popolati
        int numInsert = 1; // Numero di inserimenti, da incrementare se necessario
        String importo = add_imp.getText();
        String descrizione = add_desc.getText();
        String data = add_data.getValue() != null ? add_data.getValue().toString() : null;
        String autore = (String) add_autore.getValue();
        boolean entrata = add_entrata.isSelected();
        boolean uscita = add_out.isSelected();
        boolean ripetizione = add_rep.isSelected();
        boolean rata = add_rata.isSelected();
        String frequenza = (String) add_freq.getValue();
        String occorrenze = add_occ.getText();
        List<String> tags = add_tags.getItems();
        List<String> dateRicorrenze = new ArrayList<>();
        dateRicorrenze.add(data);
        //autore non null
        if (importo == null || importo.isBlank() || descrizione == null || descrizione.isBlank() ||
                data == null || data.isBlank() || autore == null || autore.isBlank() ||
                (!entrata && !uscita) || (ripetizione && (frequenza == null || frequenza.isBlank())) ||
                (ripetizione && rata && (occorrenze == null || occorrenze.isBlank())) ||
                tags.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("Tutti i campi devono essere compilati.");
            alert.showAndWait();
            return;
        }
        //importo non null
        //controllare che uno tra in e out sia selezionato
        //se out l'importo va inserito negativo
        double importoValue;
        try {
            importoValue = Double.parseDouble(importo);
            if (uscita) {
                importoValue = -importoValue; // Se è un'uscita, l'importo deve essere negativo
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("L'importo deve essere un numero valido.");
            alert.showAndWait();
            return;
        }


        if (ripetizione || rata) {
            // Logica per gestire le ricorrenze
            int occorrenzeValue;
            try {
                occorrenzeValue = Integer.parseInt(occorrenze);
                if (occorrenzeValue <= 0) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Errore");
                    alert.setHeaderText(null);
                    alert.setContentText("Il numero di occorrenze deve essere maggiore di zero.");
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText("Il numero di occorrenze deve essere un numero valido.");
                alert.showAndWait();
                return;
            }

            int intervallo;
            // Logica per gestire la frequenza delle ricorrenze
            if (add_freq.getValue().equals("Giornaliera")) {
                intervallo = 1;
            } else if (add_freq.getValue().equals("Settimanale")) {
                intervallo = 7;
            } else if (add_freq.getValue().equals("Mensile")) {
                intervallo = 31;
            } else if (add_freq.getValue().equals("Annuale")) {
                intervallo = 365;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText("Frequenza non valida.");
                alert.showAndWait();
                return;
            }
            // Aggiungi qui la logica per gestire le ricorrenze in base alla frequenza selezionata
            //calcolo delle date per le ricorrenze
            //le inserisco in un array di stringhe
            for (int i = 0; i < occorrenzeValue; i++) {
                // Calcolo la data per ogni ricorrenza
                String ricorrenzaData = add_data.getValue().plusDays((i + 1) * intervallo).toString();
                dateRicorrenze.add(ricorrenzaData);

            }
            numInsert = occorrenzeValue;
        }

        for (int i = 0; i < numInsert; i++) {
            Operation newElement = null;
            String newId = lastId + 1; // Genera un nuovo ID univoco per l'operazione
            // Creazione dell'elemento da inserire
            if (rata) {
                newElement = new Operation(
                        newId,
                        autore,// ID univoco, da generare in modo appropriato
                        descrizione,
                        importoValue / numInsert,
                        dateRicorrenze.get(i),
                        tags
                );
            } else {
                newElement = new Operation(
                        newId,
                        autore,// ID univoco, da generare in modo appropriato
                        descrizione,
                        importoValue,
                        dateRicorrenze.get(i),
                        tags
                );
            }

            // Aggiunta dell'elemento al file XML
            try {
                newElement.addElementToXML();
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore");
                alert.setHeaderText(null);
                alert.setContentText("Errore durante l'inserimento dei dati. " + newElement.toString());
                alert.showAndWait();
                return;
            }
        }


        // Mostrare un messaggio di successo
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText("Dati inseriti con successo.");
        alert.showAndWait();
        cleanAll();
        loadRecenti();
    }
    /**
     * Pulisce tutti i campi della form e ripristina lo stato iniziale.
     */
    public void cleanAll() {
        // Logica per pulire tutti i campi
        add_imp.clear();
        add_desc.clear();
        add_data.setValue(null);
        add_autore.getSelectionModel().clearSelection();
        add_entrata.setSelected(false);
        add_out.setSelected(false);
        add_rep.setSelected(false);
        add_rata.setSelected(false);
        add_freq.getSelectionModel().clearSelection();
        add_occ.clear();
        add_tags.getItems().clear();
        main_tag.selectToggle(null); // Deseleziona tutti i ToggleButton
        add_sottocat.getSelectionModel().clearSelection();
    }

    /**
     * Carica le operazioni recenti dal file XML e le mostra nella tabella {@code add_recenti}.
     * Aggiorna anche l'ultimo ID registrato per le nuove operazioni.
     */
    public void loadRecenti() {
        // Logic to handle recent entries

        try {
            File xmlFile = new File("app/data/operations.xml");
            if (!xmlFile.exists()) return;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("operation");

            ObservableList<OperationRow> data = FXCollections.observableArrayList();

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    String author = elem.getElementsByTagName("author").item(0).getTextContent();
                    String date = elem.getElementsByTagName("date").item(0).getTextContent();
                    Double amount = Double.parseDouble(elem.getElementsByTagName("amount").item(0).getTextContent());
                    lastId = elem.getAttribute("id"); // Aggiorna l'ultimo ID
                    data.add(new OperationRow(author, date, amount));
                }
            }

            add_recenti.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Aggiunge un tag alla lista dei tags.
     * - Se una sottocategoria è selezionata, viene utilizzata come tag.
     * - Altrimenti, viene utilizzata la categoria (ToggleButton) selezionata.
     *
     * Se il tag è già presente o se si superano i 3 tag, viene mostrato un messaggio di errore.
     */
    public void addTag() {
        List<String> tags = add_tags.getItems();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        // Trovo il ToggleButton selezionato dal gruppo main_tag
        Toggle selectedToggle = main_tag.getSelectedToggle();
        String categoria = (selectedToggle != null) ? ((ToggleButton) selectedToggle).getText() : null;
        String tagToAdd = null;
// Sottocategoria selezionata dalla comboBox
        String sottocategoria = (String) add_sottocat.getValue();
        if (sottocategoria != null && !sottocategoria.isBlank()) {
            // Priorità alla sottocategoria
            tagToAdd = sottocategoria;
        } else if (categoria != null) {
            tagToAdd = categoria;
        }


        if (tags.contains(tagToAdd)) {

            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("Tag già presente.");
            alert.showAndWait();
            return;
        }

        if (tags.size() >= 3) {
            alert.setTitle("Errore");
            alert.setHeaderText(null);
            alert.setContentText("Puoi aggiungere al massimo 3 tag.");
            alert.showAndWait();
            return;
        }

        tags.add(tagToAdd);
    }

    /**
     * Elimina il tag selezionato dalla lista {@code add_tags}, se presente.
     */
    public void delTag() {
        // Logic to delete a tag
        //elimina il tag selezionato dalla lista dei tags
        String selected = (String) add_tags.getSelectionModel().getSelectedItem();
        if (selected != null) {
            add_tags.getItems().remove(selected);
        }
    }

    /**
     * Gestisce la visibilità dei campi {@code add_freq} e {@code add_occ}.
     * Questi campi sono visibili solo se è selezionato almeno uno tra "ripetizione" e "rata".
     */
    public void setVisible() {
        // Logica per rendere visibili i campi add_freq e add_occ solo se add_rep o add_rata sono selezionati
        boolean isRepSelected = add_rep.isSelected();
        boolean isRataSelected = add_rata.isSelected();
        add_freq.setVisible(isRepSelected || isRataSelected);
        add_occ.setVisible(isRepSelected || isRataSelected);
    }

    /**
     * Carica le sottocategorie dal file {@code tags.xml}.
     * - Se {@code fam} è null, carica tutte le sottocategorie.
     * - Altrimenti, carica solo quelle appartenenti alla famiglia/categoria indicata.
     *
     * @param fam Nome della categoria principale, oppure null per tutte.
     */
    public void loadSottocat(String fam) {
        List<String> sottocatList = new ArrayList<>();
        try (FileInputStream in = new FileInputStream("app/data/tags.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            doc.getDocumentElement().normalize();

            if (fam == null || fam.isBlank()) {
                NodeList children = doc.getElementsByTagName("chtag");
                for (int j = 0; j < children.getLength(); j++) {
                    Element sub = (Element) children.item(j);
                    sottocatList.add(sub.getAttribute("name"));
                }
            } else {
                NodeList tags = doc.getElementsByTagName("tag");
                for (int i = 0; i < tags.getLength(); i++) {
                    Element tag = (Element) tags.item(i);
                    if (tag.getAttribute("name").equalsIgnoreCase(fam)) {
                        NodeList children = tag.getElementsByTagName("chtag");
                        for (int j = 0; j < children.getLength(); j++) {
                            Element sub = (Element) children.item(j);
                            sottocatList.add(sub.getAttribute("name"));
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add_sottocat.getItems().setAll(sottocatList);
    }

    /**
     * Carica gli autori dal file {@code users.xml} e li inserisce nella comboBox {@code add_autore}.
     */
    public void loadAutore() {
        List<Author> autori = new ArrayList<>();
        try (FileInputStream in = new FileInputStream("app/data/users.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            NodeList list = doc.getElementsByTagName("user");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String name = el.getAttribute("name");
                autori.add(new Author(i, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Author autore : autori) {
            add_autore.getItems().add(autore.name());
        }

    }


}
