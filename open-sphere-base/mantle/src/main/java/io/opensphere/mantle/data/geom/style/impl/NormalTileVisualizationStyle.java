package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class NormalTileVisualizationStyle.
 */
public class NormalTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new normal tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public NormalTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public NormalTileVisualizationStyle clone()
    {
        return (NormalTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        NormalTileVisualizationStyle zClone = clone();
        zClone.setDTIKey(dtiKey);
        zClone.initializeFromDataType();
        return zClone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
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
        return "Unaltered tiles from the source.";
    }

    @Override
    public String getStyleName()
    {
        return "Base";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new NormalTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        // Don't do anything, we don't alter parameters we just reset them.
    }
}
