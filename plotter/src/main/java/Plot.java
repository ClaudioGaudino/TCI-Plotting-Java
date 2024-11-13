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

import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.List;

public class Plot extends JFrame {

    private XYSeries selectedPoints;

    public Plot(XYSeriesCollection datasetAcc, XYSeriesCollection datasetAngVel) {
        super("Selezione degli eventi");

        XYPlot xyPlotAcc = new XYPlot();
        XYPlot xyPlotAngVel = new XYPlot();
        CrosshairListener listener = new CrosshairListener();
        JButton saveButton = new JButton("Salva Punti Selezionati");

        plotSetup(xyPlotAcc, listener.selectedPoints, datasetAcc);
        plotSetup(xyPlotAngVel, listener.selectedPoints, datasetAngVel);

        JFreeChart chartAcc = new JFreeChart(
                "Accelerazione",
                null,
                xyPlotAcc,
                true
        );
        JFreeChart chartAngVel = new JFreeChart(
                "Velocità angolare",
                null,
                xyPlotAngVel,
                true
        );

        ChartPanel chartPanelAcc = new ChartPanel(chartAcc);
        chartPanelAcc.setPreferredSize(new Dimension(800, 600));
        chartPanelAcc.addChartMouseListener(listener);
        chartAcc.addProgressListener(listener);
        chartAcc.addProgressListener(new ChartProgressListener() {
            @Override
            public void chartProgress(ChartProgressEvent chartProgressEvent) {
                if (chartProgressEvent.getType() == ChartProgressEvent.DRAWING_FINISHED) {
                    saveButton.setText("Salva " + listener.selectedPoints.getItemCount() + " eventi");
                }
            }
        });

        ChartPanel chartPanelAngVel = new ChartPanel(chartAngVel);
        chartPanelAngVel.setPreferredSize(new Dimension(800, 600));
        chartPanelAcc.addChartMouseListener(listener);
        chartAngVel.addProgressListener(listener);


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

        add(chartPanelAcc, BorderLayout.WEST);
        add(chartPanelAngVel, BorderLayout.EAST);
        add(saveButton, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private static void plotSetup(XYPlot plot, XYSeries selectedPoints, XYSeriesCollection dataset) {
        XYSeriesCollection scatterData = new XYSeriesCollection();
        scatterData.addSeries(selectedPoints);
        XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
        ValueAxis scatterX = new NumberAxis("X");
        ValueAxis scatterY = new NumberAxis("Y");

        plot.setDataset(0, scatterData);
        plot.setRenderer(0, scatterRenderer);
        plot.setDomainAxis(0, scatterX);
        plot.setRangeAxis(0, scatterY);
        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToRangeAxis(0, 0);

        XYItemRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);
        ValueAxis lineX = new NumberAxis("X");
        ValueAxis lineY = new NumberAxis("Y");

        plot.setDataset(1, dataset);
        plot.setRenderer(1, lineRenderer);
        plot.setDomainAxis(lineX);
        plot.setRangeAxis(lineY);
        plot.mapDatasetToDomainAxis(1, 0);
        plot.mapDatasetToRangeAxis(1, 0);
    }
}
