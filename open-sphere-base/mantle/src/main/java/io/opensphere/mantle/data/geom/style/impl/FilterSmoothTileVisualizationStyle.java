package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class FilterSmoothTileVisualizationStyle.
 */
public class FilterSmoothTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new filter smooth tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public FilterSmoothTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public FilterSmoothTileVisualizationStyle clone()
    {
        return (FilterSmoothTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        FilterSmoothTileVisualizationStyle theClone = clone();
        theClone.setDTIKey(dtiKey);
        theClone.initializeFromDataType();
        return theClone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/Smooth.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Smoothes the pixels.";
    }

    @Override
    public String getStyleName()
    {
        return "Filter: Smooth";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new FilterSmoothTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
