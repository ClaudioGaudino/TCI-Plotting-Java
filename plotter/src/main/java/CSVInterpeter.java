import com.github.psambit9791.jdsp.filter.Butterworth;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.data.xy.XYSeries;
import com.opencsv.CSVReader;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVInterpeter {
    private static final double G = 9.80665;

    public static XYSeries read_series(String path, String xCol, String yCol, String key, boolean autosort, boolean allowDuplicates) throws IOException, CsvValidationException {
        XYSeries series = new XYSeries(key, autosort, allowDuplicates);

        boolean firstLine = true;
        int xColOffset = 0, yColOffset = 0, i;
        double xTemp = 0, yTemp = 0;

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] line;

            while ((line = reader.readNext()) != null) {
                i = 0;
                if (firstLine) {
                    for (String entry : line) {
                        if (entry.equals(xCol)) {
                            xColOffset = i;
                        }
                        if (entry.equals(yCol)) {
                            yColOffset = i;
                        }
                        i++;
                    }
                    firstLine = false;
                }
                else {
                    for (String entry : line) {
                        if (i == xColOffset) {
                            xTemp = Double.parseDouble(entry);
                        }
                        if (i == yColOffset) {
                            yTemp = Double.parseDouble(entry);
                        }
                        i++;
                    }
                    series.add(xTemp, yTemp);
                }
            }
        }

        return series;
    }

    public static XYSeries read_series(String path, String xCol, String yCol, String key) throws IOException, CsvValidationException {
        return read_series(path, xCol, yCol, key, false, false);
    }

    public static void write_series_to_csv(XYSeries series, String path, String xHeader, String yHeader) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeNext(new String[] {xHeader, yHeader});

            for (int i = 0; i < series.getItemCount(); i++) {
                writer.writeNext(new String[] {series.getX(i).toString(), series.getY(i).toString()});
            }
        }
    }

    public static XYSeriesCollection[] read_dataset(Config config, boolean filtered) throws IOException, CsvValidationException {
        XYSeriesCollection datasetAcc = new XYSeriesCollection();
        XYSeriesCollection datasetAngVel = new XYSeriesCollection();

        int[] accOffsets = new int[]{0, 0, 0};
        int[] angOffsets = new int[]{0, 0, 0};
        int[] angVelOffsets = new int[]{0, 0, 0};
        int frameOffset = 0;

        List<Double> accX = new ArrayList<>();
        List<Double> accY = new ArrayList<>();
        List<Double> accZ = new ArrayList<>();

        List<Double> angX = new ArrayList<>();
        List<Double> angY = new ArrayList<>();
        List<Double> angZ = new ArrayList<>();

        List<Double> angVelX = new ArrayList<>();
        List<Double> angVelY = new ArrayList<>();
        List<Double> angVelZ = new ArrayList<>();

        List<Double> frames = new ArrayList<>();

        double angXTmp = 0, angYTmp = 0, angZTmp = 0, accXTmp = 0, accYTmp = 0, accZTmp = 0, angVelXTmp = 0, angVelYTmp = 0, angVelZTmp = 0;

        if (!config.isMultifile()) {
            String filepath = config.getFilePath();
            if (config.getFilePath().endsWith(".emt")) {
                EmtFileHandler.convert(filepath);
                filepath = filepath.replace(".emt", ".csv");
            }

            try (CSVReader reader = new CSVReader(new FileReader(filepath))) {
                String[] line;
                boolean firstLine = true;

                int i;
                while ((line = reader.readNext()) != null) {
                    if (firstLine) {
                        firstLine = false;

                        i = 0;
                        for(String entry : line) {
                            if (entry.equals(config.getAccColX())) {
                                accOffsets[0] = i;
                            }
                            else if (entry.equals(config.getAccColY())) {
                                accOffsets[1] = i;
                            }
                            else if (entry.equals(config.getAccColZ())) {
                                accOffsets[2] = i;
                            }
                            else if (entry.equals(config.getAngColX())) {
                                angOffsets[0] = i;
                            }
                            else if (entry.equals(config.getAngColY())) {
                                angOffsets[1] = i;
                            }
                            else if (entry.equals(config.getAngColZ())) {
                                angOffsets[2] = i;
                            }
                            else if (entry.equals(config.getAngVelColX())) {
                                angVelOffsets[0] = i;
                            }
                            else if (entry.equals(config.getAngVelColY())) {
                                angVelOffsets[1] = i;
                            }
                            else if (entry.equals(config.getAngVelColZ())) {
                                angVelOffsets[2] = i;
                            }
                            else if (entry.equals(config.getIndexCol())) {
                                frameOffset = i;
                            }

                            i++;
                        }
                    }
                    else {
                        i = 0;
                        for(String entry : line) {
                            if (entry.isEmpty() || entry.isBlank()) {
                                i++;
                                continue;
                            }

                            double value = Double.parseDouble(entry);
                            if (accOffsets[0] == i) {
                                accXTmp = value;
                            }
                            else if (accOffsets[1] == i) {
                                accYTmp = value;
                            }
                            else if (accOffsets[2] == i) {
                                accZTmp = value;
                            }
                            else if(angOffsets[0] == i) {
                                angXTmp = value;
                            }
                            else if (angOffsets[1] == i) {
                                angYTmp = value;
                            }
                            else if (angOffsets[2] == i) {
                                angZTmp = value;
                            }
                            else if (angVelOffsets[0] == i) {
                                angVelXTmp = value;
                            }
                            else if (angVelOffsets[1] == i) {
                                angVelYTmp = value;
                            }
                            else if (angVelOffsets[2] == i) {
                                angVelZTmp = value;
                            }
                            else if (frameOffset == i) {
                                frames.add(value);
                            }

                            i++;
                        }

                        if(config.isFree()) {
                            double[] accRot = RotationMaths.rotate(angXTmp, angYTmp, angZTmp, accXTmp, accYTmp, accZTmp);
                            double[] angVelRot = RotationMaths.rotate(angXTmp, angYTmp, angZTmp, angVelXTmp, angVelYTmp, angVelZTmp);

                            accX.add(accRot[0]);
                            accY.add(accRot[1]);
                            accZ.add(accRot[2] - G);

                            angVelX.add(angVelRot[0]);
                            angVelY.add(angVelRot[1]);
                            angVelZ.add(angVelRot[2]);
                        }
                        else {
                            accX.add(accXTmp);
                            accY.add(accYTmp);
                            accZ.add(accZTmp);

                            angVelX.add(angVelXTmp);
                            angVelY.add(angVelYTmp);
                            angVelZ.add(angVelZTmp);
                        }
                    }
                }
            }
        }
        else {
            String filepath = config.getAccelerationFilePath();
            if (config.getAccelerationFilePath().endsWith(".emt")) {
                EmtFileHandler.convert(filepath);
                filepath = filepath.replace(".emt", ".csv");
            }

            try (CSVReader accReader = new CSVReader(new FileReader(filepath))) {
                String[] line;
                boolean firstLine = true;

                int i = 0;
                while ((line = accReader.readNext()) != null) {
                    if (firstLine) {
                        int j = 0;
                        for (String entry : line) {
                            if (entry.equals(config.getAccColX())) {
                                accOffsets[0] = j;
                            }
                            else if (entry.equals(config.getAccColY())) {
                                accOffsets[1] = j;
                            }
                            else if (entry.equals(config.getAccColZ())) {
                                accOffsets[2] = j;
                            }
                            j++;
                        }
                        firstLine = false;
                    }
                    else {
                        frames.add((double) i);
                        addEntriesSimple(accOffsets, accX, accY, accZ, line);
                    }
                    i++;
                }
            }

            filepath = config.getAnglesFilePath();
            if (config.getAnglesFilePath().endsWith(".emt")) {
                EmtFileHandler.convert(filepath);
                filepath = filepath.replace(".emt", ".csv");
            }

            try (CSVReader angReader = new CSVReader(new FileReader(filepath))) {
                String[] line;
                boolean firstLine = true;

                int i = 0;
                while ((line = angReader.readNext()) != null) {
                    if (firstLine) {
                        for (String entry : line) {
                            if (entry.equals(config.getAngColX())) {
                                accOffsets[0] = i;
                            }
                            else if (entry.equals(config.getAngColY())) {
                                accOffsets[1] = i;
                            }
                            else if (entry.equals(config.getAngColZ())) {
                                accOffsets[2] = i;
                            }
                            i++;
                        }
                        firstLine = false;
                    }
                    else {
                        addEntriesSimple(accOffsets, angX, angY, angZ, line);
                    }
                }
            }

            filepath = config.getAngularVelocityFilePath();
            if (config.getAngularVelocityFilePath().endsWith(".emt")) {
                EmtFileHandler.convert(filepath);
                filepath = filepath.replace(".emt", ".csv");
            }

            try (CSVReader angVelReader = new CSVReader(new FileReader(filepath))) {
                String[] line;
                boolean firstLine = true;

                int i = 0;
                while ((line = angVelReader.readNext()) != null) {
                    if (firstLine) {
                        for (String entry : line) {
                            if (entry.equals(config.getAngVelColX())) {
                                angVelOffsets[0] = i;
                            }
                            else if (entry.equals(config.getAngVelColY())) {
                                angVelOffsets[1] = i;
                            }
                            else if (entry.equals(config.getAngVelColZ())) {
                                angVelOffsets[2] = i;
                            }
                            i++;
                        }
                        firstLine = false;
                    }
                    else {
                        addEntriesSimple(angVelOffsets, angVelX, angVelY, angVelZ, line);
                    }
                }
            }

            if (config.isFree()) {
                List<Double> freeAccX = new ArrayList<>();
                List<Double> freeAccY = new ArrayList<>();
                List<Double> freeAccZ = new ArrayList<>();

                List<Double> freeAngVelX = new ArrayList<>();
                List<Double> freeAngVelY = new ArrayList<>();
                List<Double> freeAngVelZ = new ArrayList<>();

                for (int i  = 0; i < frames.size(); i++) {
                    double[] accRot = RotationMaths.rotate(angX.get(i), angY.get(i), angZ.get(i), accX.get(i), accY.get(i), accZ.get(i));
                    double[] angVelRot = RotationMaths.rotate(angX.get(i), angY.get(i), angZ.get(i), angVelX.get(i), angVelY.get(i), angVelZ.get(i));

                    freeAccX.add(accRot[0]);
                    freeAccY.add(accRot[1]);
                    freeAccZ.add(accRot[2] - G);

                    freeAngVelX.add(angVelRot[0]);
                    freeAngVelY.add(angVelRot[1]);
                    freeAngVelZ.add(angVelRot[2]);
                }

                accX = freeAccX;
                accY = freeAccY;
                accZ = freeAccZ;

                angVelX = freeAngVelX;
                angVelY = freeAngVelY;
                angVelZ = freeAngVelZ;
            }
        }

        if (filtered) {
            /*
            CURRENT BEST FILTER PARAMETERS:
            accOrder = 8 (started at 4)
            accCutoffFreq = 10 (started at 10)
            angOrder = 8 (started at 4)
            accCutoffFreq = 10 (started at 10)
             */
            Butterworth b = new Butterworth(100);
            int accOrder = 8, angOrder = 8;
            double accCutoffFreq = 10, angCutoffFreq = 5;
            if (config.isPlotX()) {
                accX = lowPassFilter(accX, b, accOrder, accCutoffFreq);
                angVelX = lowPassFilter(angVelX, b, angOrder, angCutoffFreq);
            }
            if (config.isPlotY()) {
                accY = lowPassFilter(accY, b, accOrder, accCutoffFreq);
                angVelY = lowPassFilter(angVelY, b, angOrder, angCutoffFreq);
            }
            if (config.isPlotZ()) {
                accZ = lowPassFilter(accZ, b, accOrder, accCutoffFreq);
                angVelZ = lowPassFilter(angVelZ, b, angOrder, angCutoffFreq);
            }
        }



        XYSeries accXSeries = new XYSeries("X Acceleration");
        XYSeries accYSeries = new XYSeries("Y Acceleration");
        XYSeries accZSeries = new XYSeries("Z Acceleration");

        XYSeries angVelXSeries = new XYSeries("X Angular Velocity");
        XYSeries angVelYSeries = new XYSeries("Y Angular Velocity");
        XYSeries angVelZSeries = new XYSeries("Z Angular Velocity");

        while (!frames.isEmpty()) {
            double frame = frames.remove(0);

            if (config.isPlotX()) {
                accXSeries.add(frame, accX.remove(0));
                angVelXSeries.add(frame, angVelX.remove(0));
            }
            if (config.isPlotY()) {
                accYSeries.add(frame, accY.remove(0));
                angVelYSeries.add(frame, angVelY.remove(0));
            }
            if (config.isPlotZ()) {
                accZSeries.add(frame, accZ.remove(0));
                angVelZSeries.add(frame, angVelZ.remove(0));
            }
        }

        if (config.isPlotX()) {
            datasetAcc.addSeries(accXSeries);
            datasetAngVel.addSeries(angVelXSeries);
        }
        if (config.isPlotY()) {
            datasetAcc.addSeries(accYSeries);
            datasetAngVel.addSeries(angVelYSeries);
        }
        if (config.isPlotZ()) {
            datasetAcc.addSeries(accZSeries);
            datasetAngVel.addSeries(angVelZSeries);
        }

        return new XYSeriesCollection[]{datasetAcc, datasetAngVel};
    }

    private static int addEntriesSimple(int[] offsets, List<Double> X, List<Double> Y, List<Double> Z, String[] line) {
        int i;
        i = 0;
        for (String entry : line) {
            if (entry.isEmpty() || entry.isBlank()) {
                i++;
                continue;
            }

            double value = Double.parseDouble(entry);
            if (offsets[0] == i) {
                X.add(value);
            }
            else if (offsets[1] == i) {
                Y.add(value);
            }
            else if (offsets[2] == i) {
                Z.add(value);
            }
            i++;
        }
        return i;
    }

    private static double[] listToArray(List<Double> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            result[i] = d;
            i++;
        }

        return result;
    }

    private static List<Double> lowPassFilter(List<Double> data, Butterworth b, int order, double cutoffFreq) {
        double[] XF = listToArray(data);
        XF = b.lowPassFilter(XF, order, cutoffFreq);
        return Arrays.stream(XF).boxed().collect(Collectors.toList());
    }

    private static List<Double> highPassFilter(List<Double> data, Butterworth b, int order, double cutoffFreq) {
        double[] XF = listToArray(data);
        XF = b.highPassFilter(XF, order, cutoffFreq);
        return Arrays.stream(XF).boxed().collect(Collectors.toList());
    }

    public static void write_contacts(XYSeries[] accContacts, XYSeries[] angContacts, String path) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
            writer.writeNext(new String[]{"Frame", "Accelerazione", "Velocità_angolare", "Destro"});

            int sizeRight = accContacts[0].getItemCount();
            int sizeLeft = accContacts[1].getItemCount();
            XYSeries accRight = accContacts[0];
            XYSeries accLeft = accContacts[1];
            XYSeries angRight = angContacts[0];
            XYSeries angLeft = angContacts[1];

            int i = 0, j = 0;
            while (i < sizeRight && j < sizeLeft) {
                if ((double) accRight.getX(i) <= (double) accLeft.getX(j)) {
                    writer.writeNext(new String[]{accRight.getX(i).toString(), accRight.getY(i).toString(), angRight.getY(i).toString(), "true"});
                    i++;
                }
                else {
                    writer.writeNext(new String[]{accLeft.getX(j).toString(), accLeft.getY(j).toString(), angLeft.getY(j).toString(), "false"});
                    j++;
                }
            }
            while (i < sizeRight) {
                writer.writeNext(new String[]{accRight.getX(i).toString(), accRight.getY(i).toString(), angRight.getY(i).toString(), "true"});
                i++;
            }
            while (j < sizeLeft) {
                writer.writeNext(new String[]{accLeft.getX(j).toString(), accLeft.getY(j).toString(), angLeft.getY(j).toString(), "false"});
                j++;
            }
        }
    }
}
