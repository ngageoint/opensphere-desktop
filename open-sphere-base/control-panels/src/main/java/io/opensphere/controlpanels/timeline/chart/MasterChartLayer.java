package io.opensphere.controlpanels.timeline.chart;

import io.opensphere.controlpanels.timeline.CompositeLayer;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;

/**
 * The master chart layer.
 */
public class MasterChartLayer extends CompositeLayer
{
    /** The layer models. */
    private final ChartLayerModels myLayerModels = new ChartLayerModels();

    /** The current chart type. */
    private ChartType myChartType = ChartType.NONE;

    /** The bar chart layer. */
    private final BarChartLayer myBarLayer;

    /** The line chart layer. */
    private final LineChartLayer myLineLayer;

    /** The direct chart layer. */
    private final DirectChartLayer myDirectLayer;

    /**
     * Constructor.
     */
    public MasterChartLayer()
    {
        super();
        myBarLayer = new BarChartLayer(myLayerModels);
        myLineLayer = new LineChartLayer(myLayerModels);
        myDirectLayer = new DirectChartLayer(myLayerModels);
    }

    /**
     * Gets the chart type.
     *
     * @return the chart type
     */
    public ChartType getChartType()
    {
        return myChartType;
    }

    /**
     * Gets the layer models.
     *
     * @return the layer models
     */
    public ChartLayerModels getLayerModels()
    {
        return myLayerModels;
    }

    /**
     * Cycles through to the next chart type.
     *
     * @return the new chart type
     */
    public final ChartType nextChart()
    {
        myChartType = myChartType.next();
        updateChart();
        return myChartType;
    }

    /**
     * Sets the chart type.
     *
     * @param chartType the chart type
     */
    public void setChartType(ChartType chartType)
    {
        myChartType = chartType;
        updateChart();
    }

    /**
     * Updates the chart based on the current chart type.
     */
    private void updateChart()
    {
        clearLayers();
        myDirectLayer.setCurrentChart(false);
        switch (myChartType)
        {
            case LINE_OVERLAPPING:
                myLineLayer.setStacking(false);
                addLayer(myLineLayer);
                break;
            case LINE_STACKED:
                myLineLayer.setStacking(true);
                addLayer(myLineLayer);
                break;
            case BAR_OVERLAPPING:
                myBarLayer.setStacking(false);
                addLayer(myBarLayer);
                break;
            case BAR_STACKED:
                myBarLayer.setStacking(true);
                addLayer(myBarLayer);
                break;
            case DIRECT:
                myDirectLayer.setCurrentChart(true);
                break;
            case NONE:
                break;
            default:
        }
        if (myChartType != ChartType.NONE)
        {
            addLayer(myDirectLayer);
        }

        if (getUIModel() != null)
        {
            getUIModel().repaint();
        }
    }
}
