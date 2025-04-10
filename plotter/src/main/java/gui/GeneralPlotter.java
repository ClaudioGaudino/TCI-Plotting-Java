package gui;

import data.DataPair;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class GeneralPlotter extends JFrame {
    private String title, xLabel, yLabel;
    private XYSeriesCollection signals;
    private XYSeriesCollection pointSets;
    private List<DataPair<Double, Double>> slices;
    private List<Double> separators = null;
    private DataPair<Double, Double> yRange;
    private ChartPanel chartPanel;

    private int signalsIndex, pointsIndex, sliceIndex;

    public GeneralPlotter(
            String title,
            String xLabel,
            String yLabel,
            XYSeriesCollection signals,
            XYSeriesCollection pointSets,
            List<DataPair<Double, Double>> slices,
            DataPair<Double, Double> yRange
    ) {
        super(title);

        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.signals = signals;
        this.pointSets = pointSets;
        this.slices = slices;
        this.yRange = yRange;

        signalsIndex = signals == null ? 0 : signals.getSeriesCount();
        pointsIndex = pointSets == null ? 0 : pointSets.getSeriesCount();
        sliceIndex = slices == null ? 0 : slices.size();

        if (slices != null && !slices.isEmpty()) {
            separators = new ArrayList<>();
            for (DataPair<Double, Double> slice : slices) {
                separators.add(slice.a());
                separators.add(slice.b());
            }
        }

        init();
    }

    public GeneralPlotter(
            String title,
            String xLabel,
            String yLabel,
            XYSeriesCollection signals,
            XYSeriesCollection pointSets,
            List<Double> separators,
            DataPair<Double, Double> yRange,
            boolean diff
    ) {
        super(title);

        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.signals = signals;
        this.pointSets = pointSets;
        this.separators = separators;
        this.yRange = yRange;
        this.slices = null;

        init();
    }

    private void init() {
        JFreeChart chart = makeChart(signals, pointSets, separators);

        JPanel leftButtons = new JPanel();
        JButton signalsPrev = new JButton("<");
        JButton pointsPrev = new JButton("<");
        JButton slicePrev = new JButton("<");

        signalsPrev.addActionListener(e -> {
            if (this.signals == null || this.signals.getSeriesCount() == 0) return;

            signalsIndex--;
            signalsIndex = signalsIndex % (this.signals.getSeriesCount() + 1);
            if (signalsIndex < 0) signalsIndex = this.signals.getSeriesCount();

            System.out.println("Cycling to signal " + signalsIndex + " (prev)");

            updateChart();
        });

        pointsPrev.addActionListener(e -> {
            if (this.pointSets == null || this.pointSets.getSeriesCount() == 0) return;

            pointsIndex--;
            pointsIndex = pointsIndex % (this.pointSets.getSeriesCount() + 1);
            if (pointsIndex < 0) pointsIndex = this.pointSets.getSeriesCount();

            System.out.println("Cycling to set " + pointsIndex + " (prev)");

            updateChart();
        });

        slicePrev.addActionListener(e -> {
            if (this.slices == null || this.slices.isEmpty()) return;

            sliceIndex--;
            sliceIndex = sliceIndex % (this.slices.size() + 1);
            if (sliceIndex < 0) sliceIndex = this.slices.size();

            System.out.println("Cycling to slice " + sliceIndex + " (prev)");

            updateChart();
        });

        JPanel rightButtons = new JPanel();
        JButton signalsNext = new JButton(">");
        JButton pointsNext = new JButton(">");
        JButton sliceNext = new JButton(">");

        signalsNext.addActionListener(e -> {
            if (this.signals == null || this.signals.getSeriesCount() == 0) return;

            signalsIndex++;
            signalsIndex = signalsIndex % (this.signals.getSeriesCount() + 1);

            System.out.println("Cycling to signal " + signalsIndex + " (next)");

            updateChart();
        });

        pointsNext.addActionListener(e -> {
            if (this.pointSets == null || this.pointSets.getSeriesCount() == 0) return;

            pointsIndex++;
            pointsIndex = pointsIndex % (this.pointSets.getSeriesCount() + 1);

            System.out.println("Cycling to set " + pointsIndex + " (next)");

            updateChart();
        });

        sliceNext.addActionListener(e -> {
            if(this.slices == null || this.slices.isEmpty()) return;

            sliceIndex++;
            sliceIndex = sliceIndex % (this.slices.size() + 1);

            System.out.println("Cycling to slice " + sliceIndex + " (next)");

            updateChart();
        });

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.Y_AXIS));
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());

        leftButtons.add(signalsPrev);
        leftButtons.add(pointsPrev);
        leftButtons.add(slicePrev);

        rightButtons.add(signalsNext);
        rightButtons.add(pointsNext);
        rightButtons.add(sliceNext);

        add(chartPanel, BorderLayout.CENTER);
        add(leftButtons, BorderLayout.WEST);
        add(rightButtons, BorderLayout.EAST);
        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void updateChart() {
        SwingUtilities.invokeLater(() -> {
            XYSeriesCollection drawnSignals, drawnPoints;
            List<Double> separators;
            boolean doSlicing = false;

            if (slices == null || sliceIndex == slices.size()) {
                separators = this.separators;
            }
            else {
                separators = null;
                doSlicing = true;
            }

            drawnSignals = getDrawnSeries(doSlicing, signals, signalsIndex);

            drawnPoints = getDrawnSeries(doSlicing, pointSets, pointsIndex);

            JFreeChart chart = makeChart(drawnSignals, drawnPoints, separators);
            chartPanel.setChart(chart);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private XYSeriesCollection getDrawnSeries(boolean doSlicing, XYSeriesCollection collection, int index) {
        XYSeriesCollection drawnSignals;
        XYSeries tmp;

        if (collection == null || index == collection.getSeriesCount()) {
            if (doSlicing && collection != null) {
                drawnSignals = new XYSeriesCollection();

                for (int i = 0; i < collection.getSeriesCount(); i++) {
                    tmp = collection.getSeries(i);
                    drawnSignals.addSeries(sliceSeries(slices.get(sliceIndex).a(), slices.get(sliceIndex).b(), tmp));
                }
            }
            else {
                drawnSignals = collection;
            }
        } else {
            drawnSignals = new XYSeriesCollection();

            for (int i = 0; i < collection.getSeriesCount(); i++) {
                tmp = (i == index) ? collection.getSeries(i) : new XYSeries(collection.getSeriesKey(i));

                if (doSlicing && i == index) {
                    tmp = sliceSeries(slices.get(sliceIndex).a(), slices.get(sliceIndex).b(), tmp);
                }

                drawnSignals.addSeries(tmp);
            }
        }
        return drawnSignals;
    }

    private JFreeChart makeChart(XYSeriesCollection signals, XYSeriesCollection pointSets, List<Double> separators) {
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        double minXSignals = 0, maxXSignals = 0;

        if (signals != null && signals.getSeriesCount() != 0) {
            XYItemRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
            ValueAxis lineX = new NumberAxis(xLabel);
            ValueAxis lineY = new NumberAxis(yLabel);

            if (yRange != null) {
                lineY.setRange(yRange.a(), yRange.b());
            }

            boolean first = true;
            XYSeries curr;

            for (int i = 0; i < signals.getSeriesCount(); i++) {
                curr = signals.getSeries(i);

                if (curr.getItemCount() != 0) {
                    if (first) {
                        first = false;
                        maxXSignals = curr.getMaxX();
                        minXSignals = curr.getMinX();
                        continue;
                    }

                    if (minXSignals > curr.getMinX())
                        minXSignals = curr.getMinX();
                    if (maxXSignals < curr.getMaxX())
                        maxXSignals = curr.getMaxX();
                }
            }

            lineX.setRange(minXSignals, maxXSignals);

            plot.setDataset(1, signals);
            plot.setRenderer(1, lineRenderer);
            plot.setDomainAxis(lineX);
            plot.setRangeAxis(lineY);
            plot.mapDatasetToDomainAxis(1, 0);
            plot.mapDatasetToRangeAxis(1, 0);
        }

        if (pointSets != null && pointSets.getSeriesCount() != 0) {
            XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
            ValueAxis scatterX = new NumberAxis(xLabel);
            ValueAxis scatterY = new NumberAxis(yLabel);

            if (yRange != null) {
                scatterY.setRange(yRange.a(), yRange.b());
            }

            boolean first = true;
            double minX = 0, maxX = 0;
            XYSeries curr;

            for (int i = 0; i < pointSets.getSeriesCount(); i++) {
                curr = pointSets.getSeries(i);

                if (curr.getItemCount() != 0) {
                    if (first) {
                        first = false;
                        maxX = curr.getMaxX();
                        minX = curr.getMinX();
                        continue;
                    }

                    if (minX > curr.getMinX())
                        minX = curr.getMinX();
                    if (maxX < curr.getMaxX())
                        maxX = curr.getMaxX();
                }
            }

            if (minX == maxX)
                scatterX.setRange(minXSignals, maxXSignals);
            else
                scatterX.setRange(minX, maxX);

            plot.setDataset(0, pointSets);
            plot.setRenderer(0, scatterRenderer);
            plot.setDomainAxis(0, scatterX);
            plot.setRangeAxis(0, scatterY);
            plot.mapDatasetToDomainAxis(0, 0);
            plot.mapDatasetToRangeAxis(0, 0);
        }

        if (separators != null) {
            for (Double value : separators) {
                plot.addAnnotation(new XYLineAnnotation(
                        value, plot.getRangeAxis().getLowerBound(),
                        value, plot.getRangeAxis().getUpperBound(),
                        new BasicStroke(1),
                        Color.GRAY
                ));
            }
        }

        return new JFreeChart(
                title,
                null,
                plot,
                true
        );
    }

    private XYSeries sliceSeries(double start, double end, XYSeries series) {
        if (start < 0)
            throw new IllegalArgumentException("Splice values (" + start + ", " + end + ") out of bounds for series " + series.getKey() + " (" + series.getMaxX() + " max X)");

        XYSeries slice = new XYSeries(series.getKey());
        double x, y;
        for (int i = 0; i < series.getItemCount(); i++) {
            x = (double) series.getX(i);
            y = (double) series.getY(i);

            if (x >= start && x <= end)
                slice.add(x, y);
        }

        return slice;
    }

}
