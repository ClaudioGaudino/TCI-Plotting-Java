package data;

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
            //j starts at 2 to skip the frame and time columns
            for (int j = 2; j < signals.size(); j++) {
                spliceAndResampleSingle(splices[i][j - 2], signals.get(j), separators.get(i), spliceSize);
            }
        }

        return splices;
    }

    public static double[][] normalizeSplices(double[][][] splices) {
        double[][] normalized = new double[splices[0].length][splices[0][0].length];

        normalizeToOwnMax(splices);

        for (int i = 0; i < splices.length; i++) {
            for (int j = 0; j < splices[i].length; j++) {
                for (int k = 0; k < splices[i][j].length; k++) {
                    normalized[j][k] += splices[i][j][k];
                }
            }
        }

        for (int j = 0; j < normalized.length; j++) {
            for (int k = 0; k < normalized[0].length; k++) {
                normalized[j][k] /= splices.length;
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
        List<List<List<Double>>> tmp;
        RealMatrix V = MatrixUtils.createRealMatrix(matrix);
        NMFResult res = null;
        double[][] W, H;
        int k = 2;
        double vaf, prevvaf = 0, improvement;
        double minImprovement = 2;

        try {
            do {
                tmp = nmf.factorize(matrix, k);
                W = tmp.get(0)
                        .stream()
                        .map(innerList -> innerList.stream().mapToDouble(Double::doubleValue).toArray())
                        .toArray(double[][]::new);
                H = tmp.get(1)
                        .stream()
                        .map(innerList -> innerList.stream().mapToDouble(Double::doubleValue).toArray())
                        .toArray(double[][]::new);

                vaf = computeVAF(
                        V,
                        MatrixUtils.createRealMatrix(W),
                        MatrixUtils.createRealMatrix(H)
                );

                if (k == 2) {
                    improvement = 100;
                    res = new NMFResult(W, H, k);
                } else {
                    improvement = ((vaf - prevvaf) * 100) / prevvaf;
                    if (improvement >= minImprovement) {
                        res = new NMFResult(W, H, k);
                    }
                }

                System.out.println("k = " + k + "\tvaf = " + vaf + "\timprovement = " + improvement);

                prevvaf = vaf;
                k++;
            } while (k < (matrix.length - 1) && improvement >= minImprovement);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.shutdown();
        }

        return new NMFResult(null, null, 0);
    }

    public static SpliceSeparator<Double> generateSplicePercentAverages(List<SpliceSeparator<Integer>> separators, int spliceSize) {
        double startHit = 0, firstLeave = 0, midHit = 0, secondLeave = 0, endHit = 0;
        int delta;

        for (SpliceSeparator<Integer> separator : separators) {
            delta = separator.endHit() - separator.startHit();

            firstLeave += (separator.firstLeave() - separator.startHit()) * 100.0 / delta;
            midHit += (separator.midHit() - separator.startHit()) * 100.0 / delta;
            secondLeave += (separator.secondLeave() - separator.startHit()) * 100.0 / delta;
            endHit += (separator.endHit() - separator.startHit()) * 100.0 / delta;
        }

        firstLeave /= separators.size();
        midHit /= separators.size();
        secondLeave /= separators.size();
        endHit /= separators.size();

        return new SpliceSeparator<>(startHit, firstLeave, midHit, secondLeave, endHit);
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
            destination[i] = spline.value(separator.a() + (i * step));
        }
    }

    private static double computeVAF(RealMatrix V, RealMatrix W, RealMatrix H) {
        RealMatrix reconstruction = W.multiply(H);
        double vNorm = V.getFrobeniusNorm();
        double errorNorm = V.subtract(reconstruction).getFrobeniusNorm();

        return 1 - (errorNorm * errorNorm) / (vNorm * vNorm);
    }
}
