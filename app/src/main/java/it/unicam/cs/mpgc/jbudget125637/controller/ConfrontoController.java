package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.persistency.TagXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.persistency.UserXmlRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;

import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;

import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfrontoController {

    @FXML
    private DatePicker conf_dadata;
    @FXML
    private DatePicker conf_adata;
    @FXML
    private PieChart conf_tortain;
    @FXML
    private PieChart conf_tortaout;
    @FXML
    private StackedBarChart conf_stack;
    @FXML
    private ListView conf_resoconto;

    List<Operation> filtered = new ArrayList<>();
    List<Operation> operations = new ArrayList<>();
    List<Tags> tags = new ArrayList<>();
    List<Author> autori = new ArrayList<>();

    public void initialize() {
        aggiorna();
        // Listener per conf_dadata
        conf_dadata.valueProperty().addListener((obs, oldVal, newVal) -> {
            reloadData();
        });

        // Listener per conf_adata
        conf_adata.valueProperty().addListener((obs, oldVal, newVal) -> {
            reloadData();
        });

    }

    public void aggiorna()
    {
        loadData();
        reloadData();

    }

    public void filteredOperations() {
        filtered.clear();
        LocalDate startDate = conf_dadata.getValue();
        LocalDate endDate = conf_adata.getValue();

        System.out.println("Filtro applicato: da " + startDate + " a " + endDate);

        for (Operation op : operations) {
            try {
                LocalDate opDate = LocalDate.parse(op.getDate());

                boolean include = true;

                if (startDate != null && opDate.isBefore(startDate)) {
                    include = false;
                }
                if (endDate != null && opDate.isAfter(endDate)) {
                    include = false;
                }

                if (include) {
                    filtered.add(op);
                    System.out.println("Inclusa: " + op.getDesc() + " - " + opDate);
                }
            } catch (Exception e) {
                System.err.println("Operazione saltata: " + op.getDate());
            }
        }

        System.out.println("Totale operazioni filtrate: " + filtered.size());
    }

    public void reloadData() {
        filteredOperations();
        updatePieCharts();
        updateStackedBar();
        updateResoconto();
    }


    public void loadData() {
        UserXmlRepository userXmlRepository = new UserXmlRepository();
        autori = userXmlRepository.read();

        OperationXmlRepository operationXmlRepository = new OperationXmlRepository();
        operations = operationXmlRepository.read();

        TagXmlRepository tagXmlRepository = new TagXmlRepository();
        tags = tagXmlRepository.readChild();


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
                        .anyMatch(t -> t.description().trim().equalsIgnoreCase(tag.description().trim()));
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
        if (filtered == null || filtered.isEmpty() || tags == null || autori == null) {
            return;
        }

        // Pre-calcola i totali per autore e tag
        Map<String, Map<String, Double>> totalsMap = new HashMap<>();

        // Inizializza la mappa
        for (Author autore : autori) {
            totalsMap.put(autore.name(), new HashMap<>());
            for (Tags tag : tags) {
                totalsMap.get(autore.name()).put(tag.description(), 0.0);
            }
        }

        // Popola la mappa con i dati
        for (Operation op : filtered) {
            if (op.getAutore() == null || op.getTags() == null) continue;

            String autore = op.getAutore();
            for (Tags tag : op.getTags()) {
                if (totalsMap.containsKey(autore) && totalsMap.get(autore).containsKey(tag.description())) {
                    double current = totalsMap.get(autore).get(tag.description());
                    totalsMap.get(autore).put(tag.description(), current + Math.abs(op.getAmount()));
                }
            }
        }

        // Crea le series per TAG (sull'asse X ci saranno gli AUTORI)
        ObservableList<StackedBarChart.Series<String, Number>> seriesList = FXCollections.observableArrayList();

        for (Tags tag : tags) {
            StackedBarChart.Series<String, Number> series = new StackedBarChart.Series<>();
            series.setName(tag.description());

            for (Author autore : autori) {
                double totale = totalsMap.get(autore.name()).get(tag.description());
                series.getData().add(new StackedBarChart.Data<>(autore.name(), totale));
            }

            // Aggiungi la serie solo se ha dati > 0
            if (series.getData().stream().anyMatch(data -> data.getYValue().doubleValue() > 0)) {
                seriesList.add(series);
            }
        }

        conf_stack.setData(seriesList);
        conf_stack.setTitle("Spese/Entrate per Autore");
        conf_stack.setLegendVisible(true);
    }


    public void updateResoconto() {
        if (filtered == null || filtered.isEmpty()) {
            conf_resoconto.getItems().clear();
            conf_resoconto.getItems().add("Nessuna operazione disponibile");
            return;
        }

        LocalDate startDate = conf_dadata.getValue();
        LocalDate endDate = conf_adata.getValue();
        LocalDate today = LocalDate.now();

        double totaleEntrate = 0.0;
        double totaleUscite = 0.0;
        double totaleEntrateProg = 0.0;
        double totaleUsciteProg = 0.0;

        for (Operation op : filtered) {
            try {
                LocalDate opDate = LocalDate.parse(op.getDate());

                // Controlla se è nel range selezionato
                boolean inRange = (startDate == null || !opDate.isBefore(startDate)) &&
                        (endDate == null || !opDate.isAfter(endDate));

                if (!inRange) continue;

                // Separazione eseguite/programmate
                if (opDate.isAfter(today)) {
                    // Programmate
                    if (op.getAmount() > 0) totaleEntrateProg += op.getAmount();
                    else if (op.getAmount() < 0) totaleUsciteProg += op.getAmount();
                } else {
                    // Eseguite
                    if (op.getAmount() > 0) totaleEntrate += op.getAmount();
                    else if (op.getAmount() < 0) totaleUscite += op.getAmount();
                }
            } catch (Exception e) {
                System.err.println("Errore elaborazione operazione: " + op);
            }
        }

        conf_resoconto.getItems().clear();
        conf_resoconto.getItems().addAll(
                "Entrate eseguite: " + String.format("%.2f €", totaleEntrate),
                "Uscite eseguite: " + String.format("%.2f €", Math.abs(totaleUscite)),
                "Entrate programmate: " + String.format("%.2f €", totaleEntrateProg),
                "Uscite programmate: " + String.format("%.2f €", Math.abs(totaleUsciteProg)),
                "Saldo attuale: " + String.format("%.2f €", (totaleEntrate + totaleUscite)),
                "Saldo futuro: " + String.format("%.2f €", (totaleEntrate + totaleUscite + totaleEntrateProg + totaleUsciteProg))
        );
    }

}