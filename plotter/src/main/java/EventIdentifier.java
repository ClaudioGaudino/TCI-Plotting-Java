import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class EventIdentifier {

    private enum StepSide {
        LEFT, RIGHT, UNKNOWN
    }
    private static final double G = 9.80665 - 0.5;

    /*
    Un contatto si verifica quando l'accelerazione è in discesa ed attraversa -G
    Il contatto è destro se la velocità angolare è in salita
    Il contatto è sinistro se la veloticà angolare è in discesa
     */
    public static XYSeries[] getContactEvents(XYSeries accSeries, XYSeries angVelSeries, boolean doAcc) throws IllegalArgumentException {
        if (accSeries.getItemCount() != angVelSeries.getItemCount())
            throw new IllegalArgumentException("AccSeries and AngVelSeries have different sizes.");

        int size = accSeries.getItemCount();

        double[] acc = seriesToArray(accSeries);
        double[] ang = seriesToArray(angVelSeries);

        double accLast = acc[0];
        double angSlope = 0, angWindowEnd = acc[0];

        int window = 1;

        XYSeries contactsRight = new XYSeries("Right Contacts");
        XYSeries contactsLeft = new XYSeries("Left Contacts");
        XYSeries rightDebug = new XYSeries("Right Debug");
        XYSeries leftDebug = new XYSeries("Left Debug");

        System.out.println("\n\nPrinting " + (doAcc ? "Acceleration" : "Angular Velocity") + "\n");

        boolean hasCutoff = false;
        int cutoff = 500;
        double zeroDelta = 0.5;
        for (int j = 0; j < size; j++) {
            int i = j;

            if (i == cutoff + 470 ||
            i == cutoff + 523 ||
            i == cutoff + 571) {
                leftDebug.add(i, doAcc ? acc[i] : ang[i]);
            }
            if (i == cutoff + 498 ||
            i == cutoff + 549 ||
            i == cutoff + 599 ||
            i == cutoff + 386) {
                rightDebug.add(i, doAcc ? acc[i] : ang[i]);
            }


            if (i >= window) {
                angWindowEnd = ang[i - window];
            }
            angSlope = angWindowEnd - ang[i];

            if (acc[i] <= -G && accLast > -G) {
                //Contact detected
                if (doAcc) {
                    System.out.println("Step at frame " + i);
                }

                if (angSlope < 0) {
                    contactsLeft.add(i, doAcc ?  acc[i] : ang[i]);
                }
                else if (angSlope >= 0) {
                    contactsRight.add(i, doAcc ? acc[i] : ang[i]);
                }
            }

            accLast = acc[i];
        }

        return new XYSeries[]{contactsRight, contactsLeft, rightDebug, leftDebug};
    }

    /*Naive implementation*/
    public static XYSeries[] getContactEvents(XYSeries accSeries, XYSeries angSeries, boolean doAcc, double peakThreshold, double valleyThreshold, int window, int min_time) {
        if (accSeries.getItemCount() != angSeries.getItemCount() || window < 1 || min_time < 1)
            throw new IllegalArgumentException("Invalid params");

        //TODO: set it up such that if a second, stronger peak/valley is found at most x after the last, then the last peak/valley is replaced by the new one (attempts to remove pre-peaks nsht)
        XYSeries leftContacts = new XYSeries("Left Contacts");
        XYSeries rightContacts = new XYSeries("Right Contacts");
        XYSeries otherContacts = new XYSeries("Other Contacts");
        XYSeries leftLifts = new XYSeries("Left Lifts");
        XYSeries rightLifts = new XYSeries("Right Lifts");
        XYSeries otherLifts = new XYSeries("Other Lifts");
        double[] accValues = new double[accSeries.getItemCount()];
        double[] angValues = new double[angSeries.getItemCount()];

        for (int i = 0; i < accValues.length; i++) {
            accValues[i] = (double) accSeries.getY(i);
            angValues[i] = (double) angSeries.getY(i);
        }

        boolean peak_found, valley_found;
        StepSide lastStep = StepSide.UNKNOWN;
        int lastPeak = 0, lastValley = 0;

        for (int i = window; i < accValues.length - window; i++) {
            if (accValues[i] >= peakThreshold && i - lastPeak >= min_time) {
                peak_found = true;
                for (int j = i - window; j <= i + window; j++) {
                    if (i == j) continue;

                    if (accValues[i] < accValues[j]) {
                        peak_found = false;
                        break;
                    }
                }
            }
            else peak_found = false;

            if (accValues[i] <= valleyThreshold && i - lastValley >= min_time) {
                valley_found = true;
                for (int j = i - window; j <= i + window; j++) {
                    if (i == j) continue;

                    if (accValues[i] > accValues[j]) {
                        valley_found = false;
                        break;
                    }
                }
            }
            else valley_found = false;

            double value = doAcc ? accValues[i] : angValues[i];
            if (peak_found) {
                lastPeak = i;
                //check angular velocity direction
                if (angValues[i] >= angValues[i - 1] && angValues[i] <= angValues[i + 1]) {
                //if (angValues[i] > 0) {
                    //angular velocity rising -> right step
                    rightContacts.add(i, value);
                    lastStep = StepSide.RIGHT;
                }
                else if (angValues[i] < angValues[i - 1] && angValues[i] > angValues [i + 1]) {
                //else if (angValues[i] < 0) {
                    //angular velocity falling -> left step
                    leftContacts.add(i, value);
                    lastStep = StepSide.LEFT;
                }
                else {
                    //angular velocity is in a local maxima/minima -> side unsure
                    otherContacts.add(i, value);
                    lastStep = StepSide.UNKNOWN;
                }
            }

            if (valley_found) {
                lastValley = i;
                switch (lastStep) {
                    case LEFT:
                        leftLifts.add(i, value);
                        break;
                    case RIGHT:
                        rightLifts.add(i, value);
                        break;
                    case UNKNOWN:
                        otherLifts.add(i, value);
                        break;
                }
            }
        }

        return new XYSeries[]{leftContacts, rightContacts, otherContacts, leftLifts, rightLifts, otherLifts};
    }


    /*Complicated ass implementation
    * Taken from https://stackoverflow.com/questions/22583391/peak-signal-detection-in-realtime-timeseries-data/56174275#56174275
    * */
    public static XYSeries[] getContactEvents(XYSeries series, int lag, double peakInfluence, double threshold) {
        XYSeries contacts = new XYSeries("Peaks");
        List<Double> data = new ArrayList<>();

        for (int i = 0; i < series.getItemCount(); i++) {
            data.add((double) series.getY(i));
        }

        HashMap<String, List> peaks = analyzeDataForSignals(data, lag, threshold, peakInfluence);

        List<Integer> peak_indexes = peaks.get("signals");

        for (int i = 0; i < peak_indexes.size(); i++) {
            if (peak_indexes.get(i) == 1) {
                contacts.add(i, data.get(i));
            }
        }

        return new XYSeries[]{contacts};
    }

    private static HashMap<String, List> analyzeDataForSignals(List<Double> data, int lag, Double threshold, Double influence) {

        // init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        // the results (peaks, 1 or -1) of our algorithm
        List<Integer> signals = new ArrayList<Integer>(Collections.nCopies(data.size(), 0));

        // filter out the signals (peaks) from our original list (using influence arg)
        List<Double> filteredData = new ArrayList<Double>(data);

        // the current average of the rolling window
        List<Double> avgFilter = new ArrayList<Double>(Collections.nCopies(data.size(), 0.0d));

        // the current standard deviation of the rolling window
        List<Double> stdFilter = new ArrayList<Double>(Collections.nCopies(data.size(), 0.0d));

        // init avgFilter and stdFilter
        for (int i = 0; i < lag; i++) {
            stats.addValue(data.get(i));
        }
        avgFilter.set(lag - 1, stats.getMean());
        stdFilter.set(lag - 1, Math.sqrt(stats.getPopulationVariance())); // getStandardDeviation() uses sample variance
        stats.clear();

        // loop input starting at end of rolling window
        for (int i = lag; i < data.size(); i++) {

            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs((data.get(i) - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

                // this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (data.get(i) > avgFilter.get(i - 1)) {
                    signals.set(i, 1);
                } else {
                    signals.set(i, -1);
                }

                // filter this signal out using influence
                filteredData.set(i, (influence * data.get(i)) + ((1 - influence) * filteredData.get(i - 1)));
            } else {
                // ensure this signal remains a zero
                signals.set(i, 0);
                // ensure this value is not filtered
                filteredData.set(i, data.get(i));
            }

            // update rolling average and deviation
            for (int j = i - lag; j < i; j++) {
                stats.addValue(filteredData.get(j));
            }
            avgFilter.set(i, stats.getMean());
            stdFilter.set(i, Math.sqrt(stats.getPopulationVariance()));
            stats.clear();
        }

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);

        return returnMap;

    } // end

    private static double[] seriesToArray(XYSeries series) {
        double[] arr = new double[series.getItemCount()];

        for (int i = 0; i < series.getItemCount(); i++) {
            arr[i] = (double) series.getY(i);
        }

        return arr;
    }
}
