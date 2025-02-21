import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.jfree.data.xy.XYSeries;
import com.opencsv.CSVReader;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public static AccelerometerData readAccelerometerData(Config config, boolean filtered) throws IOException, CsvValidationException {
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

        if (!config.multifile()) {
            String filepath = config.filePath();
            if (config.filePath().endsWith(".emt")) {
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
                            if (entry.equals(config.accColX())) {
                                accOffsets[0] = i;
                            }
                            else if (entry.equals(config.accColY())) {
                                accOffsets[1] = i;
                            }
                            else if (entry.equals(config.accColZ())) {
                                accOffsets[2] = i;
                            }
                            else if (entry.equals(config.angColX())) {
                                angOffsets[0] = i;
                            }
                            else if (entry.equals(config.angColY())) {
                                angOffsets[1] = i;
                            }
                            else if (entry.equals(config.angColZ())) {
                                angOffsets[2] = i;
                            }
                            else if (entry.equals(config.angVelColX())) {
                                angVelOffsets[0] = i;
                            }
                            else if (entry.equals(config.angVelColY())) {
                                angVelOffsets[1] = i;
                            }
                            else if (entry.equals(config.angVelColZ())) {
                                angVelOffsets[2] = i;
                            }
                            else if (entry.equals(config.indexCol())) {
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
            String filepath = config.accelerationFilePath();
            if (config.accelerationFilePath().endsWith(".emt")) {
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
                            if (entry.equals(config.accColX())) {
                                accOffsets[0] = j;
                            }
                            else if (entry.equals(config.accColY())) {
                                accOffsets[1] = j;
                            }
                            else if (entry.equals(config.accColZ())) {
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

            filepath = config.anglesFilePath();
            if (config.anglesFilePath().endsWith(".emt")) {
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
                            if (entry.equals(config.angColX())) {
                                accOffsets[0] = i;
                            }
                            else if (entry.equals(config.angColY())) {
                                accOffsets[1] = i;
                            }
                            else if (entry.equals(config.angColZ())) {
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

            filepath = config.angularVelocityFilePath();
            if (config.angularVelocityFilePath().endsWith(".emt")) {
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
                            if (entry.equals(config.angVelColX())) {
                                angVelOffsets[0] = i;
                            }
                            else if (entry.equals(config.angVelColY())) {
                                angVelOffsets[1] = i;
                            }
                            else if (entry.equals(config.angVelColZ())) {
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
        return new AccelerometerData(accX, accY, accZ, angX, angY, angZ, angVelX, angVelY, angVelZ);
    }

    public static EMGData readEMGData(Config config) throws IOException {
        String path = config.emgPath();
        if (path.endsWith(".emt")) {
            EmtFileHandler.convert(path);
            path = path.replace(".emt", ".csv");
        }

        List<String> emgLabels = new ArrayList<>();
        List<List<Double>> emgColumns = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    String[] headers = line.split(",");

                    for (int i = 2; i < headers.length; i++) {
                        emgLabels.add(headers[i].trim());
                        emgColumns.add(new ArrayList<>());
                    }
                    continue;
                }

                String[] values = line.split(",");

                for (int i = 0; i < emgLabels.size(); i++) {
                    emgColumns.get(i).add(Double.parseDouble(values[i].trim()));
                }
            }
        }

        return new EMGData(emgLabels, emgColumns);
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
