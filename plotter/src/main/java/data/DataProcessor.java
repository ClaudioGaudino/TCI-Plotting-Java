package data;

import data.DataPair;
import enums.Side;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import py4j.GatewayServer;

import java.util.List;

public class DataProcessor {

    public static double[][][] spliceAndResampleEMG(List<List<Double>> signals, List<DataPair<Double, Double>> separators, int spliceSize) {
        int size = signals.get(2).size();
        for (int i = 2; i < signals.size(); i++) {
            if (signals.get(i).size() != size)
                throw new IllegalArgumentException("Signals do not have equal lengths!");
        }

        //the first two signals are frame and time (null if using filtered)
        double[][][] splices = new double[separators.size()][signals.size() - 2][spliceSize];

        for (int i = 0; i < separators.size(); i ++) {
            //j starts at 2 to skip frame and time columns
            for (int j = 2; j < signals.size(); j++) {
                spliceAndResampleSingle(splices[i][j - 2], signals.get(j), separators.get(i), spliceSize);
            }
        }

        return splices;
    }

    public static double[][][] joinSidesAndNormalizeEMG(double[][][] splices, Side initialSide) {
        int rightSlices = 0, leftSlices = 0;
        if (splices.length % 2 == 0) {
            rightSlices = splices.length / 2;
            leftSlices = rightSlices;
        } else {
            switch (initialSide) {
                case LEFT -> {
                    leftSlices = ((splices.length - 1) / 2) + 1;
                    rightSlices = (splices.length - 1) / 2;
                }
                case RIGHT -> {
                    rightSlices = ((splices.length - 1) / 2) + 1;
                    leftSlices = (splices.length - 1) / 2;
                }
                case UNKNOWN -> {
                    throw new IllegalArgumentException("Initial side may not be unknown!");
                }
            }
        }



        double[][][] normalized = new double[2][splices[0].length][splices[0][0].length];
        int is = (initialSide == Side.LEFT) ? 0 : 1;


        //normalizeSide(splices, is);
        //normalizeSide(splices, 1 - is);
        normalizeToOwnMax(splices);

        int s = is;
        for (int i = 0; i < splices.length; i ++, s = 1 - s) {
            for (int j = 0; j < splices[i].length; j++) {
                for (int k = 0; k < splices[i][j].length; k++) {
                    normalized[s][j][k] += splices[i][j][k];
                }
            }
        }

        for (int i = 0; i < normalized.length; i ++) {
            for (int j = 0; j < splices[i].length; j++) {
                for (int k = 0; k < splices[i][j].length; k++) {
                    normalized[i][j][k] /= (i == is) ? leftSlices : rightSlices;
                }
            }
        }

        return normalized;
    }

    public static NMFResult runNMF(double[][] matrix) {
        if (matrix.length < 2)
            throw new IllegalArgumentException("Matrix has less than 2 rows");

        GatewayServer.turnLoggingOff();
        GatewayServer server = new GatewayServer();
        server.start();

        NMF nmf = (NMF) server.getPythonServerEntryPoint(new Class[] {NMF.class});

        try {
            System.out.println(matrix.length + " " + matrix[0].length);
            System.out.println(nmf.num(5));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.shutdown();
        }

        int k = 2;
        double vaf, prevvaf = 0, improvement = 10;
        double minImprovement = 1;

        do {


            k++;
        } while (k < matrix.length || improvement < minImprovement);

        server.shutdown();

        return new NMFResult(null, null, 0);
    }

    private static void normalizeToOwnMax(double[][][] splices) {
        double max;

        for (int i = 0; i < splices.length; i++) {
            for (int j = 0; j < splices[0].length; j++) {
                max = splices[i][j][0];

                for (int k = 0; k < splices[0][0].length; k++) {
                    if (splices[i][j][k] > max) {
                        max = splices[i][j][k];
                    }
                }

                for (int k = 0; k < splices[0][0].length; k++) {
                    splices[i][j][k] = splices[i][j][k] / max;
                }
            }
        }
    }

    private static void normalizeSide(double[][][] splices, int s) {
        double[] max = new double[splices[0].length];

        for (int i = 0; i < max.length; i++) {
            max[i] = splices[s][i][0];
        }

        for (int i = s; i < splices.length; i += 2) {
            for (int j = 0; j < splices[i].length; j++) {
                for (int k = 0; k < splices[i][j].length; k++) {
                    if (splices[i][j][k] > max[j]) {
                        max[j] = splices[i][j][k];
                    }
                }
            }
        }

        for (int i = s; i < splices.length; i += 2) {
            for (int j = 0; j < splices[i].length; j++) {
                for (int k = 0; k < splices[i][j].length; k++) {
                    splices[i][j][k] = splices[i][j][k] / max[j];
                }
            }
        }
    }

    private static void spliceAndResampleSingle(double[] destination, List<Double> signal, DataPair<Double, Double> separator, int spliceSize) {
        if (destination.length != spliceSize)
            throw new IllegalArgumentException("Destination array is not the same length as splice size!");

        double[] time = new double[signal.size()];
        for (int i = 0; i < time.length; i++)
            time[i] = i;

        SplineInterpolator interpolator = new SplineInterpolator();
        PolynomialSplineFunction spline = interpolator.interpolate(time, signal.stream().mapToDouble(Double::doubleValue).toArray());
        double step = (separator.b() - separator.a()) / (spliceSize - 1);

        for (int i = 0; i < spliceSize; i++) {
            destination[i] = spline.value(separator.a() + i * step);
        }
    }

    private double computeVAF(RealMatrix V, RealMatrix W, RealMatrix H) {
        RealMatrix reconstruction = W.multiply(H);
        double vNorm = V.getFrobeniusNorm();
        double errorNorm = V.subtract(reconstruction).getFrobeniusNorm();

        return 1 - (errorNorm * errorNorm) / (vNorm * vNorm);
    }


}
