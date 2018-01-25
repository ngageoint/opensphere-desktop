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
 * The Class BlendAddTileVisualizationStyle.
 */
public class BlendAddTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new blend add tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public BlendAddTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public BlendAddTileVisualizationStyle clone()
    {
        return (BlendAddTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtKey)
    {
        BlendAddTileVisualizationStyle c = clone();
        c.setDTIKey(dtKey);
        c.initializeFromDataType();
        return c;
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
        return "When different data types overlap the pixels are added together.  To rapidly find \"hidden\" data.";
    }

    @Override
    public String getStyleName()
    {
        return "Blend: Add";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new BlendAddTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setBlendingParameters(TileRenderProperties trp)
    {
        trp.setBlending(new BlendingConfigGL(BlendFactor.SRC_COLOR, BlendFactor.DST_COLOR, BlendFunction.FUNC_ADD,
                BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFunction.FUNC_ADD, null));
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
