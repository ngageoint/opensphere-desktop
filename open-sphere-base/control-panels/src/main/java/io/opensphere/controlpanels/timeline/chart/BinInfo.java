package io.opensphere.controlpanels.timeline.chart;

import java.awt.Point;
import java.awt.Rectangle;

import io.opensphere.controlpanels.timeline.chart.model.ChartBin;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModel;

/**
 * Container for information for rendering an individual bin.
 */
class BinInfo
{
    /** The bin. */
    private final ChartBin myBin;

    /** The layer model. */
    private final ChartLayerModel myLayerModel;

    /** The point. */
    private final Point myPoint;

    /** The rectangle. */
    private final Rectangle myRectangle;

    /**
     * Constructor.
     *
     * @param layerModel The layer model
     * @param bin The bin
     * @param rectangle The rectangle
     */
    public BinInfo(ChartLayerModel layerModel, ChartBin bin, Rectangle rectangle)
    {
        myLayerModel = layerModel;
        myBin = bin;
        myRectangle = rectangle;
        myPoint = new Point(myRectangle.x + (myRectangle.width >> 1), myRectangle.y);
    }

    /**
     * Gets the bin.
     *
     * @return the bin
     */
    public ChartBin getBin()
    {
        return myBin;
    }

    /**
     * Gets the layer model.
     *
     * @return the layer model
     */
    public ChartLayerModel getLayerModel()
    {
        return myLayerModel;
    }

    /**
     * Gets the point.
     *
     * @return the point
     */
    public Point getPoint()
    {
        return myPoint;
    }

    /**
     * Gets the rectangle.
     *
     * @return the rectangle
     */
    public Rectangle getRectangle()
    {
        return myRectangle;
    }
}
