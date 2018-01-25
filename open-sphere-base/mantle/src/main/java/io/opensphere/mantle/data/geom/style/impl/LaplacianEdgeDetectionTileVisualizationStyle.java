package io.opensphere.mantle.data.geom.style.impl;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.FragmentShaderProperties.ShaderPropertiesSet;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.tile.TileVisualizationSupport;

/**
 * The Class LaplacianEdgeDetectionTileVisualizationStyle.
 */
public class LaplacianEdgeDetectionTileVisualizationStyle extends AbstractTileVisualizationStyle
{
    /**
     * Instantiates a new laplacian edge detection tile visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public LaplacianEdgeDetectionTileVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    @Override
    public LaplacianEdgeDetectionTileVisualizationStyle clone()
    {
        return (LaplacianEdgeDetectionTileVisualizationStyle)super.clone();
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        LaplacianEdgeDetectionTileVisualizationStyle bClone = clone();
        bClone.setDTIKey(dtiKey);
        bClone.initializeFromDataType();
        return bClone;
    }

    @Override
    public Class<? extends TileVisualizationSupport> getConvertedClassType()
    {
        return TileVisualizationSupport.class;
    }

    @Override
    public String getShaderResourceLocation()
    {
        return "/GLSL/LaplacianEdgeDetection.glsl";
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.IMAGE_TILE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Performs laplacian edge detection on the pixels in the image.";
    }

    @Override
    public String getStyleName()
    {
        return "Laplacian Edge Detection";
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new LaplacianEdgeDetectionTileVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    @Override
    public void setShaderParameters(ShaderPropertiesSet props)
    {
    }
}
