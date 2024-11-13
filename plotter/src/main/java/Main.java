import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String path = "data\\accelerazione.csv";

        try {
            XYSeries xAcc = CSVInterpeter.make_series("data\\lean_on_faces.csv", "PacketCounter", "Acc_X", "xAcc");
            //XYSeries yAcc = CSVInterpeter.make_series("data\\lean_on_faces.csv", "PacketCounter", "Acc_Y", "yAcc");
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(xAcc);
            //dataset.addSeries(yAcc);

            Plot p = new Plot(dataset);

        } catch (Exception e) {
            System.err.println("Some bad thing happen:");
            e.printStackTrace();
        }
        //Conversione file emt to csv elettromiografia
        EMTtoCSVConverter converter = new EMTtoCSVConverter();

        String inputFilePath = "data\\EMGs.emt";
        String outputFilePath = "data\\EMGs.csv";

        try {
            converter.convert(inputFilePath, outputFilePath);
            System.out.println("Conversione completata. File CSV generato in: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Errore durante la conversione: " + e.getMessage());
        }

        //Plot Elettromiografia
        String csvFilePath = "data\\EMGs.csv"; // Il file CSV generato
        SwingUtilities.invokeLater(() -> {
            EMGDataPlotter plotter = new EMGDataPlotter(csvFilePath);
            plotter.setSize(800, 600);
            plotter.setLocationRelativeTo(null);
            plotter.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            plotter.setVisible(true);
        });
    }
}
