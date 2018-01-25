package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class SobelEdgeDetectionTileVisualizationStyle.
 */
public class SobelEdgeDetectionTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new Sobel edge detection tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public SobelEdgeDetectionTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public SobelEdgeDetectionTileVisualizationStyle clone()
    {
        return (SobelEdgeDetectionTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        SobelEdgeDetectionTileVisualizationStyle cl = clone();
        cl.setDTIKey(dtiKey);
        cl.initializeFromDataType();
        return cl;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/SobelEdgeDetection.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Performs Sobel edge detection on the pixels in the image.";
    }

    @Override
    public String getStyleName()
    {
        return "Sobel Edge Detection";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new SobelEdgeDetectionTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet sps)
    {
    }
}
