import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class EventIdentifier {
    private static final double G = 9.80665;

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
        XYSeries rightDebug = new XYSeries("Right Lift");
        XYSeries leftDebug = new XYSeries("Left Lift");

        System.out.println("\n\nPrinting " + (doAcc ? "Acceleration" : "Angular Velocity") + "\n");

        boolean hasCutoff = false;
        int cutoff = 0;
        double zeroDelta = 1;
        for (int j = 0; j < size; j++) {
            int i = j;

            if (!hasCutoff) {
                if (acc[i] > zeroDelta || acc[i] < -zeroDelta) {
                    cutoff = i;
                    hasCutoff = true;
                    contactsRight.add(cutoff, doAcc ? acc[i] : ang[i]);
                }
            }
            else {
                if (i == cutoff + 470 ||
                i == cutoff + 523 ||
                i == cutoff + 571) {
                    contactsLeft.add(i, doAcc ? acc[i] : ang[i]);
                }
                if (i == cutoff + 498 ||
                i == cutoff + 549 ||
                i == cutoff + 599) {
                    contactsRight.add(i, doAcc ? acc[i] : ang[i]);
                }
            }
/*
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

*/
        }

        return new XYSeries[]{contactsRight, contactsLeft, rightDebug, leftDebug};
    }

    private static double[] seriesToArray(XYSeries series) {
        double[] arr = new double[series.getItemCount()];

        for (int i = 0; i < series.getItemCount(); i++) {
            arr[i] = (double) series.getY(i);
        }

        return arr;
    }
}
