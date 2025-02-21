import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GeneralPlotter extends JFrame {
    private XYSeriesCollection signals;
    private XYSeriesCollection pointSets;

    private XYPlot plot;
    private JFreeChart chart;

    private int signalsIndex, pointsIndex;

    public GeneralPlotter(String title, String xLabel, String yLabel, XYSeriesCollection signals, XYSeriesCollection pointSets) {
        super(title);

        this.signals = signals;
        this.pointSets = pointSets;

        signalsIndex = signals.getSeriesCount();
        pointsIndex = pointSets.getSeriesCount();

        makeChart(title, xLabel, yLabel, signals, pointSets);

        JPanel leftButtons = new JPanel();
        JButton signalsPrev = new JButton("<");
        JButton pointsPrev = new JButton("<");

        signalsPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signalsIndex--;
                if (signalsIndex < 0) {
                    signalsIndex = signals.getSeriesCount();
                }

                updateChart();
            }
        });
        pointsPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pointsIndex--;
                if (pointsIndex < 0) {
                    pointsIndex = signals.getSeriesCount();
                }

                updateChart();
            }
        });

        JPanel rightButtons = new JPanel();
        JButton signalsNext = new JButton(">");
        JButton pointsNext = new JButton(">");


        ChartPanel chartPanel = new ChartPanel(chart);
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

    }

    private void makeChart(String title, String xLabel, String yLabel, XYSeriesCollection signals, XYSeriesCollection pointSets) {
        plot = new XYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        if (pointSets != null) {
            XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
            ValueAxis scatterX = new NumberAxis(xLabel);
            ValueAxis scatterY = new NumberAxis(yLabel);

            plot.setDataset(0, this.pointSets);
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

            plot.setDataset(1, this.signals);
            plot.setRenderer(1, lineRenderer);
            plot.setDomainAxis(lineX);
            plot.setRangeAxis(lineY);
            plot.mapDatasetToDomainAxis(1, 0);
            plot.mapDatasetToRangeAxis(1, 0);
        }

        chart = new JFreeChart(
                title,
                null,
                plot,
                true
        );
    }

}
