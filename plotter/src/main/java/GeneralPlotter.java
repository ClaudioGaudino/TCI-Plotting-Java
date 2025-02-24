import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneralPlotter extends JFrame {
    private String title, xLabel, yLabel;
    private XYSeriesCollection signals;
    private XYSeriesCollection pointSets;
    private ChartPanel chartPanel;

    private int signalsIndex, pointsIndex;

    public GeneralPlotter(String title, String xLabel, String yLabel, XYSeriesCollection signals, XYSeriesCollection pointSets) {
        super(title);

        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.signals = signals;
        this.pointSets = pointSets;

        signalsIndex = signals == null ? 0 : signals.getSeriesCount();
        pointsIndex = pointSets == null ? 0 : pointSets.getSeriesCount();

        JFreeChart chart = makeChart(signals, pointSets);

        JPanel leftButtons = new JPanel();
        JButton signalsPrev = new JButton("<");
        JButton pointsPrev = new JButton("<");

        signalsPrev.addActionListener(e -> {
            if (signals == null || signals.getSeriesCount() == 0) return;

            signalsIndex--;
            signalsIndex = signalsIndex % (signals.getSeriesCount() + 1);
            if (signalsIndex < 0) signalsIndex = signals.getSeriesCount();

            System.out.println("Cycling to signal " + signalsIndex + " (prev)");

            updateChart();
        });
        pointsPrev.addActionListener(e -> {
            if (pointSets == null || pointSets.getSeriesCount() == 0) return;

            pointsIndex--;
            pointsIndex = pointsIndex % (pointSets.getSeriesCount() + 1);
            if (pointsIndex < 0) pointsIndex = pointSets.getSeriesCount();

            System.out.println("Cycling to set " + pointsIndex + " (prev)");

            updateChart();
        });

        JPanel rightButtons = new JPanel();
        JButton signalsNext = new JButton(">");
        JButton pointsNext = new JButton(">");

        signalsNext.addActionListener(e -> {
            if (signals == null || signals.getSeriesCount() == 0) return;

            signalsIndex++;
            signalsIndex = signalsIndex % (signals.getSeriesCount() + 1);

            System.out.println("Cycling to signal " + signalsIndex + " (next)");

            updateChart();
        });

        pointsNext.addActionListener(e -> {
            if (pointSets == null || pointSets.getSeriesCount() == 0) return;

            pointsIndex++;
            pointsIndex = pointsIndex % (pointSets.getSeriesCount() + 1);

            System.out.println("Cycling to set " + pointsIndex + " (next)");

            updateChart();
        });

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.Y_AXIS));
        rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());

        leftButtons.add(signalsPrev);
        leftButtons.add(pointsPrev);

        rightButtons.add(signalsNext);
        rightButtons.add(pointsNext);

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

            XYSeries tmp;

            if (signals == null || signalsIndex == signals.getSeriesCount()) {
                drawnSignals = signals;
            } else {
                drawnSignals = new XYSeriesCollection();

                for (int i = 0; i < signals.getSeriesCount(); i++) {
                     tmp = i == signalsIndex ? signals.getSeries(i) : new XYSeries(signals.getSeriesKey(i));
                     drawnSignals.addSeries(tmp);
                }
            }

            if (pointSets == null || pointsIndex == pointSets.getSeriesCount()) {
                drawnPoints = pointSets;
            } else {
                drawnPoints = new XYSeriesCollection();

                for (int i = 0; i < pointSets.getSeriesCount(); i++) {
                    tmp = i == pointsIndex ? pointSets.getSeries(i) : new XYSeries(pointSets.getSeriesKey(i));
                    drawnPoints.addSeries(tmp);
                }
            }

            JFreeChart chart = makeChart(drawnSignals, drawnPoints);
            chartPanel.setChart(chart);
            chartPanel.revalidate();
            chartPanel.repaint();
        });
    }

    private JFreeChart makeChart(XYSeriesCollection signals, XYSeriesCollection pointSets) {
        XYPlot plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        if (pointSets != null) {
            XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
            ValueAxis scatterX = new NumberAxis(xLabel);
            ValueAxis scatterY = new NumberAxis(yLabel);

            plot.setDataset(0, pointSets);
            plot.setRenderer(0, scatterRenderer);
            plot.setDomainAxis(0, scatterX);
            plot.setRangeAxis(0, scatterY);
            plot.mapDatasetToDomainAxis(0, 0);
            plot.mapDatasetToRangeAxis(0, 0);
        }

        if (signals != null) {
            XYItemRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
            ValueAxis lineX = new NumberAxis(xLabel);
            ValueAxis lineY = new NumberAxis(yLabel);

            plot.setDataset(1, signals);
            plot.setRenderer(1, lineRenderer);
            plot.setDomainAxis(lineX);
            plot.setRangeAxis(lineY);
            plot.mapDatasetToDomainAxis(1, 0);
            plot.mapDatasetToRangeAxis(1, 0);
        }

        return new JFreeChart(
                title,
                null,
                plot,
                true
        );
    }

}
