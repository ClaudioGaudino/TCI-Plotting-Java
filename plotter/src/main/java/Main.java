import com.github.psambit9791.jdsp.filter.Butterworth;
import data.*;
import data.Config;
import enums.PaddleType;
import enums.Side;
import event.EventIdentifier;
import event.PaddleEvent;
import event.RunEvent;
import gui.GeneralPlotter;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import utils.CSVInterpeter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static Process pyServer;

    public static void main(String[] args) {
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

        Config pagCentro2 = new Config(
                true,
                "",
                "data\\pagaiata\\centro2\\Acc_pagaiata.emt", "data\\pagaiata\\centro2\\Angoli_pagaiata.emt", "data\\pagaiata\\centro2\\Vel_Ang_pagaiata.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                false, false,
                true,
                false, false, true, "data\\pagaiata\\centro2\\EMG_pagaiata.emt"
        );

        Config pagfull1 = new Config(
                true, "",
                "", "data\\pagaiata\\Prove_pagaiata_18_03\\Ang_1.emt", "data\\pagaiata\\Prove_pagaiata_18_03\\Vel_Ang_1.emt",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "GSensor.X", "GSensor.Y", "GSensor.Z",
                "Frame",
                "", "", "",
                false, false,
                true,
                false, false, true, "data\\pagaiata\\Prove_pagaiata_18_03\\EMG_1.emt"
        );

        try {
            Config config = pagfull1;
            boolean filtered = true;
            boolean doRunningInstead = false;
            AccelerometerData accelerometerData = CSVInterpeter.readAccelerometerData(config, true);

            startPythonServices();

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

            pyServer.destroy();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void doPaddling(Config config, AccelerometerData accelerometerData) throws IOException {
        XYSeriesCollection[] dataset = accelerometerData.getDataset(config);

        //-----------------------------------------------------------------------
        //STEP 1 : GETTING EVENTS
        //-----------------------------------------------------------------------

        List<PaddleEvent> events = EventIdentifier.getPaddlingEvents(accelerometerData.getFreeAngVelZ(), 1, -40, 40, false);
        List<DataPair<Double, Double>> separators = new ArrayList<>();
        Side initialSide;

        if (!events.isEmpty()) {
            //remove possible false positive events like a starting leave or ending hit
            if (events.get(0).type() == PaddleType.LEAVE) {
                events.remove(0);
            }

            for (int i = events.size() - 1; i >= 0; i--) {
                if ((events.get(i).type() == events.get(0).type()) && (events.get(i).side() == events.get(0).side())) {
                    break;
                }
                else {
                    events.remove(i);
                }
            }

            initialSide = events.get(0).side();

            //generate separators for display purposes
            for (int i = 0; i < events.size() - 1; i += 2) {
                separators.add(new DataPair<>((double) events.get(i).frame(), (double) events.get(i + 1).frame()));
            }
        } else {
            initialSide = Side.LEFT;
        }

        XYSeriesCollection eventCollection = makeEventCollection(events, accelerometerData);

        GeneralPlotter plotter = new GeneralPlotter("Events", "Frame", "Velocità Angolare", dataset[1], eventCollection, separators, null);

        //-----------------------------------------------------------------------
        //STEP 2 : PROCESSING EMG SIGNALS
        //-----------------------------------------------------------------------

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
        List<SpliceSeparator<Integer>> spliceSeparators = new ArrayList<>();
        DataPair<Double, Double> tmpSeparator = null;
        for (int i = 0; i < events.size(); i += 4) {
            if (i == events.size() - 1) break;

            spliceSeparators.add(new SpliceSeparator<>(
                    events.get(i).frame(),
                    events.get(i + 1).frame(),
                    events.get(i + 2).frame(),
                    events.get(i + 3).frame(),
                    events.get(i + 4).frame()
            ));

            separatorsEMG.add(new DataPair<>((double) events.get(i).frame() * 10, (double) events.get(i + 4).frame() * 10));
        }

        GeneralPlotter emgPlotter = new GeneralPlotter("Emg", "Frame", "Ampl", emgSignals, null, separatorsEMG, null);

        //-----------------------------------------------------------------------
        //STEP 3 : RESAMPLING, NORMALIZING AND SPLITTING
        //-----------------------------------------------------------------------

        int spliceSize = 200;
        double[][][] splices = DataProcessor.spliceAndResampleEMG(emgData.getFilteredSignals(), separatorsEMG, spliceSize);
        double[][] normalized = DataProcessor.normalizeSplices(splices);
        SpliceSeparator<Double> separatorPercents = DataProcessor.generateSplicePercentAverages(spliceSeparators, spliceSize);

        XYSeriesCollection splice0 = new XYSeriesCollection();
        XYSeries tmp;
        for (int i = 0; i < splices[0].length; i++) {
            tmp = new XYSeries(emgData.getHeaders().get(i + 2));

            for (int j = 0; j < splices[0][0].length; j++) {
                tmp.add(j, splices[0][i][j]);
            }

            splice0.addSeries(tmp);
        }

        XYSeriesCollection paddlesNormalized = new XYSeriesCollection();

        for (int i = 0; i < normalized.length; i++) {
            tmp = new XYSeries(emgData.getHeaders().get(i + 2));

            for (int j = 0; j < normalized[0].length; j++) {
                tmp.add(j, normalized[i][j]);
            }

            paddlesNormalized.addSeries(tmp);
        }

        List<Double> avgPercentSplits = new ArrayList<>();
        double ratio = spliceSize / 100.0;
        avgPercentSplits.add(separatorPercents.startHit() * ratio);
        avgPercentSplits.add(separatorPercents.firstLeave() * ratio);
        avgPercentSplits.add(separatorPercents.midHit() * ratio);
        avgPercentSplits.add(separatorPercents.secondLeave() * ratio);
        avgPercentSplits.add(separatorPercents.endHit() * ratio);

        GeneralPlotter splicePlotter= new GeneralPlotter("Splice #0", "Time %", "Activation",
                splice0, null, avgPercentSplits, new DataPair<>(0.0, 1.0), false);
        GeneralPlotter paddlesPlotter = new GeneralPlotter("Normalized Paddling", "Time %", "Activation",
                paddlesNormalized, null, avgPercentSplits, new DataPair<>(0.0, 1.0), false);

        //-----------------------------------------------------------------------
        //STEP 4 : SYNERGY DETECTION
        //-----------------------------------------------------------------------

        NMFResult result = DataProcessor.runNMF(normalized);


    }

    private static void startPythonServices() throws IOException, InterruptedException {
        ProcessBuilder nmfBuidler = new ProcessBuilder();
        nmfBuidler.command("python", "python\\nmf.py");
        //nmfBuidler.command("python", "-v");
        nmfBuidler.directory(new File("."));
        nmfBuidler.inheritIO();
        nmfBuidler.redirectErrorStream(true);
        pyServer = nmfBuidler.start();

        //Wait for the python script to be ready by checking for the tmp.flag file
        Path readyFlag = Paths.get("python\\tmp.flag");

        for (int i = 0; i < 20; i++) {
            if (Files.exists(readyFlag)) {
                try {
                    Files.delete(readyFlag);
                } catch (IOException e) {
                    System.err.println("Could not delete tmp.flag file");
                    e.printStackTrace();
                }
            }

            Thread.sleep(250);
        }

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
