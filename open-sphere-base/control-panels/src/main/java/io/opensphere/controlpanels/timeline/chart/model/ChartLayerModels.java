package io.opensphere.controlpanels.timeline.chart.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * A collection of chart layer models.
 */
public class ChartLayerModels
{
    /**
     * Whether the data have changed since the last time this was set to false.
     */
    private boolean myChanged;

    /** The layers. */
    private final List<ChartLayerModel> myLayers = New.list();

    /**
     * Adds a layer.
     *
     * @param layer the layer
     */
    public void addLayer(ChartLayerModel layer)
    {
        myLayers.add(layer);
        myChanged = true;
    }

    /**
     * Clears the layers.
     */
    public void clear()
    {
        myLayers.clear();
        myChanged = true;
    }

    /**
     * Gets the layers.
     *
     * @return the layers
     */
    public List<ChartLayerModel> getLayers()
    {
        return myLayers;
    }

    /**
     * Gets the maximum count in the layers.
     *
     * @return the maximum count
     */
    public int getMaxCount()
    {
        int maxMaxCount = 0;
        for (ChartLayerModel layer : myLayers)
        {
            int maxCount = layer.getMaxCount();
            if (maxCount > maxMaxCount)
            {
                maxMaxCount = maxCount;
            }
        }
        return maxMaxCount;
    }

    /**
     * Gets the maximum count in the layers when stacked.
     *
     * @return the maximum count (stacked)
     */
    public int getMaxCountStacked()
    {
        Map<TimeSpan, Integer> spanToCountMap = New.map();
        for (ChartLayerModel layer : myLayers)
        {
            for (ChartBin bin : layer.getBins())
            {
                if (!spanToCountMap.containsKey(bin.getSpan()))
                {
                    spanToCountMap.put(bin.getSpan(), Integer.valueOf(0));
                }
                spanToCountMap.put(bin.getSpan(), Integer.valueOf(spanToCountMap.get(bin.getSpan()).intValue() + bin.getCount()));
            }
        }
        return spanToCountMap.isEmpty() ? 0 : Collections.max(spanToCountMap.values()).intValue();
    }

    /**
     * Gets whether the data have changed since the last time setChanged() was
     * set to false.
     *
     * @return whether the data have changed
     */
    public boolean isChanged()
    {
        return myChanged || myLayers.stream().anyMatch(layer -> layer.isChanged());
    }

    /**
     * Removes layer.
     *
     * @param layer the layer
     */
    public void removeLayer(ChartLayerModel layer)
    {
        myLayers.remove(layer);
        myChanged = true;
    }

    /**
     * Reorders the layers based on their current order values.
     */
    public void reorder()
    {
        Collections.sort(myLayers, new Comparator<ChartLayerModel>()
        {
            @Override
            public int compare(ChartLayerModel layer1, ChartLayerModel layer2)
            {
                return Integer.compare(layer1.getOrder(), layer2.getOrder());
            }
        });
        myChanged = true;
    }

    /**
     * Marks the data as having changed or not as far as the caller is
     * concerned.
     *
     * @param changed whether the data have changed
     */
    public void setChanged(boolean changed)
    {
        myChanged = changed;
        if (!changed)
        {
            for (ChartLayerModel layer : myLayers)
            {
                layer.setChanged(changed);
            }
        }
    }
}
