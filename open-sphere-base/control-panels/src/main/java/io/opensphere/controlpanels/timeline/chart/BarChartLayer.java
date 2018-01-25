package io.opensphere.controlpanels.timeline.chart;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;

/**
 * Bar chart layer.
 */
class BarChartLayer extends HistogramChartLayer
{
    /**
     * Constructor.
     *
     * @param layerModels the layer models
     */
    public BarChartLayer(ChartLayerModels layerModels)
    {
        super(layerModels);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        Point cursorPosition = getUIModel().getCursorPosition();
        setSelectedBin(null);
        for (List<BinInfo> layerBins : getBinInfos())
        {
            for (BinInfo binInfo : layerBins)
            {
                if (binInfo.getLayerModel().isVisible() && binInfo.getBin().getCount() > 0)
                {
                    boolean isSelected = cursorPosition != null && binInfo.getRectangle().contains(cursorPosition);
                    if (isSelected)
                    {
                        setSelectedBin(binInfo);
                    }
                    g2d.setColor(isSelected ? binInfo.getLayerModel().getSelectedColor() : binInfo.getLayerModel().getColor());
                    g2d.fill(binInfo.getRectangle());
                }
            }
        }
    }
}
