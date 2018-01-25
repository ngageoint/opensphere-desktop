package io.opensphere.wms.state.activate.controllers;

import com.bitsys.fade.mist.state.v4.LayerType;

import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.util.lang.BooleanUtilities;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.wms.state.model.WMSLayerState;

/** Translates v4 objects to v3 for activation. */
public final class SaveStateV4ToV3Translator
{
    /**
     * Converts a LayerType to a WFSLayerState.
     *
     * @param layerV4 the layer type
     * @return the layer state
     */
    public static WMSLayerState toLayerType(LayerType layerV4)
    {
        WMSLayerState layer = new WMSLayerState();
        layer.setAlpha(NumberUtilities.toFloat(layerV4.getAlpha(), 1));
        layer.setColorizeStyle(layerV4.getColorizeStyle());
        layer.setFixedHeight(BooleanUtilities.toBoolean(layerV4.isFixedHeight()));
        layer.setFixedWidth(BooleanUtilities.toBoolean(layerV4.isFixedWidth()));
        layer.setGetMapUrl(layerV4.getGetMapUrl());
        layer.setId(layerV4.getId());
        layer.setIsAnimate(Boolean.parseBoolean(layerV4.getAnimate()));
        layer.setLayerType(layerV4.getType().toLowerCase());
        layer.setMaxDisplaySize(NumberUtilities.toInt(layerV4.getMaxDisplaySize()));
        layer.setMinDisplaySize(NumberUtilities.toInt(layerV4.getMinDisplaySize()));
        if (layerV4.getParams() != null)
        {
            layer.getParameters().setLayerName(StateUtilities.getValue(layerV4.getParams().getAny(), "layers"));
        }
        layer.setSplitLevels(Integer.valueOf(NumberUtilities.parseInt(layerV4.getSplitLevels(), 18)));
        if (layerV4.getTags() != null)
        {
            layer.getTags().addAll(layerV4.getTags().getTag());
        }
        layer.setType(layerV4.getLayerType());
        layer.setTitle(layerV4.getTitle());
        layer.setUrl(layerV4.getUrl());
        layer.setVisible(layerV4.isVisible());
        // MIST web saves:
        // base color
        // color control
        // date format
        // extents
        // data provider
        // styles
        // time format
        // projections
        // urls
        // min/max xoom
        // style
        return layer;
    }

    /** Disallow instantiation. */
    private SaveStateV4ToV3Translator()
    {
    }
}
