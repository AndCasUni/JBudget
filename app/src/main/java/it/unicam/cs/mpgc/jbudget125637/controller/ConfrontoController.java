package it.unicam.cs.mpgc.jbudget125637.controller;

import it.unicam.cs.mpgc.jbudget125637.model.Author;
import it.unicam.cs.mpgc.jbudget125637.model.Operation;
import it.unicam.cs.mpgc.jbudget125637.model.Tags;
import it.unicam.cs.mpgc.jbudget125637.persistency.OperationXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.persistency.TagXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.persistency.UserXmlRepository;
import it.unicam.cs.mpgc.jbudget125637.service.RefreshService;
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
import java.util.function.Predicate;

public class ConfrontoController {

    private OperationXmlRepository operationRepository;
    private TagXmlRepository tagRepository;
    private UserXmlRepository userRepository;

    @FXML private DatePicker conf_dadata;
    @FXML private DatePicker conf_adata;
    @FXML private PieChart conf_tortain;
    @FXML private PieChart conf_tortaout;
    @FXML private StackedBarChart<String, Number> conf_stackentrate;
    @FXML private StackedBarChart<String, Number> conf_stackuscite;
    @FXML private ListView<String> conf_resoconto;

    private List<Operation> filteredOperations = new ArrayList<>();
    private List<Operation> allOperations = new ArrayList<>();
    private List<Tags> allTags = new ArrayList<>();
    private List<Author> allAuthors = new ArrayList<>();

    public ConfrontoController() {
    }

    @FXML
    public void initialize() {
        this.operationRepository = new OperationXmlRepository();
        this.tagRepository = new TagXmlRepository();
        this.userRepository = new UserXmlRepository();

        RefreshService.registerConfrontoController(this);
        loadAllData();
        setupDateListeners();
        reloadVisualizations();
    }

    /**
     * inizializza i listener per i DatePicker
     */
    private void setupDateListeners() {
        conf_dadata.valueProperty().addListener((obs, oldVal, newVal) -> reloadVisualizations());
        conf_adata.valueProperty().addListener((obs, oldVal, newVal) -> reloadVisualizations());
    }

    public void loadAllData() {
        allAuthors = userRepository.read();
        allOperations = operationRepository.read();
        allTags = tagRepository.readChild();
    }

    /**
     * Ricarica tutte le visualizzazioni basate sui dati filtrati
     */
    public void reloadVisualizations() {
        filterOperationsByDate();
        updatePieCharts();
        updateStackedBarCharts();
        updateSummaryReport();
    }

    /**
     * Filtra le operazioni in base alle date selezionate
     */
    private void filterOperationsByDate() {
        LocalDate startDate = conf_dadata.getValue();
        LocalDate endDate = conf_adata.getValue();

        Predicate<Operation> dateFilter = createDateFilter(startDate, endDate);
        filteredOperations = allOperations.stream()
                .filter(dateFilter)
                .toList();
    }

    /**
     * Crea un predicato per filtrare le operazioni in base alle date
     */
    private Predicate<Operation> createDateFilter(LocalDate startDate, LocalDate endDate) {
        return operation -> {
            try {
                LocalDate operationDate = LocalDate.parse(operation.getDate());
                return (startDate == null || !operationDate.isBefore(startDate)) &&
                        (endDate == null || !operationDate.isAfter(endDate));
            } catch (Exception e) {
                return false;
            }
        };
    }

    /**
     * aggiorna entrambi i grafici a torta
     */
    private void updatePieCharts() {
        updatePieChart(conf_tortain, amount -> amount > 0, "Entrate");
        updatePieChart(conf_tortaout, amount -> amount < 0, "Uscite");
    }

    /**
     * aggiorna un grafico a torta specifico
     */
    private void updatePieChart(PieChart chart, Predicate<Double> amountFilter, String title) {
        if (filteredOperations.isEmpty() || allTags.isEmpty()) {
            chart.getData().clear();
            return;
        }

        ObservableList<PieChart.Data> pieData = createPieChartData(amountFilter);
        configurePieChart(chart, pieData, title);
    }

    /**
     * crea i dati per il grafico a torta basati sul filtro di importo
     */
    private ObservableList<PieChart.Data> createPieChartData(Predicate<Double> amountFilter) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        allTags.forEach(tag -> {
            double total = calculateTagTotal(tag, amountFilter);
            if (total != 0) {
                pieData.add(new PieChart.Data(tag.description().trim(), Math.abs(total)));
            }
        });

        return pieData;
    }

    /**
     * calcola il totale per un tag specifico basato sul filtro di importo
     */
    private double calculateTagTotal(Tags tag, Predicate<Double> amountFilter) {
        return filteredOperations.stream()
                .filter(operation -> hasTag(operation, tag))
                .mapToDouble(Operation::getAmount)
                .filter(amountFilter::test)
                .sum();
    }

    /**
     * controlla se un'operazione ha un tag specifico
     */
    private boolean hasTag(Operation operation, Tags tag) {
        return operation.getTags().stream()
                .anyMatch(t -> t.description().trim().equalsIgnoreCase(tag.description().trim()));
    }

    /**
     * configura le proprietà del grafico a torta
     */
    private void configurePieChart(PieChart chart, ObservableList<PieChart.Data> data, String title) {
        chart.setTitle(title);
        chart.setLegendVisible(true);
        chart.setLegendSide(Side.BOTTOM);
        chart.setLabelsVisible(true);
        chart.setLabelLineLength(10);
        chart.setData(data);
    }

    /**
     * aggiorna entrambi i grafici a barre impilate
     */
    private void updateStackedBarCharts() {
        if (filteredOperations.isEmpty() || allTags.isEmpty() || allAuthors.isEmpty()) {
            conf_stackentrate.getData().clear();
            conf_stackuscite.getData().clear();
            return;
        }

        BarChartData positiveData = calculateBarChartData(amount -> amount > 0);
        BarChartData negativeData = calculateBarChartData(amount -> amount < 0);

        conf_stackentrate.setData(positiveData.series());
        conf_stackentrate.setTitle("Entrate per Autore");
        conf_stackentrate.setLegendVisible(true);

        conf_stackuscite.setData(negativeData.series());
        conf_stackuscite.setTitle("Uscite per Autore");
        conf_stackuscite.setLegendVisible(true);
    }

    /**
     * calcola i dati per il grafico a barre impilate basati sul filtro di importo
     */
    private BarChartData calculateBarChartData(Predicate<Double> amountFilter) {
        Map<String, Map<String, Double>> totalsMap = initializeTotalsMap();

        filteredOperations.forEach(operation -> {
            if (operation.getAutore() == null || operation.getTags() == null) return;

            String author = operation.getAutore();
            double amount = operation.getAmount();

            if (totalsMap.containsKey(author) && amountFilter.test(amount)) {
                operation.getTags().forEach(tag -> {
                    double current = totalsMap.get(author).getOrDefault(tag.description(), 0.0);
                    totalsMap.get(author).put(tag.description(), current + Math.abs(amount));
                });
            }
        });

        return createBarChartSeries(totalsMap);
    }

    /**
     * inizializza la mappa dei totali per autori e tag
     */
    private Map<String, Map<String, Double>> initializeTotalsMap() {
        Map<String, Map<String, Double>> totalsMap = new HashMap<>();

        allAuthors.forEach(author -> {
            Map<String, Double> tagMap = new HashMap<>();
            allTags.forEach(tag -> tagMap.put(tag.description(), 0.0));
            totalsMap.put(author.name(), tagMap);
        });

        return totalsMap;
    }

    /**
     * crea le serie per il grafico a barre impilate
     */
    private BarChartData createBarChartSeries(Map<String, Map<String, Double>> totalsMap) {
        ObservableList<StackedBarChart.Series<String, Number>> seriesList = FXCollections.observableArrayList();

        allTags.forEach(tag -> {
            StackedBarChart.Series<String, Number> series = new StackedBarChart.Series<>();
            series.setName(tag.description());

            allAuthors.forEach(author -> {
                double total = totalsMap.get(author.name()).get(tag.description());
                series.getData().add(new StackedBarChart.Data<>(author.name(), total));
            });

            if (series.getData().stream().anyMatch(data -> data.getYValue().doubleValue() > 0)) {
                seriesList.add(series);
            }
        });

        return new BarChartData(seriesList);
    }

    /**
     * aggiorna il resoconto finanziario
     */
    private void updateSummaryReport() {
        if (filteredOperations.isEmpty()) {
            showNoOperationsMessage();
            return;
        }

        FinancialSummary summary = calculateFinancialSummary();
        displayFinancialSummary(summary);
    }

    /**
     * calcola il resoconto finanziario
     */
    private FinancialSummary calculateFinancialSummary() {
        LocalDate today = LocalDate.now();
        FinancialSummary summary = new FinancialSummary();

        filteredOperations.forEach(operation -> {
            try {
                LocalDate operationDate = LocalDate.parse(operation.getDate());
                double amount = operation.getAmount();

                if (operationDate.isAfter(today)) {
                    summary.addProgrammed(amount);
                } else {
                    summary.addExecuted(amount);
                }
            } catch (Exception e) {
                System.err.println("Errore elaborazione operazione: " + operation);
            }
        });

        return summary;
    }

    /**
     * mostra il resoconto finanziario nella ListView
     */
    private void displayFinancialSummary(FinancialSummary summary) {
        conf_resoconto.getItems().clear();
        conf_resoconto.getItems().addAll(
                "Entrate eseguite: " + String.format("%.2f €", summary.getExecutedIncome()),
                "Uscite eseguite: " + String.format("%.2f €", Math.abs(summary.getExecutedExpense())),
                "Entrate programmate: " + String.format("%.2f €", summary.getProgrammedIncome()),
                "Uscite programmate: " + String.format("%.2f €", Math.abs(summary.getProgrammedExpense())),
                "Saldo attuale: " + String.format("%.2f €", summary.getCurrentBalance()),
                "Saldo futuro: " + String.format("%.2f €", summary.getFutureBalance())
        );
    }

    private void showNoOperationsMessage() {
        conf_resoconto.getItems().clear();
        conf_resoconto.getItems().add("Nessuna operazione disponibile");
    }

    /**
     * Record per contenere i dati del grafico a barre impilate
     */
    private record BarChartData(ObservableList<StackedBarChart.Series<String, Number>> series) {}

    /**
     * Classe interna per gestire il resoconto finanziario
     */
    private static class FinancialSummary {
        private double executedIncome = 0;
        private double executedExpense = 0;
        private double programmedIncome = 0;
        private double programmedExpense = 0;

        public void addExecuted(double amount) {
            if (amount > 0) executedIncome += amount;
            else executedExpense += amount;
        }

        public void addProgrammed(double amount) {
            if (amount > 0) programmedIncome += amount;
            else programmedExpense += amount;
        }

        public double getExecutedIncome() { return executedIncome; }
        public double getExecutedExpense() { return executedExpense; }
        public double getProgrammedIncome() { return programmedIncome; }
        public double getProgrammedExpense() { return programmedExpense; }
        public double getCurrentBalance() { return executedIncome + executedExpense; }
        public double getFutureBalance() {
            return executedIncome + executedExpense + programmedIncome + programmedExpense;
        }
    }
}