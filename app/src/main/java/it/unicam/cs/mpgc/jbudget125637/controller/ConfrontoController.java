package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfrontoController {

    @FXML private DatePicker conf_dadata;
    @FXML private DatePicker conf_adata;
    @FXML private PieChart conf_tortain;
    @FXML private PieChart conf_tortaout;
    @FXML private StackedBarChart conf_stack;
    @FXML private ListView conf_resoconto;

    List<Operation> filtered = new ArrayList<>();
    List<Operation> operations = new ArrayList<>();
    List<Tags> tags =  new ArrayList<>();
    List<Author> autori = new ArrayList<>();

    public void initialize() {
        loadData();
        reloadData();
        // Listener per conf_dadata
        conf_dadata.valueProperty().addListener((obs, oldVal, newVal) -> {
            reloadData();
        });

        // Listener per conf_adata
        conf_adata.valueProperty().addListener((obs, oldVal, newVal) -> {
            reloadData();
        });

    }

    public void filteredOperations() {
       filtered.clear();
        String startDate = conf_dadata.getValue() != null ?
                conf_dadata.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;
        String endDate = conf_adata.getValue() != null ?
                conf_adata.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;

        System.out.println(startDate + " " + endDate);
       for (Operation op : operations) {

           if (startDate == null && endDate == null) {
               filtered.add(op); // nessun filtro, aggiungi tutto
           } else if (startDate != null && endDate != null) {
               if (isInRange(op.getDate(), startDate, endDate)) {
                   filtered.add(op);
               }
           } else if (startDate != null) { // solo data inizio
               if (!LocalDate.parse(op.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                       .isBefore(LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")))) {
                   filtered.add(op);
               }
           } else if (endDate != null) { // solo data fine
               if (!LocalDate.parse(op.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                       .isAfter(LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")))) {
                   filtered.add(op);
               }
           }
       }
        // Se nessun filtro attivo, mostra tutte le operazioni
    }

    public void reloadData() {
        filteredOperations();
        updatePieCharts();
        updateStackedBar();
        updateResoconto();
    }

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
    public void loadData() {
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


        try (FileInputStream in = new FileInputStream("app/data/operations.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);
            NodeList list = doc.getElementsByTagName("operation");
            for (int i = 0; i < list.getLength(); i++) {
                Element el = (Element) list.item(i);
                String id = el.getAttribute("id");
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

        try (FileInputStream in = new FileInputStream("app/data/tags.xml")) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(in);

            NodeList tagList = doc.getElementsByTagName("tag");

            for (int i = 0; i < tagList.getLength(); i++) {
                Element tagElement = (Element) tagList.item(i);
                String tagName = tagElement.getAttribute("name");
                int tagId = Integer.parseInt(tagElement.getAttribute("id"));

                // Aggiungi il tag principale
                tags.add(new Tags(tagId, tagName));

                // Leggi i chtag figli
                NodeList chtagList = tagElement.getElementsByTagName("chtag");
                for (int j = 0; j < chtagList.getLength(); j++) {
                    Element chtagElement = (Element) chtagList.item(j);
                    String chtagName = chtagElement.getAttribute("name");
                    int chtagId = Integer.parseInt(chtagElement.getAttribute("id"));

                    tags.add(new Tags(chtagId, chtagName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePieCharts() {
        updatePieChart(conf_tortain, true, "Entrate");   // solo valori positivi
        updatePieChart(conf_tortaout, false, "Uscite"); // solo valori negativi
    }

    private void updatePieChart(PieChart chart, boolean positiveOnly, String title) {
        chart.getData().clear();
        if (filtered == null || filtered.isEmpty() || tags == null) {
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Tags tag : tags) {
            double totale = 0.0;
            for (Operation op : filtered) {
                boolean match = op.getTags().stream()
                        .anyMatch(t -> t.trim().equalsIgnoreCase(tag.description().trim()));
                if (match) {
                    double amount = op.getAmount();
                    if ((positiveOnly && amount > 0) || (!positiveOnly && amount < 0)) {
                        totale += amount;
                    }
                }
            }

            if (totale != 0) {
                pieData.add(new PieChart.Data(tag.description().trim(), Math.abs(totale)));
            }
        }

        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(10);
        chart.setData(pieData);
    }



    public void updateStackedBar() {
        conf_stack.getData().clear();
        if (filtered == null || filtered.isEmpty() || tags == null || autori == null) return;

        for (Tags tag : tags) {
            StackedBarChart.Series<String, Number> series = new StackedBarChart.Series<>();
            series.setName(tag.description());

            for (Author autore : autori) {
                double totale = 0.0;
                for (Operation op : filtered) {
                    if (op.getAutore().equals(autore.name()) &&
                            op.getTags().contains(tag.description())) {
                        totale += op.getAmount();
                    }
                }
                if (totale != 0) { // aggiungi solo valori non nulli
                    series.getData().add(new StackedBarChart.Data<>(autore.name(), Math.abs(totale)));
                }
            }

            if (!series.getData().isEmpty()) {
                conf_stack.getData().add(series);
            }
        }
    }


    public void updateResoconto() {
        if (filtered == null || filtered.isEmpty()) {
            conf_resoconto.getItems().clear();
            conf_resoconto.getItems().add("Nessuna operazione disponibile");
            return;
        }

        LocalDate startDate = conf_dadata.getValue(); // data di inizio per totali
        LocalDate endDate = conf_adata.getValue();    // data di fine per programmate

        double totaleEntrate = 0.0;
        double totaleUscite = 0.0;
        double totaleEntrateProg = 0.0;
        double totaleUsciteProg = 0.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Operation op : filtered) {
            LocalDate opDate = LocalDate.parse(op.getDate(), formatter);

            // TOTALE
            if (startDate == null || !opDate.isBefore(startDate)) {
                if (op.getAmount() > 0) totaleEntrate += op.getAmount();
                else if (op.getAmount() < 0) totaleUscite += op.getAmount();
            }

            // PROGRAMMATE
            LocalDate today = LocalDate.now();
            if (op.isPlanned() && !opDate.isBefore(today) &&
                    (endDate == null || !opDate.isAfter(endDate))) {
                if (op.getAmount() > 0) totaleEntrateProg += op.getAmount();
                else if (op.getAmount() < 0) totaleUsciteProg += op.getAmount();
            }
        }

        // Mostra i totali nella ListView
        conf_resoconto.getItems().clear();
        conf_resoconto.getItems().addAll(
                "Totale Entrate: " + String.format("%.2f", totaleEntrate),
                "Totale Uscite: " + String.format("%.2f", totaleUscite),
                "Totale Entrate Programmate: " + String.format("%.2f", totaleEntrateProg),
                "Totale Uscite Programmate: " + String.format("%.2f", totaleUsciteProg)
        );
    }


}
