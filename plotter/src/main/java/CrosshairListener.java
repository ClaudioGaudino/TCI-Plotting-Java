import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;
import java.util.List;

public class CrosshairListener implements ChartMouseListener, ChartProgressListener {

    private final XYPlot xyPlot;
    public XYSeries selectedPoints;
    private boolean clicked;

    public CrosshairListener(XYPlot xyPlot) {
        this.xyPlot = xyPlot;
        clicked = false;
        selectedPoints = new XYSeries("Punti Selezionati", false, false);
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
        clicked = true;
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

    }

    @Override
    public void chartProgress(ChartProgressEvent chartProgressEvent) {
        if(chartProgressEvent.getType() == ChartProgressEvent.DRAWING_FINISHED && clicked) {
            clicked = false;
            //System.out.println(xyPlot.getDomainCrosshairValue() + ", " + xyPlot.getRangeCrosshairValue());
            try {
                selectedPoints.add(xyPlot.getDomainCrosshairValue(), xyPlot.getRangeCrosshairValue());
            } catch (SeriesException e) {
                if (e.getMessage().equals("X-value already exists.")) {
                    selectedPoints.remove(xyPlot.getDomainCrosshairValue());
                }
                else {
                    e.printStackTrace();
                }
            }
        }
    }
}
