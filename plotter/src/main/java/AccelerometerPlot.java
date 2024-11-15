import jdk.jfr.Event;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


public class AccelerometerPlot extends JFrame {
    public AccelerometerPlot(XYSeriesCollection datasetAcc, XYSeriesCollection datasetAngVel) {
        super("Selezione degli eventi");

        XYPlot xyPlotAcc = new XYPlot();
        XYPlot xyPlotAngVel = new XYPlot();
        CrosshairListener listener = new CrosshairListener();
        JButton saveButton = new JButton("Salva Punti Selezionati");

        XYSeries[] accContacts = EventIdentifier.getContactEvents(datasetAcc.getSeries(0), datasetAngVel.getSeries(0), true);
        XYSeriesCollection accContactsCollection = new XYSeriesCollection();
        accContactsCollection.addSeries(accContacts[0]);
        accContactsCollection.addSeries(accContacts[1]);

        plotSetup(xyPlotAcc, accContactsCollection, datasetAcc);

        XYSeries[] angContacts = EventIdentifier.getContactEvents(datasetAcc.getSeries(0), datasetAngVel.getSeries(0), false);
        XYSeriesCollection angContactsCollection = new XYSeriesCollection();
        angContactsCollection.addSeries(angContacts[0]);
        angContactsCollection.addSeries(angContacts[1]);

        plotSetup(xyPlotAngVel, angContactsCollection, datasetAngVel);

        //plotSetup(xyPlotAcc, listener.selectedPoints, datasetAcc);
        //plotSetup(xyPlotAngVel, listener.selectedPoints, datasetAngVel);

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

    private static void plotSetup(XYPlot plot, XYSeriesCollection contacts, XYSeriesCollection dataset) {
        XYItemRenderer scatterRenderer = new XYLineAndShapeRenderer(false, true);
        ValueAxis scatterX = new NumberAxis("X");
        ValueAxis scatterY = new NumberAxis("Y");

        plot.setDataset(0, contacts);
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
