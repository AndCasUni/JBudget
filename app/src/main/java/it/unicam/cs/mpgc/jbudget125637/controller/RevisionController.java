package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import it.unicam.cs.mpgc.jbudget125637.model.CompleteOperationRow;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**

 * Questa classe gestisce il caricamento e il filtraggio delle operazioni
 * (entrate/uscite) in base ai criteri selezionati dall'utente:
 * autore, tipologia di movimento, intervallo di date e tag.

 * I tag supportano una gerarchia padre/figlio caricata da file XML,
 * in modo che selezionando un tag padre vengano automaticamente
 * considerati anche i suoi figli.
 */
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

    @FXML
    private TableView<CompleteOperationRow> rev_lista;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_colautore;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_coldata;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_coldesc;
    @FXML
    private TableColumn<CompleteOperationRow, Double> rev_colimpo;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_coltag1;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_coltag2;
    @FXML
    private TableColumn<CompleteOperationRow, String> rev_coltag3;

    /** Gerarchia di tag padre → lista di figli caricata da XML. */
    private Map<String, List<String>> tagHierarchy;

    /** Lista completa delle operazioni caricate da file XML. */
    ObservableList<CompleteOperationRow> data = FXCollections.observableArrayList();

    /**
     * Metodo di inizializzazione chiamato automaticamente da JavaFX.
     *
     * - Imposta i binding delle colonne della tabella
     * - Carica gli autori disponibili da file
     * - Carica le operazioni
     * - Carica la gerarchia dei tag
     */
    public void initialize() {
        rev_colautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        rev_coldesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        rev_coldata.setCellValueFactory(new PropertyValueFactory<>("date"));
        rev_colimpo.setCellValueFactory(new PropertyValueFactory<>("amount"));
        rev_coltag1.setCellValueFactory(new PropertyValueFactory<>("tag1"));
        rev_coltag2.setCellValueFactory(new PropertyValueFactory<>("tag2"));
        rev_coltag3.setCellValueFactory(new PropertyValueFactory<>("tag3"));
        loadAutore();
        loadData();
        tagHierarchy = loadTagHierarchy("app/data/tags.xml");

    }

    /**
     * Applica i filtri impostati (autore, entrate/uscite, date, tag) e
     * aggiorna la tabella {@code rev_lista} con le operazioni filtrate.
     *
     * Filtri applicabili:
     * - Autore selezionato dalla combo box
     * - Entrate e/o uscite tramite checkbox
     * - Intervallo di date tramite date picker
     * - Tag padre (con espansione automatica dei figli)
     */
    public void cerca()
    {

        String autore = rev_autore.getValue();
        boolean entrate = rev_entrate.isSelected();
        boolean uscite = rev_uscite.isSelected();

        //date da trasformare in formato GG/MM/YYYY

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String dataInizio = rev_dadata.getValue() != null ? rev_dadata.getValue().format(formatter) : null;
        String dataFine   = rev_adata.getValue() != null ? rev_adata.getValue().format(formatter) : null;

        List<String> selectedTags = new ArrayList<>();
        if (rev_casa.isSelected()) selectedTags.add(rev_casa.getText());
        if (rev_lavoro.isSelected()) selectedTags.add(rev_lavoro.getText());
        if (rev_hobby.isSelected()) selectedTags.add(rev_hobby.getText());
        if (rev_sport.isSelected()) selectedTags.add(rev_sport.getText());
        if (rev_cibo.isSelected()) selectedTags.add(rev_cibo.getText());
        if (rev_salute.isSelected()) selectedTags.add(rev_salute.getText());

        System.out.println("Cerca: " + autore + ", Entrate: " + entrate + ", Uscite: " + uscite + ", Data Inizio: " + dataInizio + ", Data Fine: " + dataFine + "tags " + selectedTags);
        ObservableList<CompleteOperationRow> filteredData = FXCollections.observableArrayList();
        Set<String> expandedTags = new HashSet<>();
        for (String tag : selectedTags) {
            expandedTags.add(tag); // sempre il padre
            expandedTags.addAll(tagHierarchy.getOrDefault(tag, Collections.emptyList())); // i figli
        }

        for (CompleteOperationRow row : data) {
            boolean matchesAutore = (autore == null || autore.isEmpty() || row.getAuthor().equals(autore));

            // Gestione entrate/uscite
            boolean matchesEntrateUscite;
            if (entrate && uscite) {
                matchesEntrateUscite = true; // entrambi selezionati → includo tutto
            } else if (entrate) {
                matchesEntrateUscite = row.getAmount() > 0;
            } else if (uscite) {
                matchesEntrateUscite = row.getAmount() < 0;
            } else {
                matchesEntrateUscite = true; // nessuno selezionato → includo tutto
            }

            // Gestione date
            boolean matchesDate = true;
            if (dataInizio != null && !dataInizio.isEmpty() && dataFine != null && !dataFine.isEmpty()) {
                matchesDate = isInRange(row.getDate(), dataInizio, dataFine);
            } else if (dataInizio != null && !dataInizio.isEmpty()) {
                matchesDate = !LocalDate.parse(row.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        .isBefore(LocalDate.parse(dataInizio, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else if (dataFine != null && !dataFine.isEmpty()) {
                matchesDate = !LocalDate.parse(row.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        .isAfter(LocalDate.parse(dataFine, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }

            // Gestione tags
            boolean matchesTags = expandedTags.isEmpty() ||
                    expandedTags.stream().anyMatch(tag ->
                            tag.equals(row.getTag1()) || tag.equals(row.getTag2()) || tag.equals(row.getTag3())
                    );
            // Filtro finale
            if (matchesAutore && matchesEntrateUscite && matchesDate && matchesTags) {
                filteredData.add(row);
            }
        }

        rev_lista.setItems(filteredData);


        rev_lista.setItems(filteredData);
    }
    /**
     * Carica la gerarchia di tag dal file XML.
     * Ogni tag padre può contenere zero o più sotto-tag (<chtag>).
     *
     * @param xmlPath percorso del file XML dei tag
     * @return mappa contenente il nome del tag padre come chiave e la lista dei figli come valore
     */
    private Map<String, List<String>> loadTagHierarchy(String xmlPath) {
        Map<String, List<String>> tagHierarchy = new HashMap<>();
        try {
            File xmlFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // prendo tutti i tag <tag> (padri)
            NodeList tagNodes = doc.getElementsByTagName("tag");
            for (int i = 0; i < tagNodes.getLength(); i++) {
                Element tagElement = (Element) tagNodes.item(i);
                String parentName = tagElement.getAttribute("name");

                List<String> children = new ArrayList<>();

                // prendo i figli <chtag>
                NodeList childNodes = tagElement.getElementsByTagName("chtag");
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Element childElement = (Element) childNodes.item(j);
                    String childName = childElement.getAttribute("name");
                    children.add(childName);
                }

                tagHierarchy.put(parentName, children);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tagHierarchy;
    }

    /**
     * Verifica se una data è compresa nell’intervallo [startDate, endDate] e se l'intervallo è valido.
     *
     * @param date data da verificare, nel formato "dd/MM/yyyy"
     * @param startDate data di inizio intervallo, nel formato "dd/MM/yyyy"
     * @param endDate data di fine intervallo, nel formato "dd/MM/yyyy"
     * @return true se la data è compresa nell’intervallo (estremi inclusi), false altrimenti
     */
    public boolean isInRange(String date, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            LocalDate target = LocalDate.parse(date, formatter);
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            // Controllo coerenza intervallo
            if (end.isBefore(start)) {
                System.out.println("Data di fine precedente a data di inizio!");
                return false;
            }

            // Controllo inclusione nell’intervallo
            return ( !target.isBefore(start) && !target.isAfter(end) );

        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false; // formato non valido → false
        }
    }

    /**
     * Carica dal file XML la lista delle operazioni e la memorizza
     * nella variabile {@code data}.
     *
     * Ogni operazione contiene autore, descrizione, data, importo
     * e fino a tre tag.
     */
    public void loadData()
    {
        try {
            File xmlFile = new File("app/data/operations.xml");
            System.out.println(xmlFile.getPath() + " " + xmlFile.exists());

            if (!xmlFile.exists()) return;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("operation");


            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    String author = elem.getElementsByTagName("author").item(0).getTextContent();
                    String description = elem.getElementsByTagName("description").item(0).getTextContent();
                    String date = elem.getElementsByTagName("date").item(0).getTextContent();
                    Double amount = Double.parseDouble(elem.getElementsByTagName("amount").item(0).getTextContent());
                    List<String> tagsList = new ArrayList<>();
                    NodeList tags = elem.getElementsByTagName("tags");
                    for (int k = 0; k < tags.getLength(); k++) {
                        Element tag = (Element) tags.item(k);
                        NodeList children = tag.getElementsByTagName("tag");
                        for (int j = 0; j < children.getLength(); j++) {
                            Element sub = (Element) children.item(j);
                            tagsList.add(sub.getTextContent());
                        }
                    }

                    String tag1 = tagsList.size() > 0 ? tagsList.get(0) : "";
                    String tag2 = tagsList.size() > 1 ? tagsList.get(1) : "";
                    String tag3 = tagsList.size() > 2 ? tagsList.get(2) : "";

                    data.add(new CompleteOperationRow(author,description, date, amount, tag1, tag2, tag3));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Carica la lista degli autori dal file XML e la inserisce
     * nella combo box {@code rev_autore}.
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
            rev_autore.getItems().add(autore.name());
        }

    }
}
