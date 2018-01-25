package io.opensphere.wms.state.save.controllers;

import java.util.Collection;
import java.util.List;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.TileLevelController;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;
import io.opensphere.wms.layer.WMSLayerValueProvider;
import io.opensphere.wms.state.model.Parameters;
import io.opensphere.wms.state.model.WMSLayerAndState;
import io.opensphere.wms.state.model.WMSLayerState;

/**
 * Retrieves all necessary informtion from a given set of layers and creates
 * state models containing the values specific to the set of layers.
 */
public class LayersToStateModels
{
    /**
     * The mantle toolbox.
     */
    private final MantleToolbox myMantleToolbox;

    /**
     * Constructs a new layers to state model.
     *
     * @param mantleToolbox the mantle toolbox used to get the visualization
     *            style registry.
     */
    public LayersToStateModels(MantleToolbox mantleToolbox)
    {
        myMantleToolbox = mantleToolbox;
    }

    /**
     * Takes the values to restore in a layer for the specified layers and
     * applies them to new state models.
     *
     * @param layers The layers to get the restore values from.
     * @return The state models containing restore values for each layer.
     */
    public List<WMSLayerAndState> toStateModels(Collection<? extends WMSLayerValueProvider> layers)
    {
        List<WMSLayerAndState> stateModels = New.list();

        for (WMSLayerValueProvider layer : layers)
        {
            WMSLayerState state = new WMSLayerState();
            VisualizationStyle style = myMantleToolbox.getVisualizationStyleRegistry().getStyle(TileVisualizationSupport.class,
                    layer.getTypeInfo().getTypeKey(), true);
            state.setColorizeStyle(style.getStyleName());
            state.setFixedHeight(layer.getConfiguration().getFixedHeight() != null
                    && !layer.getConfiguration().getFixedHeight().equals(Integer.valueOf(0)));
            state.setFixedWidth(layer.getConfiguration().getFixedWidth() != null
                    && !layer.getConfiguration().getFixedWidth().equals(Integer.valueOf(0)));
            state.setGetMapUrl(layer.getConfiguration().getGetMapConfig().getUsableGetMapURL());

            MapVisualizationInfo mapVisInfo = layer.getTypeInfo().getMapVisualizationInfo();
            if (mapVisInfo != null)
            {
                TileLevelController leveler = mapVisInfo.getTileLevelController();
                if (leveler != null && leveler.isDivisionOverride())
                {
                    state.setHoldLevel(Integer.valueOf(leveler.getDivisionHoldGeneration()));
                }
            }

            float alpha = layer.getTypeInfo().getBasicVisualizationInfo().getTypeOpacity()
                    / (float)ColorUtilities.COLOR_COMPONENT_MAX_VALUE;
            state.setAlpha(alpha);

            state.setId(layer.getTypeInfo().getTypeKey());
            state.setMaxDisplaySize(layer.getMaximumDisplaySize());
            state.setMinDisplaySize(layer.getMinimumDisplaySize());
            state.setSplitLevels(layer.getConfiguration().getDisplayConfig().getResolveLevels());
            state.setType(layer.getConfiguration().getLayerType().toString());
            state.setUrl(layer.getTypeInfo().getUrl());
            state.setVisible(layer.getTypeInfo().isVisible());
            state.setTitle(layer.getTypeInfo().getDisplayName());

            BasicVisualizationInfo basic = layer.getTypeInfo().getBasicVisualizationInfo();
            if (basic != null && basic.getLoadsTo().isTimelineEnabled())
            {
                state.setIsAnimate(true);
            }

            fillParameters(state, layer);

            stateModels.add(new WMSLayerAndState(layer, state));
        }

        return stateModels;
    }

    /**
     * Sets the values in the parameters state model.
     *
     * @param state The overall layer state.
     * @param layer The layer to save state for.
     */
    private void fillParameters(WMSLayerState state, WMSLayerValueProvider layer)
    {
        Parameters parameters = state.getParameters();

        parameters.setLayerName(layer.getConfiguration().getLayerName());
        parameters.setBgColor(layer.getConfiguration().getGetMapConfig().getBGColor());
        parameters.setCustom(layer.getConfiguration().getGetMapConfig().getCustomParams());
        parameters.setFormat(layer.getConfiguration().getGetMapConfig().getImageFormat());
        parameters.setHeight(layer.getConfiguration().getGetMapConfig().getTextureHeight());
        parameters.setWidth(layer.getConfiguration().getGetMapConfig().getTextureWidth());
        parameters.setSrs(layer.getConfiguration().getGetMapConfig().getSRS());
        parameters.setStyle(layer.getConfiguration().getGetMapConfig().getStyle());
        parameters.setTransparent(layer.getConfiguration().getGetMapConfig().getTransparent());
    }
}
