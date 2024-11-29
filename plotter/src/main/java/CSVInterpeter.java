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

    public static Data read_dataset(Config config, boolean filtered) throws IOException, CsvValidationException {
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

                        accX.add(accXTmp);
                        accY.add(accYTmp);
                        accZ.add(accZTmp);

                        angX.add(angXTmp);
                        angY.add(angYTmp);
                        angZ.add(angZTmp);

                        angVelX.add(angVelXTmp);
                        angVelY.add(angVelYTmp);
                        angVelZ.add(angVelZTmp);
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
        }
        return new Data(accX, accY, accZ, angX, angY, angZ, angVelX, angVelY, angVelZ);
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
