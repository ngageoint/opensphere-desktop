package io.opensphere.core.hud.framework;

import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;
import io.opensphere.core.viewer.impl.Viewer3D;

/**
 * A window whose standard model viewer is a 3d viewer.
 *
 * @param <S> Layout constraint type
 * @param <T> Layout type
 */
public abstract class Window3D<S extends LayoutConstraints, T extends AbstractLayout<S>> extends Window<S, T>
{
    /**
     * Constructor.
     *
     * @param hudTransformer The transformer helper for publishing geometries.
     * @param location The size of the window onscreen.
     * @param geographicLocation The location of the window geographically.
     * @param zOrder the z-order for the window.
     */
    public Window3D(TransformerHelper hudTransformer, ScreenBoundingBox location, GeographicBoundingBox geographicLocation,
            int zOrder)
    {
        super(hudTransformer, location, geographicLocation, zOrder);
    }

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer helper for publishing geometries.
     * @param location The size of the window onscreen.
     * @param zOrder the z-order for the window.
     */
    public Window3D(TransformerHelper hudTransformer, ScreenBoundingBox location, int zOrder)
    {
        super(hudTransformer, location, zOrder);
    }

    /**
     * Constructor.
     *
     * @param hudTransformer The transformer helper for publishing geometries.
     * @param size The size of the window onscreen.
     * @param location The Location of the tool within the window.
     * @param resize Resize options.
     * @param zOrder the z-order for the window.
     */
    public Window3D(TransformerHelper hudTransformer, ScreenBoundingBox size, ToolLocation location, ResizeOption resize,
            int zOrder)
    {
        super(hudTransformer, size, location, resize, zOrder);
    }

    @Override
    public MapContext<Viewer3D> createMapContext()
    {
        ScreenBoundingBox frameBox = getAbsoluteLocation();

        Viewer3D modelViewer = new Viewer3D(new Viewer3D.Builder(), false);

        MapContext<Viewer3D> mapContext = new SimpleMapContext<>(new ScreenViewer(), modelViewer);
        mapContext.reshape((int)frameBox.getWidth(), (int)frameBox.getHeight());
        mapContext.getStandardViewer().setViewOffset(frameBox.getUpperLeft());

        return mapContext;
    }
}
