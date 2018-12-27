package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * A simple style used to invert the color (essentially taking the compliment
 * within the color scale).
 */
public class InvertColorTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public InvertColorTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public InvertColorTileVisualizationStyle clone()
    {
        return (InvertColorTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        InvertColorTileVisualizationStyle clone = clone();
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
        return "/GLSL/InvertColor.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "The data pixels are displayed inverted.";
    }

    @Override
    public String getStyleName()
    {
        return "Invert Color";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new InvertColorTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
        /* intentionally blank */
    }
}
