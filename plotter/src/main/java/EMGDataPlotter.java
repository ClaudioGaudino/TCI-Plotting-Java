import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EMGDataPlotter extends JFrame {



    public EMGDataPlotter(String csvFilePath) {

        List<List<Double>> data = readCSVData(csvFilePath);
        // Crea un grafico per "prima colonna muscolo"
        int columnIndex = 2; // Prima colonna (indice basato sull'ordine della tabella)
        XYSeries series = new XYSeries("Prima colonna");

        for (int i = 0; i < data.get(0).size(); i++) {
            series.add(i, data.get(columnIndex).get(i)); // usa 'i' come valore di tempo
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "EMG Data Plot",         // Titolo del grafico
                "X",                 // Asse X
                "Y",         // Asse Y
                dataset,                 // Dataset
                PlotOrientation.VERTICAL,
                true,                    // Legenda
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private List<List<Double>> readCSVData(String csvFilePath) {


        //Lettura Dati per una sola colonna
        List<List<Double>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                // Salta la prima riga di intestazione
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] values = line.split(",");

                // Aggiunge i valori alle rispettive colonne
                for (int i = 0; i < values.length; i++) {
                    // Inizializza le liste delle colonne se necessario
                    if (data.size() <= i) {
                        data.add(new ArrayList<>());
                    }
                    try {
                        data.get(i).add(Double.parseDouble(values[i]));
                    } catch (NumberFormatException e) {
                        System.err.println("Errore nel parsing del valore: " + values[i]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }
}
