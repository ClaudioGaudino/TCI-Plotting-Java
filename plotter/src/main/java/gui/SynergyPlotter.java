package gui;

import data.NMFResult;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SynergyPlotter extends JFrame {
    private NMFResult extracted;
    private String title;
    private List<String> headers;

    public SynergyPlotter(NMFResult extracted, String title, List<String> headers) {
        super(title);

        this.extracted = extracted;
        this.title = title;
        this.headers = headers;

        JPanel chartContainer = new JPanel();
        chartContainer.setLayout(new BoxLayout(chartContainer, BoxLayout.Y_AXIS));

        setSize(1170, 900);

        int chartWidth = getWidth() / 2 - 30; // account for spacing/margin
        int chartHeight = 250; // fixed height for clarity


        for (int i = 0; i < extracted.k(); i++) {
            JPanel pairPanel = new JPanel();
            pairPanel.setLayout(new BoxLayout(pairPanel, BoxLayout.X_AXIS));
            pairPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JFreeChart barChart = createBarPlot(i);
            JFreeChart lineChart = createLinePlot(i);

            ChartPanel barPanel = new ChartPanel(barChart);
            ChartPanel linePanel = new ChartPanel(lineChart);

            barPanel.setPreferredSize(new Dimension(chartWidth, chartHeight));
            linePanel.setPreferredSize(new Dimension(chartWidth, chartHeight));

            pairPanel.add(barPanel);
            pairPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacer
            pairPanel.add(linePanel);

            pairPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
            chartContainer.add(pairPanel);
            chartContainer.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer between rows
        }

        JScrollPane scrollPane = new JScrollPane(chartContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane);


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private JFreeChart createBarPlot(int i) {
        String title = "Synergy #" + (i + 1);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int j = 0; j < extracted.w().length; j++) {
            StringBuilder result = new StringBuilder();
            for (char c : headers.get(j + 2).toCharArray()) {
                if (Character.isUpperCase(c)) {
                    result.append(c);
                }
            }

            dataset.addValue(extracted.w()[j][i], "series", result.toString());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Muscle", "Weight", dataset,
                PlotOrientation.VERTICAL, false, true, false
        );

        CategoryPlot plot = chart.getCategoryPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(0.0, 1.0);

        return chart;
    }

    private JFreeChart createLinePlot(int i) {
        String title = "Activation #" + (i + 1);
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries activationCoeff = new XYSeries("ac");
        double progress;

        for (int j = 0; j < extracted.h()[0].length; j++) {
            progress = (j * 100.0) / extracted.h()[0].length;
            activationCoeff.add(progress, extracted.h()[i][j]);
        }

        dataset.addSeries(activationCoeff);

        return ChartFactory.createXYLineChart(
                title, "Time (% of cycle)" ,"Activation",
                dataset, PlotOrientation.VERTICAL, false, true, false
        );
    }
}
