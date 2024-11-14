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
import java.util.Arrays;
import java.util.List;

public class EMGComplete extends JFrame {
    private List<double[]> emgData = new ArrayList<>();
    private List<String> emgLabels = new ArrayList<>();
    private double[] timeData;

    public EMGComplete(String csvFilePath) {
        // Imposta la finestra
        setTitle("EMG Signal Plotter");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Leggi il file CSV
        readCSV(csvFilePath);

        // Pannello per i bottoni
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, emgLabels.size()));

        // Crea un bottone per ogni segnale EMG
        for (int i = 0; i < emgLabels.size(); i++) {
            int index = i; // Indice per il segnale EMG
            JButton button = new JButton(emgLabels.get(i));
            button.addActionListener(e -> plotEMGSignal(index));
            buttonPanel.add(button);
        }

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Metodo per leggere il file CSV e salvare i dati
    private void readCSV(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true; // Variabile per riconoscere la prima riga (intestazione)

            List<Double> timeList = new ArrayList<>();
            List<List<Double>> emgColumns = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                // Processa la prima riga come intestazione
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] headers = line.split(",");

                    // Aggiungi le etichette dei segnali EMG a partire dalla terza colonna
                    for (int i = 2; i < headers.length; i++) {
                        emgLabels.add(headers[i].trim());
                        emgColumns.add(new ArrayList<>()); // Crea una colonna per ogni segnale EMG
                    }
                    continue; // Salta alla prossima iterazione per processare i dati
                }

                // Debug: stampa ogni riga dei dati
                System.out.println("Riga dei dati: " + line);

                String[] values = line.split(",");
                // Parsing della colonna Time
                timeList.add(Double.parseDouble(values[1].trim()));

                // Aggiungi i valori EMG per ogni colonna
                for (int i = 0; i < emgLabels.size(); i++) {
                    emgColumns.get(i).add(Double.parseDouble(values[i + 2].trim()));
                }
            }

            // Converti i dati in array
            timeData = timeList.stream().mapToDouble(Double::doubleValue).toArray();
            for (List<Double> column : emgColumns) {
                emgData.add(column.stream().mapToDouble(Double::doubleValue).toArray());
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Metodo per visualizzare un grafico per un segnale EMG specifico
    private void plotEMGSignal(int index) {
        XYSeries series = new XYSeries(emgLabels.get(index));
        double[] emgSignal = emgData.get(index);

        for (int i = 0; i < timeData.length; i++) {
            series.add(timeData[i], emgSignal[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "EMG Signal Plot - " + emgLabels.get(index),
                "Time (s)",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Mostra il grafico in una finestra di dialogo
        ChartPanel chartPanel = new ChartPanel(chart);
        JDialog dialog = new JDialog(this, "EMG Signal: " + emgLabels.get(index), true);
        dialog.setSize(600, 400);
        dialog.add(chartPanel);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        // Percorso del file CSV
        String csvFilePath = "data\\EMGs.csv";
        SwingUtilities.invokeLater(() -> new EMGComplete(csvFilePath).setVisible(true));
    }
}

