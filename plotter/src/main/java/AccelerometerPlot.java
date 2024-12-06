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
        xyPlotAcc.setDomainPannable(true);
        xyPlotAcc.setRangePannable(true);
        XYPlot xyPlotAngVel = new XYPlot();
        xyPlotAngVel.setDomainPannable(true);
        xyPlotAngVel.setRangePannable(true);
        CrosshairListener listener = new CrosshairListener();


        //XYSeries[] accContacts = EventIdentifier.getContactEvents(datasetAcc.getSeries(0), datasetAngVel.getSeries(0), true);
        XYSeries[] accContacts = EventIdentifier.getContactEvents(datasetAcc.getSeries(0), 3, 0.2, 8);
        XYSeriesCollection accContactsCollection = new XYSeriesCollection();
        for (XYSeries series : accContacts) {
            accContactsCollection.addSeries(series);
        }

        plotSetup(xyPlotAcc, accContactsCollection, datasetAcc);

        XYSeries[] angContacts = EventIdentifier.getContactEvents(datasetAcc.getSeries(0), datasetAngVel.getSeries(0), false);
        XYSeriesCollection angContactsCollection = new XYSeriesCollection();
        for (XYSeries series : angContacts) {
            angContactsCollection.addSeries(series);
        }

        plotSetup(xyPlotAngVel, angContactsCollection, datasetAngVel);

        JButton saveButton = new JButton("Salva " + "(accContacts[0].getItemCount() + accContacts[1].getItemCount())" + " Eventi");

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


        ChartPanel chartPanelAngVel = new ChartPanel(chartAngVel);
        chartPanelAngVel.setPreferredSize(new Dimension(800, 600));
        chartPanelAcc.addChartMouseListener(listener);
        chartAngVel.addProgressListener(listener);


        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CSVInterpeter.write_contacts(accContacts, angContacts, "results/contacts.csv");
                    System.out.println("Salvataggio compiuto");
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
