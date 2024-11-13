import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Main {
    public static void main(String[] args) {
        String path = "data\\accelerazione.csv";

        Config config = new Config(
                false,
                "data\\lean_on_faces.csv",
                "","","",
                "Acc_X", "Acc_Y", "Acc_Z",
                "Euler_X", "Euler_Y", "Euler_Z",
                "Gyr_X", "Gyr_Y", "Gyr_Z",
                "PacketCounter",
                "", "", "",
                true,
                true,
                true,
                true
        );

        try {
            XYSeriesCollection[] dataset = CSVInterpeter.read_dataset(config);

            Plot p = new Plot(dataset[0], dataset[1]);

        } catch (Exception e) {
            System.err.println("Some bad thing happen:");
            e.printStackTrace();
        }
    }
}
