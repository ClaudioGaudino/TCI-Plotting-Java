import com.github.psambit9791.jdsp.filter.Butterworth;
import enums.PaddleType;
import enums.Side;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.IOException;
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

        Config corsa3 = new Config(
                true,
                "",
                "data\\corsa3\\Accelerazioni.emt", "data\\corsa3\\Angoli.emt", "data\\corsa3\\Velocità angolari.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "","","",
                true, false, true,
                false, false, true, "data\\corsa3\\Segnali EMG.emt"
        );

        try {
            Config config = pagCentro1;
            boolean filtered = true;
            boolean doRunningInstead = false;
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

            if (doRunningInstead) {
                doRunning(config, accelerometerData);
            } else {
                doPaddling(config, accelerometerData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doPaddling(Config config, AccelerometerData accelerometerData) throws IOException {
        XYSeriesCollection[] dataset = accelerometerData.getDataset(config);

        //GETTING EVENTS
        List<PaddleEvent> events = EventIdentifier.getPaddlingEvents(accelerometerData.getFreeAngVelZ(), 1, -10, 10, false);
        List<DataPair<Double, Double>> separators = new ArrayList<>();
        Side initialSide;
        //remove possible false positive events like a starting leave or ending hit
        if (events.get(0).type() == PaddleType.LEAVE) {
            events.remove(0);
        }
        if (events.get(events.size() - 1).type() == PaddleType.HIT) {
            events.remove(events.size() - 1);
        }

        initialSide = events.get(0).side();

        //generate separators for display purposes
        for (int i = 0; i < events.size() - 1; i += 2) {
            separators.add(new DataPair<>((double) events.get(i).frame(), (double) events.get(i + 1).frame()));
        }

        XYSeriesCollection eventCollection = makeEventCollection(events, accelerometerData);

        GeneralPlotter plotter = new GeneralPlotter("Events", "Frame", "Ampl", dataset[1], eventCollection, separators, null);

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
            //separatorsEMG.add(new DataPair<>(sep.a() * 10, sep.b() * 10));
            separatorsEMG.add(sep);
        }

        GeneralPlotter emgPlotter = new GeneralPlotter("Emg", "Frame", "Ampl", emgSignals, null, separatorsEMG, null);

        int spliceSize = 200;
        double[][][] splices = DataProcessor.spliceAndResampleEMG(emgData.getFilteredSignals(), separatorsEMG, spliceSize);
        double[][][] normalized = DataProcessor.joinSidesAndNormalizeEMG(splices, initialSide);

        XYSeriesCollection leftPaddles = new XYSeriesCollection();
        for (int i = 0; i < normalized[0].length; i++) {
            XYSeries tmp = new XYSeries(emgData.getHeaders().get(i + 2));

            for (int j = 0; j < normalized[0][i].length; j++) {
                tmp.add((double) j / spliceSize, normalized[0][i][j]);
            }

            leftPaddles.addSeries(tmp);
        }

        XYSeriesCollection rightPaddles = new XYSeriesCollection();
        for (int i = 0; i < normalized[1].length; i++) {
            XYSeries tmp = new XYSeries(emgData.getHeaders().get(i + 2));

            for (int j = 0; j < normalized[1][i].length; j++) {
                tmp.add((double) j / spliceSize, normalized[1][i][j]);
            }

            rightPaddles.addSeries(tmp);
        }

        GeneralPlotter leftSidePlotter = new GeneralPlotter("Left Paddling", "Time %", "Activation", leftPaddles, null, null, new DataPair<Double, Double>(0.0, 1.0));
        GeneralPlotter rightSidePlotter = new GeneralPlotter("Right Paddling", "Time %", "Activation", rightPaddles, null, null, new DataPair<Double, Double>(0.0, 1.0));
    }

    private static void doRunning(Config config, AccelerometerData accelerometerData) throws IOException {
        XYSeriesCollection[] dataset = accelerometerData.getDataset(config);

        List<RunEvent> events = EventIdentifier.getRunningEvents(dataset[0].getSeries(0), dataset[1].getSeries(0), true, 14, 1, 17, 7);
        List<DataPair<Double, Double>> separators = new ArrayList<>();

        for (int i = 0; i < events.size() - 1; i += 2) {
            separators.add(new DataPair<>((double) events.get(i).getFrame(), (double) events.get(i + 1).getFrame()));
        }

        XYSeriesCollection eventCollection = makeRunEventCollection(events);

        GeneralPlotter plotterAcc = new GeneralPlotter("Plot", "Frame", "Ampl", dataset[0], eventCollection, separators, null);
        GeneralPlotter plotterAng = new GeneralPlotter("Plot", "Frame", "Ampl", dataset[1], eventCollection, separators, null);

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

        GeneralPlotter emgPlotter = new GeneralPlotter("Emg", "Frame", "Ampl", emgSignals, null, separatorsEMG, null);

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

    private static XYSeriesCollection makeRunEventCollection(List<RunEvent> events) {
        XYSeriesCollection eventCollection = new XYSeriesCollection();
        XYSeries leftContacts = new XYSeries("Left Contacts");
        XYSeries rightContacts = new XYSeries("Right Contacts");
        XYSeries leftLifts = new XYSeries("Left Lifts");
        XYSeries rightLifts = new XYSeries("Right Lifts");
        XYSeries unknownContacts = new XYSeries("Unknown Contacts");
        XYSeries unknownLifts = new XYSeries("Unknown Lifts");

        for (RunEvent event : events) {
            switch (event.getSide()) {
                case Left -> {
                    if (event.getType() == RunEvent.Type.Contact)
                        leftContacts.add(event.getFrame(), event.getValue());
                    else
                        leftLifts.add(event.getFrame(), event.getValue());
                    break;
                }
                case Right -> {
                    if (event.getType() == RunEvent.Type.Contact)
                        rightContacts.add(event.getFrame(), event.getValue());
                    else
                        rightLifts.add(event.getFrame(), event.getValue());
                    break;
                }
                case Unknown -> {
                    if (event.getType() == RunEvent.Type.Contact)
                        unknownContacts.add(event.getFrame(), event.getValue());
                    else
                        unknownLifts.add(event.getFrame(), event.getValue());
                    break;
                }
            }
        }

        eventCollection.addSeries(leftContacts);
        eventCollection.addSeries(rightContacts);
        eventCollection.addSeries(leftLifts);
        eventCollection.addSeries(rightLifts);
        eventCollection.addSeries(unknownContacts);
        eventCollection.addSeries(unknownLifts);

        return eventCollection;
    }
}
