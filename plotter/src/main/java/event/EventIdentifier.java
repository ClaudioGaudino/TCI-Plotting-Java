package event;

import enums.PaddleType;
import enums.Side;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.List;

public class EventIdentifier {

    //-- ROWING FUNCTIONS --//

    /**
     * Finds the instants in which the paddle hits the water and leaves it throughout a measurement of angular velocity.
     * The sensor is assumed to be placed at the CENTER point of the paddle.
     * The underwater phases are marked by periods in which the angular velocity maintains a semi-constant value.
     * The left underwater phase is found from the first valley lower than 0 to the last consecutive valley that is still lower than 0, as the rotation of the paddle is steady and counterclockwise (negative ang vel)
     * The right underwater phase is found from the first peak higher than 0 to the last consecutive peak that is still higher than 0, as the rotation of the paddle is steady and clockwise (positive ang vel)
     * @param zAngVel a list containing the value of the paddle's angular velocity around the Z (VERTICAL) axis, placed at the CENTER of the paddle.
     * @param window how far back and ahead of a given point to check wether it is a peak or valley
     * @param lowerThreshold the threshold under which the data points are considered to be in the left underwater section
     * @param upperThreshold the threshold over which the data points are considered to be in the right underwater section
     * @return a list of events ordered chronologically
     */
    public static List<PaddleEvent> getPaddlingEvents(List<Double> zAngVel, int window, double lowerThreshold, double upperThreshold) {
        if (lowerThreshold > upperThreshold)
            throw new IllegalArgumentException();

        List<PaddleEvent> events = new ArrayList<>();

        boolean inLeftZone = false, inRightZone = false;
        boolean justEntered = false;
        boolean foundValley, foundPeak;

        int tmpHit = 0, tmpLeave = 0;

        for (int i = 1; i < zAngVel.size(); i++) {
            if (zAngVel.get(i) < lowerThreshold && zAngVel.get(i - 1) >= lowerThreshold) {
                inLeftZone = true;
                justEntered = true;
            }
            if (zAngVel.get(i - 1) < lowerThreshold && zAngVel.get(i) >= lowerThreshold) {
                inLeftZone = false;
                tmpHit = 0;
            }
            if (zAngVel.get(i) > upperThreshold && zAngVel.get(i - 1) <= upperThreshold) {
                inRightZone = true;
                justEntered = true;
            }
            if (zAngVel.get(i - 1) > upperThreshold && zAngVel.get(i) <= upperThreshold) {
                inRightZone = false;
                tmpHit = 0;
            }

            if (inLeftZone) {
                if (justEntered) {
                    if (tmpHit != 0)
                        events.add(new PaddleEvent(PaddleType.HIT, Side.RIGHT, tmpHit));
                        //rightHit.add(new data.DataPair<>(tmpHit, zAngVel.get(tmpLeave)));
                    tmpHit = 0;
                    if (tmpLeave != 0)
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.RIGHT, tmpLeave));
                        //rightLeave.add(new data.DataPair<>(tmpLeave, zAngVel.get(tmpLeave)));
                    tmpLeave = 0;
                    justEntered = false;
                }

                foundValley = true;
                for (int j = i - window; j <= Math.min(i + window, zAngVel.size() - 1); j++) {
                    if (i == j) continue;

                    if (zAngVel.get(i) > zAngVel.get(j)) {
                        foundValley = false;
                        break;
                    }
                }

                if (foundValley) {
                    if (tmpHit == 0) {
                        tmpHit = i;
                        events.add(new PaddleEvent(PaddleType.HIT, Side.LEFT, i));
                        //leftHit.add(new data.DataPair<>(i, zAngVel.get(i)));
                    } else {
                        tmpLeave = i;
                    }
                }
            } else if (inRightZone) {
                if (justEntered) {
                    if (tmpHit != 0)
                        events.add(new PaddleEvent(PaddleType.HIT, Side.LEFT, tmpHit));
                        //leftHit.add(new data.DataPair<>(tmpHit, zAngVel.get(tmpLeave)));
                    tmpHit = 0;
                    if (tmpLeave != 0)
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.LEFT, tmpLeave));
                        //leftLeave.add(new data.DataPair<>(tmpLeave, zAngVel.get(tmpLeave)));
                    tmpLeave = 0;
                    justEntered = false;
                }

                foundPeak = true;
                for (int j = i - window; j <= Math.min(i + window, zAngVel.size() - 1); j++) {
                    if (i == j) continue;

                    if (zAngVel.get(i) < zAngVel.get(j)) {
                        foundPeak = false;
                        break;
                    }
                }

                if (foundPeak) {
                    if (tmpHit == 0) {
                        tmpHit = i;
                        events.add(new PaddleEvent(PaddleType.HIT, Side.RIGHT, i));
                    } else {
                        tmpLeave = i;
                    }
                }
            } else {
                if (tmpLeave != 0) {
                    if (zAngVel.get(tmpLeave) < 0)
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.LEFT, tmpLeave));
                    else
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.RIGHT, tmpLeave));
                    tmpLeave = 0;
                }
            }
        }

        return events;
    }

    public static List<PaddleEvent> getPaddlingEvents(List<Double> angVel, int window, double lowerThreshold, double upperThreshold, boolean useless) {
        if (lowerThreshold > upperThreshold)
            throw new IllegalArgumentException();

        List<PaddleEvent> events = new ArrayList<>();

        boolean inLeftZone = true, inRightZone = true, justEntered = false, eventPossible = false;
        int tmpLeaveR = 0, tmpLeaveL = 0;
        double curr;

        for (int i = 1; i < angVel.size(); i++) {
            curr = angVel.get(i);

            if (angVel.get(i - 1) > lowerThreshold && curr <= lowerThreshold) {
                if (inRightZone) {
                    inLeftZone = true;
                    justEntered = true;
                    inRightZone = false;

                    if (tmpLeaveR != 0) {
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.RIGHT, tmpLeaveR));
                        tmpLeaveR = 0;
                    }
                }
            }

            if (angVel.get(i - 1) < upperThreshold && curr >= upperThreshold) {
                if (inLeftZone) {
                    inRightZone = true;
                    justEntered = true;
                    inLeftZone = false;

                    if (tmpLeaveL != 0) {
                        events.add(new PaddleEvent(PaddleType.LEAVE, Side.LEFT, tmpLeaveL));
                        tmpLeaveL = 0;
                    }
                }
            }


            if (inLeftZone) {
                //If this is a valley in the left zone, then it might be an event
                eventPossible = true;
                for (int j = Math.max(i - window, 0); j <= Math.min(i + window, angVel.size() - 1); j++) {
                    if (j == i) continue;

                    if (curr > angVel.get(j)) {
                        eventPossible = false;
                        break;
                    }
                }

                if (eventPossible) {
                    //If we just entered this zone, we need to check how the previous zone worked out:
                    //it is entirely possible that the threshold was passed by just one anomalous peak that escaped filtering
                    //in that case the previous event to this will be a HIT, which is impossible to happen right before a second HIT (the one being currently detected)
                    //so we remove the previous HIT as well as the previous LEAVE and act as the zone change never happened
                    if (justEntered && events.get(events.size() - 1).type() != PaddleType.LEAVE) {
                        justEntered = false;
                        events.remove(events.size() - 1);
                        events.remove(events.size() - 1);
                    }

                    if (justEntered) {
                        events.add(new PaddleEvent(PaddleType.HIT, Side.LEFT, i));
                        justEntered = false;
                    } else {
                        tmpLeaveL = i;
                    }
                }
            }

            if (inRightZone) {
                eventPossible = true;
                for (int j = Math.max(i - window, 0); j <= Math.min(i + window, angVel.size() - 1); j++) {
                    if (j == i) continue;

                    if (curr < angVel.get(j)) {
                        eventPossible = false;
                        break;
                    }
                }

                if (eventPossible) {
                    if (justEntered && events.get(events.size() - 1).type() != PaddleType.LEAVE) {
                        justEntered = false;
                        events.remove(events.size() - 1);
                        events.remove(events.size() - 1);
                    }

                    if (justEntered) {
                        events.add(new PaddleEvent(PaddleType.HIT, Side.RIGHT, i));
                        justEntered = false;
                    } else {
                        tmpLeaveR = i;
                    }
                }
            }
        }


        return events;
    }

    //-- RUNNING FUNCTIONS --//

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
    public static List<RunEvent> getRunningEvents(XYSeries accSeries, XYSeries angSeries, boolean doAcc, double peakThreshold, int window, int min_time, int replaceWindow) {
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
        Side lastStep = Side.UNKNOWN;
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
                    lastStep = Side.RIGHT;
                    System.out.println("Right contact at frame " + peakI);
                }
                else if (angValues[peakI] < angValues[peakI - 1] && angValues[peakI] > angValues [peakI + 1]) {
                    //angular velocity falling -> left step
                    contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Left, peakI, value));
                    lastStep = Side.LEFT;
                    System.out.println("Left contact at frame " + peakI);
                }
                else {
                    //angular velocity is in a local maxima/minima -> side unsure -> check previous step and alternate
                    switch (lastStep) {
                        case LEFT:
                            contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Right, peakI, value));
                            lastStep = Side.RIGHT;
                            break;
                        case RIGHT:
                            contacts.add(new RunEvent(RunEvent.Type.Contact, RunEvent.Side.Left, peakI, value));
                            lastStep = Side.LEFT;
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

        return contacts;
    }
}
