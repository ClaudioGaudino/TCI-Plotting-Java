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

        double accDelta = 0, accLast = acc[0], accWindowEnd = acc[0];
        double angDelta = 0, angLast = ang[0], angWindowEnd = acc[0];

        int window = 1;

        XYSeries contactsRight = new XYSeries("Right Contacts");
        XYSeries contactsLeft = new XYSeries("Left Contacts");

        for (int i = 0; i < size; i++) {
            if (acc[i] <= -G && accLast > -G) {
                //Contact detected
                if (angDelta < 0) {
                    contactsLeft.add(i, doAcc ?  acc[i] : ang[i]);
                }
                else if (angDelta >= 0) {
                    contactsRight.add(i, doAcc ? acc[i] : ang[i]);
                }
            }
            accLast = acc[i];
            angLast = ang[i];

            if (i >= window) {
                angWindowEnd = ang[i - (window - 1)];
            }
            angDelta = angWindowEnd - Math.abs(ang[i]);
        }

        return new XYSeries[]{contactsRight, contactsLeft};
    }

    private static double[] seriesToArray(XYSeries series) {
        double[] arr = new double[series.getItemCount()];

        for (int i = 0; i < series.getItemCount(); i++) {
            arr[i] = (double) series.getY(i);
        }

        return arr;
    }
}
