import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

public class Plot extends JFrame {

    private XYSeries selectedPoints;
    private JFreeChart chart;
    private XYPlot xyPlot;

    /*public Plot(XYSeriesCollection dataset) {
        super("Clickable Plot");

        chart = ChartFactory.createXYLineChart(
                "Acceleration",
                "Frame",
                "Acceleration",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        xyPlot = chart.getXYPlot();

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        CrosshairListener listener = new CrosshairListener(xyPlot);

        chartPanel.addChartMouseListener(listener);
        chart.addProgressListener(listener);

        add(chartPanel, BorderLayout.CENTER);

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }*/

    public Plot(XYSeriesCollection dataset) {
        super("clickable");

        xyPlot = new XYPlot();
        CrosshairListener listener = new CrosshairListener(xyPlot);
        JButton saveButton = new JButton("Salva Punti Selezionati");

        XYSeriesCollection scatterData = new XYSeriesCollection();
        scatterData.addSeries(listener.selectedPoints);
        XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
        ValueAxis scatterX = new NumberAxis("X");
        ValueAxis scatterY = new NumberAxis("Y");

        xyPlot.setDataset(0, scatterData);
        xyPlot.setRenderer(0, scatterRenderer);
        xyPlot.setDomainAxis(0, scatterX);
        xyPlot.setRangeAxis(0, scatterY);
        xyPlot.mapDatasetToDomainAxis(0, 0);
        xyPlot.mapDatasetToRangeAxis(0, 0);

        XYItemRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
        ValueAxis lineX = new NumberAxis("X");
        ValueAxis lineY = new NumberAxis("Y");

        xyPlot.setDataset(1, dataset);
        xyPlot.setRenderer(1, lineRenderer);
        xyPlot.setDomainAxis(lineX);
        xyPlot.setRangeAxis(lineY);
        xyPlot.mapDatasetToDomainAxis(1, 0);
        xyPlot.mapDatasetToRangeAxis(1, 0);

        chart = new JFreeChart(
                "chartname",
                null,
                xyPlot,
                true
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        chartPanel.addChartMouseListener(listener);
        chart.addProgressListener(listener);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CSVInterpeter.write_series_to_csv(listener.selectedPoints, "results/selected_points.csv", "time", "Y");
                    System.out.println("Saving complete");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        add(chartPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
