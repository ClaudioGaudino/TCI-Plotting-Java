import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
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


        //Leggi il fileCSV + Filtraggio Butter
        readCSVAndFilter(csvFilePath);


        //Pannello per i bottoni
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, emgLabels.size()));

        /*// Crea un bottone per ogni segnale EMG
        for (int i = 0; i < emgLabels.size(); i++) {
            int index = i; // Indice per il segnale EMG
            JButton button = new JButton(emgLabels.get(i));
            button.addActionListener(e -> plotEMGSignal(index));
            //button.addActionListener(e -> plotNormalizedSignal(index));
            buttonPanel.add(button);
        }*/

        add(buttonPanel, BorderLayout.SOUTH);

        // Crea un pannello iniziale per il grafico
        chartPanel = new ChartPanel(null);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        add(chartPanel, BorderLayout.CENTER);

        for (int i = 0; i < emgLabels.size(); i++) {
            int index = i; // Indice per il segnale EMG

            // Bottone per la visualizzazione filtrata
            JButton button = new JButton(emgLabels.get(i));
            button.addActionListener(e -> plotEMGSignal(index));
            buttonPanel.add(button);

            // Bottone per la normalizzazione con scelta del range
            JButton normalizeButton = new JButton("Normalize con range " + emgLabels.get(i));
            normalizeButton.addActionListener(e -> plotNormalizedWithRange(index));
            buttonPanel.add(normalizeButton);

            // Bottone per confronto
            JButton compareButton = new JButton("Compare " + emgLabels.get(i));
            compareButton.addActionListener(e -> plotComparison(index));
            buttonPanel.add(compareButton);
        }

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

                //double[] normalizedSignal = normalizeToFixedLength(timeData, filteredSignal4, 201);
                //emgData.add(normalizedSignal);

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

    //Normalizzazione
    private double[] normalizeToFixedLength(double[] time, double[] signal, int points) {
        int length = Math.min(points, time.length);
        double[] truncatedTime = Arrays.copyOfRange(time, 0, length);
        double[] truncatedSignal = Arrays.copyOfRange(signal, 0, length);

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction spline = interpolator.interpolate(truncatedTime, truncatedSignal);

        // Generate the uniform grid (from 0 to 100% in x points)
        double[] normalizedTime = new double[points];
        double minTime = truncatedTime[0];
        double maxTime = truncatedTime[length - 1];
        double step = (maxTime - minTime) / (points - 1);

        // Interpolate values of the signal onto the new grid
        double[] normalizedSignal = new double[points];
        for (int i = 0; i < points; i++) {
            normalizedTime[i] = minTime + i * step;
            normalizedSignal[i] = spline.value(normalizedTime[i]);
        }

        return normalizedSignal;
    }

    //plot segnale normalizzato + range
    private void plotNormalizedWithRange(int index) {
        // Mostra una finestra di dialogo per l'intervallo
        String rangeInput = JOptionPane.showInputDialog(
                this,
                "Enter the range of points (e.g., 0-200):",
                "Normalize Range",
                JOptionPane.PLAIN_MESSAGE
        );

        if (rangeInput == null || !rangeInput.matches("\\d+-\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid range format. Use 'start-end' format.");
            return;
        }

        // Parse range
        String[] parts = rangeInput.split("-");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);

        if (start < 0 || end >= timeData.length || start >= end) {
            JOptionPane.showMessageDialog(this, "Invalid range. Ensure 0 <= start < end < " + timeData.length);
            return;
        }

        // Mostra una finestra di dialogo per il numero di punti
        String pointsInput = JOptionPane.showInputDialog(
                this,
                "Enter the number of points for normalization:",
                "Number of Points",
                JOptionPane.PLAIN_MESSAGE
        );

        if (pointsInput == null || !pointsInput.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid number format. Enter a positive integer.");
            return;
        }

        int points = Integer.parseInt(pointsInput);
        if (points <= 1) {
            JOptionPane.showMessageDialog(this, "Number of points must be greater than 1.");
            return;
        }

        // Esegui la normalizzazione
        double[] emgSignal = emgData.get(index);
        double[] rangeTime = Arrays.copyOfRange(timeData, start, end + 1);
        double[] rangeSignal = Arrays.copyOfRange(emgSignal, start, end + 1);

        double[] normalizedSignal = normalizeToFixedLength(rangeTime, rangeSignal, points);

        // Crea un nuovo grafico in una nuova finestra
        XYSeries series = new XYSeries(emgLabels.get(index) + " (Normalized)");
        for (int i = 0; i < normalizedSignal.length; i++) {
            double percentage = (i / (double) (normalizedSignal.length - 1)) * 100;
            series.add(percentage, normalizedSignal[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Normalized EMG Signal (" + emgLabels.get(index) + ")",
                "% Time",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Visualizza il grafico in una nuova finestra
        JFrame graphFrame = new JFrame("Normalized EMG Signal - " + emgLabels.get(index));
        graphFrame.setSize(800, 600);
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphFrame.add(new ChartPanel(chart));
        graphFrame.setVisible(true);

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
    }

    //Metodo per visualizzare grafico per segnale emg filtrato + normalizzato
    private void plotNormalizedSignal(int index) {
        XYSeries series = new XYSeries(emgLabels.get(index));
        double[] emgSignal = emgData.get(index);

        // Normalizza l'intero segnale (numero di punti uguale alla lunghezza originale)
        double[] normalizedSignal = normalizeToFixedLength(timeData, emgSignal, emgSignal.length);

        // Usa la percentuale del tempo per l'asse x
        for (int i = 0; i < normalizedSignal.length; i++) {
            double percentage = (i / (double) (normalizedSignal.length - 1)) * 100;
            series.add(percentage, normalizedSignal[i]);
        }

        // Crea un nuovo grafico in una nuova finestra
        series = new XYSeries(emgLabels.get(index) + " (Normalized)");
        for (int i = 0; i < normalizedSignal.length; i++) {
            double percentage = (i / (double) (normalizedSignal.length - 1)) * 100;
            series.add(percentage, normalizedSignal[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Normalized EMG Signal (" + emgLabels.get(index) + ")",
                "% Time",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Visualizza il grafico in una nuova finestra
        JFrame graphFrame = new JFrame("Normalized EMG Signal - " + emgLabels.get(index));
        graphFrame.setSize(800, 600);
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphFrame.add(new ChartPanel(chart));
        graphFrame.setVisible(true);
    }


    private void plotComparison(int index) {
        // Mostra una finestra di dialogo per l'intervallo
        String rangeInput = JOptionPane.showInputDialog(
                this,
                "Enter the range of points for normalization (e.g., 0-200):",
                "Select Range for Comparison",
                JOptionPane.PLAIN_MESSAGE
        );

        if (rangeInput == null || !rangeInput.matches("\\d+-\\d+")) {
            JOptionPane.showMessageDialog(this, "Invalid range format. Use 'start-end' format.");
            return;
        }

        // Parse range
        String[] parts = rangeInput.split("-");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);

        if (start < 0 || end >= timeData.length || start >= end) {
            JOptionPane.showMessageDialog(this, "Invalid range. Ensure 0 <= start < end < " + timeData.length);
            return;
        }

        // Segnali da plottare
        double[] emgSignal = emgData.get(index);
        double[] rangeTime = Arrays.copyOfRange(timeData, start, end + 1);
        double[] rangeSignal = Arrays.copyOfRange(emgSignal, start, end + 1);
        double[] normalizedSignal = normalizeToFixedLength(rangeTime, rangeSignal, rangeSignal.length);

        // Crea il dataset per il confronto
        XYSeries originalSeries = new XYSeries("Original Signal");
        for (int i = start; i <= end; i++) {
            originalSeries.add(timeData[i], emgSignal[i]);
        }

        XYSeries normalizedSeries = new XYSeries("Normalized Signal");
        for (int i = 0; i < normalizedSignal.length; i++) {
            normalizedSeries.add(rangeTime[0] + i * (rangeTime[rangeTime.length - 1] - rangeTime[0]) / (normalizedSignal.length - 1), normalizedSignal[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(originalSeries);
        dataset.addSeries(normalizedSeries);

        // Crea il grafico
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparison of Original and Normalized Signals - " + emgLabels.get(index),
                "Time",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Visualizza il grafico in una nuova finestra
        JFrame graphFrame = new JFrame("Comparison - " + emgLabels.get(index));
        graphFrame.setSize(800, 600);
        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        graphFrame.add(new ChartPanel(chart));
        graphFrame.setVisible(true);
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

