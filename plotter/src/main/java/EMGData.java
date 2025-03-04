import com.github.psambit9791.jdsp.filter.Butterworth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EMGData {
    private List<String> headers;
    private List<List<Double>> signals;

    private List<List<Double>> filteredSignals;

    public EMGData(List<String> headers, List<List<Double>> signals) {
        this.headers = headers;
        this.signals = signals;
        filteredSignals = null;
    }

    public void filter() {
        Butterworth b = new Butterworth(1000);
        filteredSignals = new ArrayList<>();
        filteredSignals.add(null);
        filteredSignals.add(null);

        for (int i = 2; i < signals.size(); i++) {
            double[] tmp = signals.get(i).stream().mapToDouble(Double::doubleValue).toArray();

            tmp = b.bandPassFilter(tmp, 4, 20, 450);
            tmp = b.highPassFilter(tmp, 4, 40);
            tmp = fullWaveRectify(tmp);
            tmp = b.lowPassFilter(tmp, 4, 15);
            tmp = halfWaveRectify(tmp);

            filteredSignals.add(Arrays.stream(tmp).boxed().toList());
        }
    }

    private double[] fullWaveRectify(double[] signal) {
        double[] rectifiedSignal = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            rectifiedSignal[i] = Math.abs(signal[i]);
        }
        return rectifiedSignal;
    }

    private double[] halfWaveRectify(double[] signal) {
        double[] rectifiedSignal = new double[signal.length];
        for (int i = 0; i < signal.length; i++) {
            rectifiedSignal[i] = Math.max(0, signal[i]);
        }
        return rectifiedSignal;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<Double>> getSignals() {
        return signals;
    }

    public List<List<Double>> getFilteredSignals() {
        return filteredSignals;
    }
}
