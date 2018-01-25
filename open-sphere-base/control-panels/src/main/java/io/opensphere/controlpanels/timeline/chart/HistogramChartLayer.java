package io.opensphere.controlpanels.timeline.chart;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import gnu.trove.map.hash.TIntIntHashMap;
import io.opensphere.controlpanels.timeline.AbstractTimelineLayer;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartBin;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModel;
import io.opensphere.controlpanels.timeline.chart.model.ChartLayerModels;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.awt.AWTUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Histogram chart layer.
 */
abstract class HistogramChartLayer extends AbstractTimelineLayer
{
    /** The bin infos. */
    private final List<List<BinInfo>> myBinInfos = New.list();

    /** Whether inputs to the bin info have changed. */
    private boolean myBinInputsChanged;

    /** Whether to stack the layers. */
    private boolean myIsStacking = true;

    /** The layer models. */
    private final ChartLayerModels myLayerModels;

    /** The selected bin info. */
    private BinInfo mySelectedBin;

    /** The viewport listener. */
    private ChangeListener<Object> myViewportListener;

    /**
     * Constructor.
     *
     * @param layerModels the layer models
     */
    public HistogramChartLayer(ChartLayerModels layerModels)
    {
        super();
        myLayerModels = layerModels;
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        String text;
        if (mySelectedBin == null)
        {
            text = incoming;
        }
        else
        {
            text = StringUtilities.concat("<html>", mySelectedBin.getLayerModel().getName(), "<br>Features: <b>",
                    String.valueOf(mySelectedBin.getBin().getCount()), "</b></html>");
        }
        return text;
    }

    @Override
    public void paint(Graphics2D g2d)
    {
        super.paint(g2d);

        if (myBinInputsChanged || myLayerModels.isChanged())
        {
            calculateBinInfos();
            myBinInputsChanged = false;
            myLayerModels.setChanged(false);
        }
    }

    /**
     * Sets whether to stack the layers.
     *
     * @param isStacking whether to stack the layers
     */
    public void setStacking(boolean isStacking)
    {
        myIsStacking = isStacking;
        myBinInputsChanged = true;
    }

    @Override
    public void setUIModel(TimelineUIModel model)
    {
        super.setUIModel(model);

        if (myViewportListener == null && model != null)
        {
            myViewportListener = new ChangeListener<Object>()
            {
                @Override
                public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue)
                {
                    myBinInputsChanged = true;
                }
            };
            model.getUISpan().addListener(myViewportListener);
            model.timelinePanelBoundsProperty().addListener(myViewportListener);
        }
    }

    /**
     * Gets the bin infos.
     *
     * @return the bin infos
     */
    protected List<List<BinInfo>> getBinInfos()
    {
        return myBinInfos;
    }

    /**
     * Sets the selected bin.
     *
     * @param selectedBin the selected bin
     */
    protected void setSelectedBin(BinInfo selectedBin)
    {
        mySelectedBin = selectedBin;
    }

    /**
     * Calculates the bin infos.
     */
    private void calculateBinInfos()
    {
        myBinInfos.clear();

        int maxCount = myIsStacking ? myLayerModels.getMaxCountStacked() : myLayerModels.getMaxCount();
        if (maxCount > 0)
        {
            TIntIntHashMap xToHeightMap = new TIntIntHashMap();
            for (ChartLayerModel layer : myLayerModels.getLayers())
            {
                List<BinInfo> layerBins = New.list(layer.getBins().size());
                for (ChartBin bin : layer.getBins())
                {
                    Rectangle rect = getRectangle(bin, maxCount);

                    // Perform stacking
                    if (myIsStacking)
                    {
                        if (xToHeightMap.containsKey(rect.x))
                        {
                            rect.translate(0, -xToHeightMap.get(rect.x));
                            xToHeightMap.adjustValue(rect.x, rect.height);
                        }
                        else
                        {
                            xToHeightMap.put(rect.x, rect.height);
                        }
                    }

                    layerBins.add(new BinInfo(layer, bin, rect));
                }
                myBinInfos.add(layerBins);
            }
        }
    }

    /**
     * Gets an AWT rectangle for the given bin.
     *
     * @param bin the bin
     * @param maxCount the maximum count
     * @return the AWT rectangle
     */
    private Rectangle getRectangle(ChartBin bin, int maxCount)
    {
        int x = getUIModel().timeToX(bin.getSpan().getStart());
        int width = MathUtil.subtractSafe(getUIModel().timeToX(bin.getSpan().getEnd()), x);
        int height = getUIModel().getTimelinePanelBounds().height * bin.getCount() / maxCount;
        int y = AWTUtilities.getMaxY(getUIModel().getTimelinePanelBounds()) - height;
        return new Rectangle(x, y, width, height);
    }
}
