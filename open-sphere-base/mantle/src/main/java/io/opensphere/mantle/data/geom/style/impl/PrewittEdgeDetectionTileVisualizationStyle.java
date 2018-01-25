package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class PrewittEdgeDetectionTileVisualizationStyle.
 */
public class PrewittEdgeDetectionTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new Prewitt edge detection tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public PrewittEdgeDetectionTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public PrewittEdgeDetectionTileVisualizationStyle clone()
    {
        return (PrewittEdgeDetectionTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        PrewittEdgeDetectionTileVisualizationStyle aClone = clone();
        aClone.setDTIKey(dtiKey);
        aClone.initializeFromDataType();
        return aClone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/PrewittEdgeDetection.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Performs Prewitt edge detection on the pixels in the image.";
    }

    @Override
    public String getStyleName()
    {
        return "Prewitt Edge Detection";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new PrewittEdgeDetectionTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
