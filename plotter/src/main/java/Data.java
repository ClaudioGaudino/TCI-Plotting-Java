import com.github.psambit9791.jdsp.filter.Butterworth;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Data {
    public enum Axis {
        X, Y, Z, MAGNITUDE
    }
    public enum Type {
        ACCELERATION, ANG_VELOCITY
    }

    private final static double G = 9.80665;

    private int size;

    private List<Double> accX;
    private List<Double> accY;
    private List<Double> accZ;

    private List<Double> angX;
    private List<Double> angY;
    private List<Double> angZ;

    private List<Double> angVelX;
    private List<Double> angVelY;
    private List<Double> angVelZ;

    private List<Double> freeAccX;
    private List<Double> freeAccY;
    private List<Double> freeAccZ;
    private List<Double> freeAngVelX;
    private List<Double> freeAngVelY;
    private List<Double> freeAngVelZ;

    private List<Double> accMagnitude;
    private List<Double> angVelMagnitude;

    public Data(List<Double> accX, List<Double> accY, List<Double> accZ,
                List<Double> angX, List<Double> angY, List<Double> angZ,
                List<Double> angVelX, List<Double> angVelY, List<Double> angVelZ) {

        if (accX.size() != accY.size() ||
        accX.size() != accZ.size() ||
        accX.size() != angX.size() ||
        accX.size() != angY.size() ||
        accX.size() != angZ.size() ||
        accX.size() != angVelX.size() ||
        accX.size() != angVelY.size() ||
        accX.size() != angVelZ.size()) {
            throw new IllegalArgumentException("One or more of the data lists have a different size than the others");
        }

        this.size = accX.size();
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.angX = angX;
        this.angY = angY;
        this.angZ = angZ;
        this.angVelX = angVelX;
        this.angVelY = angVelY;
        this.angVelZ = angVelZ;
    }

    public void filter(Axis axis, Type type, Butterworth b, int order, double cutoffFreq) {
        switch (axis) {
            case X -> {
                switch (type) {
                    case ACCELERATION -> {
                        accX = lowPassFilter(accX, b, order, cutoffFreq);
                        freeAccX = (freeAccX == null) ? null : lowPassFilter(freeAccX, b, order, cutoffFreq);
                    }
                    case ANG_VELOCITY -> {
                        angVelX = lowPassFilter(angVelX, b, order, cutoffFreq);
                        freeAngVelX = (freeAngVelX == null) ? null : lowPassFilter(freeAngVelX, b, order, cutoffFreq);
                    }
                }
            }
            case Y -> {
                switch (type) {
                    case ACCELERATION -> {
                        accY = lowPassFilter(accY, b, order, cutoffFreq);
                        freeAccY = (freeAccY == null) ? null : lowPassFilter(freeAccY, b, order, cutoffFreq);
                    }
                    case ANG_VELOCITY -> {
                        angVelY = lowPassFilter(angVelY, b, order, cutoffFreq);
                        freeAngVelY = (freeAngVelY == null) ? null : lowPassFilter(freeAngVelY, b, order, cutoffFreq);
                    }
                }
            }
            case Z -> {
                switch (type) {
                    case ACCELERATION -> {
                        accZ = lowPassFilter(accZ, b, order, cutoffFreq);
                        freeAccZ = (freeAccZ == null) ? null : lowPassFilter(freeAccZ, b, order, cutoffFreq);
                    }
                    case ANG_VELOCITY -> {
                        angVelZ = lowPassFilter(angVelZ, b, order, cutoffFreq);
                        freeAngVelZ = (freeAngVelZ == null) ? null : lowPassFilter(freeAngVelZ, b, order, cutoffFreq);
                    }
                }
            }
            case MAGNITUDE -> {
                switch (type) {
                    case ACCELERATION -> {
                        getAccMagnitude();
                        accMagnitude = lowPassFilter(accMagnitude, b, order, cutoffFreq);
                    }
                    case ANG_VELOCITY -> {
                        getAngVelMagnitude();
                        angVelMagnitude = lowPassFilter(angVelMagnitude, b, order, cutoffFreq);
                    }
                }
            }
        }
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

    private static double[] listToArray(List<Double> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Double d : list) {
            result[i] = d;
            i++;
        }

        return result;
    }

    public void makeFree() {
        freeAccX = new ArrayList<>();
        freeAccY = new ArrayList<>();
        freeAccZ = new ArrayList<>();
        freeAngVelX = new ArrayList<>();
        freeAngVelY = new ArrayList<>();
        freeAngVelZ = new ArrayList<>();

        for (int i  = 0; i < size; i++) {
            double[] accRot = RotationMaths.rotate(angX.get(i), angY.get(i), angZ.get(i), accX.get(i), accY.get(i), accZ.get(i));
            double[] angVelRot = RotationMaths.rotate(angX.get(i), angY.get(i), angZ.get(i), angVelX.get(i), angVelY.get(i), angVelZ.get(i));

            freeAccX.add(accRot[0]);
            freeAccY.add(accRot[1]);
            freeAccZ.add(accRot[2] - G);

            freeAngVelX.add(angVelRot[0]);
            freeAngVelY.add(angVelRot[1]);
            freeAngVelZ.add(angVelRot[2]);

            if (Objects.equals(freeAccZ.get(i), accZ.get(i))) {
                System.out.println("they equal at " + i);
            }
        }
    }

    public XYSeriesCollection[] getDataset(Config config) {
        XYSeriesCollection datasetAcc = new XYSeriesCollection();
        XYSeriesCollection datasetAngVel = new XYSeriesCollection();

        if (config.isUseAccMagnitude()) {
            XYSeries accMagnitudeSeries = new XYSeries("Acceleration (Magnitude)");

            getAccMagnitude();

            for (int frame = 0; frame < size; frame++)
                accMagnitudeSeries.add(frame, accMagnitude.get(frame));

            datasetAcc.addSeries(accMagnitudeSeries);
        }
        else {
            XYSeries accXSeries = new XYSeries("X Acceleration");
            XYSeries accYSeries = new XYSeries("Y Acceleration");
            XYSeries accZSeries = new XYSeries("Z Acceleration");

            List<Double> localAccX = config.isFree() ? this.freeAccX : this.accX;
            List<Double> localAccY = config.isFree() ? this.freeAccY : this.accY;
            List<Double> localAccZ = config.isFree() ? this.freeAccZ : this.accZ;

            for (int frame = 0; frame < size; frame++) {
                if (config.isPlotX()) {
                    if (config.isFree()) {
                        accXSeries.add(frame, localAccX.get(frame));
                    } else {
                        accXSeries.add(frame, localAccX.get(frame));
                    }

                }
                if (config.isPlotY()) {
                    accYSeries.add(frame, localAccY.get(frame));
                }
                if (config.isPlotZ()) {
                    accZSeries.add(frame, localAccZ.get(frame));
                }
            }

            if (config.isPlotX()) {
                datasetAcc.addSeries(accXSeries);
            }
            if (config.isPlotY()) {
                datasetAcc.addSeries(accYSeries);
            }
            if (config.isPlotZ()) {
                datasetAcc.addSeries(accZSeries);
            }
        }

        if (config.isUseAngVelMagnitude()) {
            XYSeries angVelMagnitudeSeries = new XYSeries("Angular Velocity (Magnitude)");

            getAngVelMagnitude();

            for (int frame = 0; frame < size; frame++)
                angVelMagnitudeSeries.add(frame, angVelMagnitude.get(frame));

            datasetAngVel.addSeries(angVelMagnitudeSeries);
        }
        else {
            XYSeries angVelXSeries = new XYSeries("X Angular Velocity");
            XYSeries angVelYSeries = new XYSeries("Y Angular Velocity");
            XYSeries angVelZSeries = new XYSeries("Z Angular Velocity");

            List<Double> localAngVelX = config.isFree() ? this.freeAngVelX : this.angVelX;
            List<Double> localAngVelY = config.isFree() ? this.freeAngVelY : this.angVelY;
            List<Double> localAngVelZ = config.isFree() ? this.freeAngVelZ : this.angVelZ;

            for (int frame = 0; frame < size; frame++) {
                if (config.isPlotX()) {
                    if (config.isFree()) {
                        angVelXSeries.add(frame, localAngVelX.get(frame));
                    } else {
                        angVelXSeries.add(frame, localAngVelZ.get(frame));
                    }

                }
                if (config.isPlotY()) {
                    angVelYSeries.add(frame, localAngVelY.get(frame));
                }
                if (config.isPlotZ()) {
                    angVelZSeries.add(frame, localAngVelZ.get(frame));
                }
            }

            if (config.isPlotX()) {
                datasetAngVel.addSeries(angVelXSeries);
            }
            if (config.isPlotY()) {
                datasetAngVel.addSeries(angVelYSeries);
            }
            if (config.isPlotZ()) {
                datasetAngVel.addSeries(angVelZSeries);
            }
        }

        return new XYSeriesCollection[]{datasetAcc, datasetAngVel};
    }

    public int getSize() {
        return size;
    }

    public List<Double> getAccX() {
        return accX;
    }

    public List<Double> getAccY() {
        return accY;
    }

    public List<Double> getAccZ() {
        return accZ;
    }

    public List<Double> getAngX() {
        return angX;
    }

    public List<Double> getAngY() {
        return angY;
    }

    public List<Double> getAngZ() {
        return angZ;
    }

    public List<Double> getAngVelX() {
        return angVelX;
    }

    public List<Double> getAngVelY() {
        return angVelY;
    }

    public List<Double> getAngVelZ() {
        return angVelZ;
    }

    public List<Double> getFreeAccX() {
        return freeAccX;
    }

    public List<Double> getFreeAccY() {
        return freeAccY;
    }

    public List<Double> getFreeAccZ() {
        return freeAccZ;
    }

    public List<Double> getFreeAngVelX() {
        return freeAngVelX;
    }

    public List<Double> getFreeAngVelY() {
        return freeAngVelY;
    }

    public List<Double> getFreeAngVelZ() {
        return freeAngVelZ;
    }

    public List<Double> getAccMagnitude() {
        if (accMagnitude == null)
            accMagnitude = calculateMagnitude(accX, accY, accZ, true);

        return accMagnitude;
    }

    public List<Double> getAngVelMagnitude() {
        if (angVelMagnitude == null)
            angVelMagnitude = calculateMagnitude(angVelX, angVelY, angVelZ, false);

        return angVelMagnitude;
    }

    private List<Double> calculateMagnitude(List<Double> x, List<Double> y, List<Double> z, boolean removeG) {
        List<Double> mag = new ArrayList<>();
        double tmp;

        for (int i = 0; i < size; i++) {
            tmp = Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2)) - (removeG ? G : 0);
            mag.add(tmp);
        }

        return mag;
    }
}
