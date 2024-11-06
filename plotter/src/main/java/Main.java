import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
    }
}
