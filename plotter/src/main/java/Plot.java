import org.jfree.chart.*;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Plot extends JFrame {

    private List<DoublePoint> selectedPoints;
    private JFreeChart chart;
    private XYPlot xyPlot;

    public Plot(XYSeriesCollection dataset) {
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
    }
}
