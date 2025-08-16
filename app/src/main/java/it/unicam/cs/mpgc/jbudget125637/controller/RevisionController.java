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
import java.util.ArrayList;
import java.util.List;

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

    public void initialize() {
        loadAutore();
    }

    public void cerca()
    {
        rev_colautore.setCellValueFactory(new PropertyValueFactory<>("author"));
        rev_coldesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        rev_coldata.setCellValueFactory(new PropertyValueFactory<>("date"));
        rev_colimpo.setCellValueFactory(new PropertyValueFactory<>("amount"));
        rev_coltag1.setCellValueFactory(new PropertyValueFactory<>("tag1"));
        rev_coltag2.setCellValueFactory(new PropertyValueFactory<>("tag2"));
        rev_coltag3.setCellValueFactory(new PropertyValueFactory<>("tag3"));
        // Implement search logic here
        String autore = rev_autore.getValue();
        boolean entrate = rev_entrate.isSelected();
        boolean uscite = rev_uscite.isSelected();

        //date da trasformare in formato GG/MM/YYYY

        String dataInizio = rev_dadata.getValue() != null ? rev_dadata.getValue().toString() : null;
        String dataFine = rev_adata.getValue() != null ? rev_adata.getValue().toString() : null;

        List<String> selectedTags = new ArrayList<>();
        if (rev_casa.isSelected()) selectedTags.add(rev_casa.getText());
        if (rev_lavoro.isSelected()) selectedTags.add(rev_lavoro.getText());
        if (rev_hobby.isSelected()) selectedTags.add(rev_hobby.getText());
        if (rev_sport.isSelected()) selectedTags.add(rev_sport.getText());
        if (rev_cibo.isSelected()) selectedTags.add(rev_cibo.getText());
        if (rev_salute.isSelected()) selectedTags.add(rev_salute.getText());

        // Add logic to filter revisions based on the selected criteria
        System.out.println("Cerca: " + autore + ", Entrate: " + entrate + ", Uscite: " + uscite + ", Data Inizio: " + dataInizio + ", Data Fine: " + dataFine + "tags " + selectedTags);
        try {
            File xmlFile = new File("app/data/operations.xml");
            System.out.println(xmlFile.getPath() + " " + xmlFile.exists());

            if (!xmlFile.exists()) return;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("operation");

            ObservableList<CompleteOperationRow> data = FXCollections.observableArrayList();

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
                    //debugging
                    System.out.println("Autore: " + author + ", Descrizione: " + description + ", Data: " + date + ", Importo: " + amount + ", Tags: " + tagsList);

                }
            }

            rev_lista.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
