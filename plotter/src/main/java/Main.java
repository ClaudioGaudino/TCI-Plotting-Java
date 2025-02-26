import com.github.psambit9791.jdsp.filter.Butterworth;
import enums.PaddleType;
import enums.Side;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
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
                true, ""
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
                true, ""
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
                false, false, true, ""
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
                false, false, true, ""
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
                false, false, true, ""
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
                false, false, true, ""
        );

        Config pagCentro1 = new Config(
                true,
                "",
                "data\\pagaiata\\centro1\\Pagaiata_centro_accelerazioni.emt", "data\\pagaiata\\centro1\\Pagaiata_centro_angoli.emt", "data\\pagaiata\\centro1\\Pagaiata_centro_velocità_angolari.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,false,
                true,
                false, false, true,
                "data\\EMGs.csv"
        );

        Config pagEstremita1 = new Config(
                true,
                "",
                "data\\pagaiata\\estremita1\\Pagaiata_estremità_accelerazioni.emt", "data\\pagaiata\\estremita1\\Pagaiata_estremità_angoli.emt", "data\\pagaiata\\estremita1\\Pagaiata_estremità_velocità_angolari.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                true,false,
                true,
                false, false, true, ""
        );

        try {
            Config config = pagCentro1;
            boolean filtered = true;
            AccelerometerData accelerometerData = CSVInterpeter.readAccelerometerData(config, true);

            if (config.free())
                accelerometerData.makeFree();
            if (filtered) {
                Butterworth b = new Butterworth(100);
                if (config.useAccMagnitude()) {
                    accelerometerData.filter(AccelerometerData.Axis.MAGNITUDE, AccelerometerData.Type.ACCELERATION, b, 4, 10);
                }
                if (config.useAngVelMagnitude()) {
                    accelerometerData.filter(AccelerometerData.Axis.MAGNITUDE, AccelerometerData.Type.ANG_VELOCITY, b, 4, 10);
                }

                if (config.plotX()) {
                    accelerometerData.filter(AccelerometerData.Axis.X, AccelerometerData.Type.ACCELERATION, b, 4, 10);
                    accelerometerData.filter(AccelerometerData.Axis.X, AccelerometerData.Type.ANG_VELOCITY, b, 4, 10);
                }
                if (config.plotY()) {
                    accelerometerData.filter(AccelerometerData.Axis.Y, AccelerometerData.Type.ACCELERATION, b, 4, 10);
                    accelerometerData.filter(AccelerometerData.Axis.Y, AccelerometerData.Type.ANG_VELOCITY, b, 4, 10);
                }
                if (config.plotZ()) {
                    accelerometerData.filter(AccelerometerData.Axis.Z, AccelerometerData.Type.ACCELERATION, b, 4, 10);
                    accelerometerData.filter(AccelerometerData.Axis.Z, AccelerometerData.Type.ANG_VELOCITY, b, 4, 6);
                }
            }

            XYSeriesCollection[] dataset = accelerometerData.getDataset(config);

            List<PaddleEvent> events = EventIdentifier.getPaddlingEvents(accelerometerData.getFreeAngVelZ(), 1, -10, 10);
            List<DataPair<Double, Double>> separators = new ArrayList<>();

            for (int i = 0; i < events.size() - 1; i += 2) {
                separators.add(new DataPair<>((double) events.get(i).frame(), (double) events.get(i + 1).frame()));
            }

            XYSeriesCollection eventCollection = makeEventCollection(events, accelerometerData);

            GeneralPlotter plotter = new GeneralPlotter("Plot", "Frame", "Ampl", dataset[1], eventCollection, separators);

            EMGData emgData = CSVInterpeter.readEMGData(config);
            emgData.filter();

            XYSeriesCollection emgSignals = new XYSeriesCollection();
            for (int i = 2; i < emgData.getSignals().size(); i++) {
                XYSeries tmp = new XYSeries(emgData.getHeaders().get(i));

                for (double frame : emgData.getSignals().get(0)) {
                    tmp.add(frame, emgData.getFilteredSignals().get(i).get((int) frame));
                }

                emgSignals.addSeries(tmp);
            }

            List<DataPair<Double, Double>> separatorsEMG = new ArrayList<>();
            for (DataPair<Double, Double> sep : separators) {
                separatorsEMG.add(new DataPair<>(sep.a() * 10, sep.b() * 10));
            }

            GeneralPlotter emgPlotter = new GeneralPlotter("Emg", "Frame", "Ampl", emgSignals, null, separatorsEMG);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static XYSeriesCollection makeEventCollection(List<PaddleEvent> events, AccelerometerData data) {
        XYSeriesCollection eventCollection = new XYSeriesCollection();
        XYSeries leftHits = new XYSeries("Left Hits");
        XYSeries rightHits = new XYSeries("Right Hits");
        XYSeries leftLeaves = new XYSeries("Left Leaves");
        XYSeries rightLeaves = new XYSeries("Right Leaves");

        double i, value;

        for (PaddleEvent event : events) {
            i = event.frame();
            value = data.getFreeAngVelZ().get(event.frame());
            if (event.side() == Side.RIGHT) {
                if (event.type() == PaddleType.HIT)
                    rightHits.add(i, value);
                else
                    rightLeaves.add(i, value);
            }
            else {
                if (event.type() == PaddleType.HIT)
                    leftHits.add(i, value);
                else
                    leftLeaves.add(i, value);
            }
        }

        eventCollection.addSeries(leftHits);
        eventCollection.addSeries(rightHits);
        eventCollection.addSeries(leftLeaves);
        eventCollection.addSeries(rightLeaves);
        return eventCollection;
    }
}
