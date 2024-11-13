import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.jfree.data.xy.XYSeries;
import com.opencsv.CSVReader;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVInterpeter {

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

    public static XYSeriesCollection[] read_dataset(Config config) throws IOException, CsvValidationException {
        XYSeriesCollection datasetAcc = new XYSeriesCollection();
        XYSeriesCollection datasetAngVel = new XYSeriesCollection();

        int[] accOffsets = new int[]{0, 0, 0};
        int[] angOffsets = new int[]{0, 0, 0};
        int[] angVelOffsets = new int[]{0, 0, 0};
        int frameOffset = 0;

        List<Double> accX = new ArrayList<>();
        List<Double> accY = new ArrayList<>();
        List<Double> accZ = new ArrayList<>();

        List<Double> angVelX = new ArrayList<>();
        List<Double> angVelY = new ArrayList<>();
        List<Double> angVelZ = new ArrayList<>();

        List<Double> frames = new ArrayList<>();

        double angX = 0, angY = 0, angZ = 0, accXTmp = 0, accYTmp = 0, accZTmp = 0, angVelXTmp = 0, angVelYTmp = 0, angVelZTmp = 0;

        if (!config.isMultifile()) {
            try (CSVReader reader = new CSVReader(new FileReader(config.getFilePath()))) {
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
                                angX = value;
                            }
                            else if (angOffsets[1] == i) {
                                angY = value;
                            }
                            else if (angOffsets[2] == i) {
                                angZ = value;
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
                            double[] accRot = RotationMaths.rotate(angX, angY, angZ, accXTmp, accYTmp, accZTmp);
                            double[] angVelRot = RotationMaths.rotate(angX, angY, angZ, angVelXTmp, angVelYTmp, angVelZTmp);

                            accX.add(accRot[0]);
                            accY.add(accRot[1]);
                            accZ.add(accRot[2]);

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
}
