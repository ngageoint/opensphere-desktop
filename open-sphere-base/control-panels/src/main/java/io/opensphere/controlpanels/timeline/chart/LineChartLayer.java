package io.opensphere.controlpanels.timeline.chart;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;
import io.opensphere.core.model.time.TimeSpan;

/**
 * Line chart layer.
 */
class LineChartLayer extends HistogramChartLayer
{
    /**
     * Constructor.
     *
     * @param layerModels the layer models
     */
    public LineChartLayer(ChartLayerModels layerModels)
    {
        super(layerModels);
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        Point cursorPosition = getUIModel().getCursorPosition();
        setSelectedBin(null);
        Rectangle rect = new Rectangle();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (List<BinInfo> layerBins : getBinInfos())
        {
            ChartLayerModel layerModel = null;
            if (!layerBins.isEmpty())
            {
                layerModel = layerBins.get(0).getLayerModel();
            }

            if (layerModel != null && layerModel.isVisible())
            {
                TimeSpan extents = layerModel.getExtents();
                boolean hadOverlap = false;
                for (int i = 0; i < layerBins.size(); ++i)
                {
                    BinInfo binInfo = layerBins.get(i);
                    if (binInfo.getBin().getSpan().overlaps(extents))
                    {
                        rect.setBounds(binInfo.getPoint().x - DOT_PADDING, binInfo.getPoint().y - DOT_PADDING, DOT_CIRCUMFERENCE,
                                DOT_CIRCUMFERENCE);

                        boolean isSelected = cursorPosition != null && rect.contains(cursorPosition);
                        if (isSelected)
                        {
                            setSelectedBin(binInfo);
                        }

                        // Draw the line
                        if (i > 0)
                        {
                            BinInfo prevBinInfo = layerBins.get(i - 1);
                            g2d.setColor(layerModel.getColor());
                            g2d.drawLine(prevBinInfo.getPoint().x, prevBinInfo.getPoint().y, binInfo.getPoint().x,
                                    binInfo.getPoint().y);
                        }

                        // Draw the dot
                        if (binInfo.getBin().getCount() > 0)
                        {
                            g2d.setColor(isSelected ? layerModel.getSelectedColor() : layerModel.getColor());
                            g2d.fillOval(rect.x, rect.y, rect.width, rect.height);
                        }

                        hadOverlap = true;
                    }
                    else if (hadOverlap)
                    {
                        // Draw the final line segment
                        BinInfo prevBinInfo = layerBins.get(i - 1);
                        g2d.setColor(layerModel.getColor());
                        g2d.drawLine(prevBinInfo.getPoint().x, prevBinInfo.getPoint().y, binInfo.getPoint().x,
                                binInfo.getPoint().y);
                        break;
                    }
                }
            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }
}
