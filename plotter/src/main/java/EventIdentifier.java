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

    /**
     * Finds the foot contact and foot lift events throughout a measurement of acceleration and angular velocity.
     * A contact event is identified by a peak (local maxima) in the acceleration values, while a lift event by a valley (local minima).
     * The algorithm is able to discern between right and left foot contacts based off of the corresponding trend in the angular velocity: rising means right contact, falling means left.
     * The side of a foot lift event is always assumed to be the same as the last contact's side.
     * In the case that a contact is identified in an instant where the angular velocity is in a local minima/maxima, the side cannot be clearly identified and will be set as unknown.
     * Uses default values for all other parameters.
     * @param accSeries
     * @param angSeries
     * @param doAcc
     * @return an array of series containing (in order):
     *  0 - the left contacts
     *  1 - the right contacts
     *  2 - the unknown side contacts
     *  3 - the left lifts
     *  4 - the right lifts
     *  5 - the unknown side lifts
     */
    public static XYSeries[] getContactEvents(XYSeries accSeries, XYSeries angSeries, boolean doAcc) {
        //return getContactEvents(accSeries, angSeries, doAcc, 14, 13, 1, 17, 7);
        return getContactEventsV2(accSeries, angSeries, doAcc, 14, 1, 17, 7);
    }

    /**
     * Finds the foot contact and foot lift events throughout a measurement of acceleration and angular velocity.
     * A contact event is identified by a peak (local maxima) in the acceleration values, while a lift event by peak placed just before the next contact (not guaranteed by this impl).
     * The algorithm is able to discern between right and left foot contacts based off of the corresponding trend in the angular velocity: rising means right contact, falling means left.
     * The side of a foot lift event is always assumed to be the same as the last contact's side.
     * In the case that a contact is identified in an instant where the angular velocity is in a local minima/maxima, the side cannot be clearly identified and will be set as unknown.
     * @param accSeries a series containing pairs (frame, value) representing the acceleration measurement (works best on acceleration magnitude)
     * @param angSeries a series containing pairs (frame, value) representing the angular velocity measurement along the Z axis after rotating the sensor frame of reference to the local one.
     * @param doAcc wether the returned values have to be acceleration (true) or angular velocity (false), only used for plotting.
     * @param peakThreshold the acceleration value that a peak must surpass in order to be treated as such.
     * @param valleyThreshold the acceleration value that a valley must surpass in order to be treated as such.
     * @param window the amount of measurements (before and after) that must be lower than the currently analyzed one in order for it to be considered a peak.
     * @param min_time the minimum amount of measurements that must be between two peaks and two valleys (a peak will only halt peak findings and vice versa for valleys)
     * @param replaceWindow the amount of measurements to check ahead for stronger peaks/valleys in order to avoid small peaks/valleys caused by noise from blocking the actual peaks/valleys to be detected.
     * @return an array of series containing (in order):
     *  0 - the left contacts
     *  1 - the right contacts
     *  2 - the unknown side contacts
     *  3 - the left lifts
     *  4 - the right lifts
     *  5 - the unknown side lifts
     */
    public static XYSeries[] getContactEvents(XYSeries accSeries, XYSeries angSeries, boolean doAcc, double peakThreshold, double valleyThreshold, int window, int min_time, int replaceWindow) {
        if (accSeries.getItemCount() != angSeries.getItemCount() || window < 1 || min_time < 1 || replaceWindow < 0)
            throw new IllegalArgumentException("Invalid params");

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
        int lastPeak = 0, lastValley = 0, first_contact = 0;

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

            if (accValues[i] >= valleyThreshold && i - lastValley >= min_time) {
                valley_found = true;
                for (int j = i - window; j <= i + window; j++) {
                    if (i == j) continue;

                    if (accValues[i] < accValues[j]) {
                        valley_found = false;
                        break;
                    }
                }
            }
            else valley_found = false;


            if (peak_found) {
                double value;
                int peakI = i;

                for (int j = i + 1; j <= i + replaceWindow && j < accValues.length; j++) {
                    if (accValues[peakI] < accValues[j]) {
                        peakI = j;
                    }
                }
                value = doAcc ? accValues[peakI] : angValues[peakI];
                lastPeak = peakI;

                if (first_contact == 0) first_contact = peakI;

                //check angular velocity direction
                if (angValues[peakI] >= angValues[peakI - 1] && angValues[peakI] <= angValues[peakI + 1]) {
                    //angular velocity rising -> right step
                    rightContacts.add(peakI, value);
                    lastStep = StepSide.RIGHT;
                    System.out.println("Right contact at frame " + peakI);
                }
                else if (angValues[peakI] < angValues[peakI - 1] && angValues[peakI] > angValues [peakI + 1]) {
                    //angular velocity falling -> left step
                    leftContacts.add(peakI, value);
                    lastStep = StepSide.LEFT;
                    System.out.println("Left contact at frame " + peakI);
                }
                else {
                    //angular velocity is in a local maxima/minima -> side unsure -> check previous step and alternate
                    switch (lastStep) {
                        case LEFT:
                            rightContacts.add(peakI, value);
                            lastStep = StepSide.RIGHT;
                            break;
                        case RIGHT:
                            leftContacts.add(peakI, value);
                            lastStep = StepSide.LEFT;
                            break;
                        case UNKNOWN:
                            otherContacts.add(peakI, value);
                            //no need to change lastStep as it already is "UNKNOWN"
                            break;
                    }
                    System.out.println("Unknown contact at frame " + peakI);
                }
            }

            if (valley_found) {
                /*if (first_valley) {
                    first_valley = false;
                    continue;
                }*/

                double value;
                int valleyI = i;

                /*for (int j = i + 1; j <= i + replaceWindow && j < accValues.length; j++) {
                    if (accValues[valleyI] < accValues[j]) {
                        valleyI = j;
                    }
                }*/

                if(valleyI <= first_contact) continue;

                value = doAcc ? accValues[valleyI] : angValues[valleyI];
                lastValley = valleyI;

                switch (lastStep) {
                    case LEFT:
                        leftLifts.add(valleyI, value);
                        System.out.println("Left lift at frame " + valleyI);
                        break;
                    case RIGHT:
                        rightLifts.add(valleyI, value);
                        System.out.println("Right lift at frame " + valleyI);
                        break;
                    case UNKNOWN:
                        otherLifts.add(valleyI, value);
                        System.out.println("Unknown lift at frame " + valleyI);
                        break;
                }
            }
        }

        return new XYSeries[]{leftContacts, rightContacts, otherContacts, leftLifts, rightLifts, otherLifts};
    }


    /**
     * Finds the foot contact and foot lift events throughout a measurement of acceleration and angular velocity.
     * A contact event is identified by a peak (local maxima) in the acceleration values, while a lift event by the valley placed just before the next contact.
     * The algorithm is able to discern between right and left foot contacts based off of the corresponding trend in the angular velocity: rising means right contact, falling means left.
     * The side of a foot lift event is always assumed to be the same as the corresponding contact's side.
     * In the case that a contact is identified in an instant where the angular velocity is in a local minima/maxima, the side is inferred by opposing it to the side of the previous contact.
     * If the side of a contact cannot be identified, and it is the first detected contact or the previous contact is also unidentified, then it will stay as such.
     * @param accSeries a series containing pairs (frame, value) representing the acceleration measurement (works best on acceleration magnitude)
     * @param angSeries a series containing pairs (frame, value) representing the angular velocity measurement along the Z axis after rotating the sensor frame of reference to the local one.
     * @param doAcc wether the returned values have to be acceleration (true) or angular velocity (false), only used for plotting.
     * @param peakThreshold the acceleration value that a peak must surpass in order to be treated as such.
     * @param window the amount of measurements (before and after) that must be lower than the currently analyzed one in order for it to be considered a peak.
     * @param min_time the minimum amount of measurements that must be between two peaks
     * @param replaceWindow the amount of measurements to check ahead for stronger peaks in order to avoid small peaks caused by noise from blocking the actual peaks to be detected.
     * @return an array of series containing (in order):
     *  0 - the left contacts
     *  1 - the right contacts
     *  2 - the unknown side contacts
     *  3 - the left lifts
     *  4 - the right lifts
     *  5 - the unknown side lifts
     */
    public static XYSeries[] getContactEventsV2(XYSeries accSeries, XYSeries angSeries, boolean doAcc, double peakThreshold, int window, int min_time, int replaceWindow) {
        if (accSeries.getItemCount() != angSeries.getItemCount() || window < 1 || min_time < 1 || replaceWindow < 0)
            throw new IllegalArgumentException("Invalid params");

        XYSeries leftContactsSeries = new XYSeries("Left Contacts");
        XYSeries rightContactsSeries = new XYSeries("Right Contacts");
        XYSeries otherContactsSeries = new XYSeries("Other Contacts");
        XYSeries leftLiftsSeries = new XYSeries("Left Lifts");
        XYSeries rightLiftsSeries = new XYSeries("Right Lifts");
        XYSeries otherLiftsSeries = new XYSeries("Other Lifts");
        double[] accValues = new double[accSeries.getItemCount()];
        double[] angValues = new double[angSeries.getItemCount()];

        for (int i = 0; i < accValues.length; i++) {
            accValues[i] = (double) accSeries.getY(i);
            angValues[i] = (double) angSeries.getY(i);
        }

        List<RunEvent> contacts = new ArrayList<>();
        List<RunEvent> lifts = new ArrayList<>();

        boolean peak_found, valley_found;
        StepSide lastStep = StepSide.UNKNOWN;
        int lastPeak = 0, first_contact = 0;

        //finds and collects all contact events
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
            } else peak_found = false;

            if (peak_found) {
                double value;
                int peakI = i;

                for (int j = i + 1; j <= i + replaceWindow && j < accValues.length; j++) {
                    if (accValues[peakI] < accValues[j]) {
                        peakI = j;
                    }
                }
                value = doAcc ? accValues[peakI] : angValues[peakI];
                lastPeak = peakI;

                if (first_contact == 0) first_contact = peakI;

                //check angular velocity direction to infer side
                if (angValues[peakI] >= angValues[peakI - 1] && angValues[peakI] <= angValues[peakI + 1]) {
                    //angular velocity rising -> right step
                    contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Right, peakI, value));
                    lastStep = StepSide.RIGHT;
                    System.out.println("Right contact at frame " + peakI);
                }
                else if (angValues[peakI] < angValues[peakI - 1] && angValues[peakI] > angValues [peakI + 1]) {
                    //angular velocity falling -> left step
                    contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Left, peakI, value));
                    lastStep = StepSide.LEFT;
                    System.out.println("Left contact at frame " + peakI);
                }
                else {
                    //angular velocity is in a local maxima/minima -> side unsure -> check previous step and alternate
                    switch (lastStep) {
                        case LEFT:
                            contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Right, peakI, value));
                            lastStep = StepSide.RIGHT;
                            break;
                        case RIGHT:
                            contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Left, peakI, value));
                            lastStep = StepSide.LEFT;
                            break;
                        case UNKNOWN:
                            contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Unknown, peakI, value));
                            //no need to change lastStep as it already is "UNKNOWN"
                            break;
                    }
                    System.out.println("Unknown contact at frame " + peakI);
                }
            }
        }

        int i;
        boolean firstContact = true;
        //goes backwards from each contact but the first and looks for the first valley, then marks it as a lift event
        for (int y = 0; y < contacts.size(); y++) {
            if (firstContact) {
                firstContact = false;
                continue;
            }

            RunEvent e = contacts.get(y);

            valley_found = false;
            i = e.getFrame() - 1;
            while (!valley_found) {
                valley_found = true;
                for (int j = i - window; j <= i + window; j++) {
                    if (i == j) continue;

                    if (accValues[i] > accValues[j]) {
                        valley_found = false;
                        i--;
                        break;
                    }
                }
            }

            double value;
            int valleyI = i;

            if(valleyI <= first_contact) continue;

            value = doAcc ? accValues[valleyI] : angValues[valleyI];

            switch (contacts.get(y-1).getSide()) {
                case Left:
                    lifts.add(new RunEvent(RunEvent.Type.Lift, RunEvent.Side.Left, valleyI, value));
                    System.out.println("Left lift at frame " + valleyI);
                    break;
                case Right:
                    lifts.add(new RunEvent(RunEvent.Type.Lift, RunEvent.Side.Right, valleyI, value));
                    System.out.println("Right lift at frame " + valleyI);
                    break;
                case Unknown:
                    lifts.add(new RunEvent(RunEvent.Type.Lift, RunEvent.Side.Unknown, valleyI, value));
                    System.out.println("Unknown lift at frame " + valleyI);
                    break;
            }
        }

        for (RunEvent e : contacts) {
            switch (e.getSide()) {
                case Left -> leftContactsSeries.add(e.getFrame(), e.getValue());
                case Right -> rightContactsSeries.add(e.getFrame(), e.getValue());
                case Unknown -> otherContactsSeries.add(e.getFrame(), e.getValue());
            }
        }

        for (RunEvent e : lifts) {
            switch (e.getSide()) {
                case Left -> leftLiftsSeries.add(e.getFrame(), e.getValue());
                case Right -> rightLiftsSeries.add(e.getFrame(), e.getValue());
                case Unknown -> otherLiftsSeries.add(e.getFrame(), e.getValue());

            }
        }

        return new XYSeries[]{leftContactsSeries, rightContactsSeries, otherContactsSeries, leftLiftsSeries, rightLiftsSeries, otherLiftsSeries};
    }

    /*
    Un contatto si verifica quando l'accelerazione è in discesa ed attraversa -G
    Il contatto è destro se la velocità angolare è in salita
    Il contatto è sinistro se la veloticà angolare è in discesa
     */
    public static XYSeries[] getContactEventsInitial(XYSeries accSeries, XYSeries angVelSeries, boolean doAcc) throws IllegalArgumentException {
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

    /*Complicated ass implementation
    * Taken from https://stackoverflow.com/questions/22583391/peak-signal-detection-in-realtime-timeseries-data/56174275#56174275
    * */
    public static XYSeries[] getContactEventsComplicated(XYSeries series, int lag, double peakInfluence, double threshold) {
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
