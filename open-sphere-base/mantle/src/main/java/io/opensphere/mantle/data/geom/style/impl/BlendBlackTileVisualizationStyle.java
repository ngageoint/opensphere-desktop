package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL.BlendFactor;
import io.opensphere.core.geometry.renderproperties.BlendingConfigGL.BlendFunction;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.core.geometry.renderproperties.TileRenderProperties;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class BlendBlackTileVisualizationStyle.
 */
public class BlendBlackTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new blend black tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public BlendBlackTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public BlendBlackTileVisualizationStyle clone()
    {
        return (BlendBlackTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        BlendBlackTileVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
        // TODO: Add this when enzio is done.
        return null;
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "The data pixels are displayed in black.";
    }

    @Override
    public String getStyleName()
    {
        return "Blend: Black";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new BlendBlackTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setBlendingParameters(TileRenderProperties trp)
    {
        trp.setBlending(new BlendingConfigGL(BlendFactor.ZERO, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFunction.FUNC_ADD,
                BlendFactor.ZERO, BlendFactor.ONE, BlendFunction.FUNC_ADD, null));
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
