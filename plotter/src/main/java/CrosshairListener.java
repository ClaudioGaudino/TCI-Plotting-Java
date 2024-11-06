import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;

import java.util.List;

public class CrosshairListener implements ChartMouseListener, ChartProgressListener {

    private final XYPlot xyPlot;
    private List<DoublePoint> selectedPoints;
    private boolean clicked;

    public CrosshairListener(XYPlot xyPlot) {
        this.xyPlot = xyPlot;
        clicked = false;
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
            System.out.println("CLICKED ON THE CHART OMG");
            System.out.println(xyPlot.getDomainCrosshairValue() + ", " + xyPlot.getRangeCrosshairValue());
            selectedPoints.add(new DoublePoint(xyPlot.getDomainCrosshairValue(), xyPlot.getRangeCrosshairValue()));
        }
    }
}
