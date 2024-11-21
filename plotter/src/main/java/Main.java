import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Main {
    public static void main(String[] args) {
        String path = "data\\accelerazione.csv";

        /*Config config = new Config(
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
        );*/

        Config config = new Config(
                true,
                "",
                "data\\corsa1\\Accelerazioni_prova_1.emt", "data\\corsa1\\Angoli_prova_1.emt", "data\\corsa1\\Vel_ang_prova_1.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,
                false,
                false,
                true
        );

        Config config2 = new Config(
                true,
                "",
                "data\\accelerazione.emt", "data\\angoli.emt", "data\\vel_ang.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,
                false,
                false,
                true
        );

        try {
            //ConfigGUI gui = new ConfigGUI();
            XYSeriesCollection[] dataset = CSVInterpeter.read_dataset(config, true);
            AccelerometerPlot p = new AccelerometerPlot(dataset[0], dataset[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
