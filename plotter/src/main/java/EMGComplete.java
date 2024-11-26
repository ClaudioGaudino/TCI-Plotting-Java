import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.github.psambit9791.jdsp.filter.Butterworth;

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
    private ChartPanel chartPanel;

    public EMGComplete(String csvFilePath) {
        // Imposta la finestra
        setTitle("EMG Signal Plotter");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Leggi il file CSV
        //readCSV(csvFilePath);

        //Leggi il fileCSV + Filtraggio Passa-Banda
        readCSVAndFilter(csvFilePath);


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

        // Crea un pannello iniziale per il grafico
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        add(chartPanel, BorderLayout.CENTER);
    }

    private void readCSVAndFilter(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;

            List<Double> timeList = new ArrayList<>();
            List<List<Double>> emgColumns = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] headers = line.split(",");

                    for (int i = 2; i < headers.length; i++) {
                        emgLabels.add(headers[i].trim());
                        emgColumns.add(new ArrayList<>());
                    }
                    continue;
                }

                String[] values = line.split(",");
                timeList.add(Double.parseDouble(values[1].trim()));

                for (int i = 0; i < emgLabels.size(); i++) {
                    emgColumns.get(i).add(Double.parseDouble(values[i + 2].trim()));
                }
            }

            timeData = timeList.stream().mapToDouble(Double::doubleValue).toArray();

            for (List<Double> column : emgColumns) {
                double[] rawSignal = column.stream().mapToDouble(Double::doubleValue).toArray();
                double[] filteredSignal = applyBandPassFilter(rawSignal);
                double[] filteredSignal2 = applyHighPassFilter(filteredSignal);
                double[] filteredSignal3 = fullWaveRectify(filteredSignal2);
                double[] filteredSignal4 = applyLowPassFilter(filteredSignal3);
                emgData.add(filteredSignal4);

            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }


    }

    //Filtraggio Passa-Basso
    private double[] applyLowPassFilter(double[] signal) {
        // Crea un'istanza del filtro Butterworth con la frequenza di campionamento
        Butterworth butterworth = new Butterworth(2000);

        // Applica il filtro al segnale e ottieni il risultato

        return butterworth.lowPassFilter(signal, 4, 15);
    }

    //Filtraggio Passa-Alto
    private double[] applyHighPassFilter(double[] signal) {
        // Crea un'istanza del filtro Butterworth con la frequenza di campionamento
        Butterworth butterworth = new Butterworth(2000);

        // Applica il filtro al segnale e ottieni il risultato

        return butterworth.highPassFilter(signal, 4, 40);
    }

    // Metodo per rettificare un segnale (full-wave rectification)
    private double[] fullWaveRectify(double[] signal) {
        double[] rectifiedSignal = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            rectifiedSignal[i] = Math.abs(signal[i]);
        }
        return rectifiedSignal;
    }

    //Filtraggio Passa-Banda
    private double[] applyBandPassFilter(double[] signal) {
        // Crea un'istanza del filtro Butterworth con la frequenza di campionamento
        Butterworth butterworth = new Butterworth(2000);

        // Applica il filtro al segnale e ottieni il risultato

        return butterworth.bandPassFilter(signal, 4, 20, 450);
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
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        //Aggiornamento in real time
        chartPanel.setChart(chart);
        printFilteredSignal(emgSignal);
    }

    private void printFilteredSignal(double[] signal) {
        // Stampa l'intestazione della tabella
        System.out.printf("%-10s %-10s %-15s%n", "Frame", "Time", emgLabels.get(0));

        // Stampa i dati in formato tabellare
        for (int i = 0; i < timeData.length; i++) {
            // Stampa il frame (indice), il tempo e il valore del segnale EMG per il frame i
            System.out.printf("%-10d %-10.3f %-15.4f%n", i, timeData[i], signal[i]);
        }
    }


    public static void main(String[] args) {
        // Percorso del file CSV
        String csvFilePath = "data\\EMGs.csv";
        SwingUtilities.invokeLater(() -> new EMGComplete(csvFilePath).setVisible(true));
    }
}

