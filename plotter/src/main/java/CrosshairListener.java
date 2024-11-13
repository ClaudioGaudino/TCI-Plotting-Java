import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeries;

public class CrosshairListener implements ChartMouseListener, ChartProgressListener {

    private XYPlot currentPlot;
    public XYSeries selectedPoints;
    private boolean clicked;

    public CrosshairListener() {
        clicked = false;
        selectedPoints = new XYSeries("Punti Selezionati", false, false);
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
        currentPlot = chartMouseEvent.getChart().getXYPlot();
        clicked = true;
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

    }

    @Override
    public void chartProgress(ChartProgressEvent chartProgressEvent) {
        if(chartProgressEvent.getType() == ChartProgressEvent.DRAWING_FINISHED && chartProgressEvent.getChart().getXYPlot().equals(currentPlot) && clicked ) {
            clicked = false;

            try {
                selectedPoints.add(currentPlot.getDomainCrosshairValue(), currentPlot.getRangeCrosshairValue());
            } catch (SeriesException e) {
                if (e.getMessage().equals("X-value already exists.")) {
                    selectedPoints.remove(currentPlot.getDomainCrosshairValue());
                }
                else {
                    e.printStackTrace();
                }
            } finally {
                currentPlot = null;
            }
        }
    }
}
