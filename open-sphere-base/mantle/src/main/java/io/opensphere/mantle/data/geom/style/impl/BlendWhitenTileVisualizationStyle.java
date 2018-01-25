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
 * The Class BlendWhitenTileVisualizationStyle.
 */
public class BlendWhitenTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new blend whiten tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public BlendWhitenTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public BlendWhitenTileVisualizationStyle clone()
    {
        return (BlendWhitenTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        BlendWhitenTileVisualizationStyle clone = clone();
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
        return "Tiles blend to white, the more data types overlap the color gets closer to pure white.";
    }

    @Override
    public String getStyleName()
    {
        return "Blend: Whiten";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new BlendWhitenTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setBlendingParameters(TileRenderProperties trp)
    {
        trp.setBlending(new BlendingConfigGL(BlendFactor.SRC_ALPHA, BlendFactor.ONE, BlendFunction.FUNC_ADD, null));
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
