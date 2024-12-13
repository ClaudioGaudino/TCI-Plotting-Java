import com.github.psambit9791.jdsp.filter.Butterworth;
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

        Config config1 = new Config(
                true,
                "",
                "data\\corsa1\\Accelerazioni_prova_1.emt", "data\\corsa1\\Angoli_prova_1.emt", "data\\corsa1\\Vel_ang_prova_1.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,false,
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
                true,false,
                true,
                false,
                false,
                true
        );

        Config config3 = new Config(
                true,
                "",
                "data\\corsa2\\Accelerazioni_prova_4.emt", "data\\corsa2\\Angoli_prova_4.emt", "data\\corsa2\\Velocita_angolari_prova_4.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,false,
                true,
                false, false, true
        );

        Config marco1 = new Config(
                false,
                "data\\marco\\1.csv",
                "","","",
                "Acc_X", "Acc_Y", "Acc_Z",
                "Euler_X", "Euler_Y", "Euler_Z",
                "Gyr_X", "Gyr_Y", "Gyr_Z",
                "PacketCounter",
                "", "", "",
                true, false,
                true,
                false, false, true
        );

        Config marco2 = new Config(
                false,
                "data\\marco\\2.csv",
                "","","",
                "Acc_X", "Acc_Y", "Acc_Z",
                "Euler_X", "Euler_Y", "Euler_Z",
                "Gyr_X", "Gyr_Y", "Gyr_Z",
                "PacketCounter",
                "", "", "",
                true, false,
                true,
                false, false, true
        );

        Config marco3 = new Config(
                false,
                "data\\marco\\3.csv",
                "","","",
                "Acc_X", "Acc_Y", "Acc_Z",
                "Euler_X", "Euler_Y", "Euler_Z",
                "Gyr_X", "Gyr_Y", "Gyr_Z",
                "PacketCounter",
                "", "", "",
                true, false,
                true,
                false, false, true
        );

        try {
            //ConfigGUI gui = new ConfigGUI();
            Config config = marco3;
            boolean filtered = true;
            Data data = CSVInterpeter.read_dataset(config, true);

            if (config.isFree())
                data.makeFree();
            if (filtered) {
                Butterworth b = new Butterworth(60);
                if (config.isUseAccMagnitude()) {
                    data.filter(Data.Axis.MAGNITUDE, Data.Type.ACCELERATION, b, 4, 10);
                }
                if (config.isUseAngVelMagnitude()) {
                    data.filter(Data.Axis.MAGNITUDE, Data.Type.ANG_VELOCITY, b, 4, 10);
                }

                if (config.isPlotX()) {
                    data.filter(Data.Axis.X, Data.Type.ACCELERATION, b, 4, 10);
                    data.filter(Data.Axis.X, Data.Type.ANG_VELOCITY, b, 4, 10);
                }
                if (config.isPlotY()) {
                    data.filter(Data.Axis.Y, Data.Type.ACCELERATION, b, 4, 10);
                    data.filter(Data.Axis.Y, Data.Type.ANG_VELOCITY, b, 4, 10);
                }
                if (config.isPlotZ()) {
                    data.filter(Data.Axis.Z, Data.Type.ACCELERATION, b, 4, 10);
                    data.filter(Data.Axis.Z, Data.Type.ANG_VELOCITY, b, 4, 10);
                }
            }

            //System.out.println(data.getAccMagnitude().get(1580));

            XYSeriesCollection[] dataset = data.getDataset(config);

            dataset[0].addSeries(constant("-G", dataset[0].getItemCount(0), -9.80665));
            dataset[0].addSeries(constant("+G", dataset[0].getItemCount(0), 9.80665));

            AccelerometerPlot p = new AccelerometerPlot(dataset[0], dataset[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static XYSeries constant(String key, int length, double value) {
        XYSeries constant = new XYSeries(key);
        for(int i = 0; i < length; i++) {
            constant.add(i, value);
        }
        return constant;
    }
}
