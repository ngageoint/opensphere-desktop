package io.opensphere.core.map;

import java.util.List;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistry;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;

/**
 * Convenience implementation of {@link MapManager}.
 */
public class MapManagerAdapter extends SimpleMapContext<DynamicViewer> implements MapManager
{
    /**
     * Constructor.
     */
    public MapManagerAdapter()
    {
        super((ScreenViewer)null, (DynamicViewer)null);
    }

    @Override
    public Vector2i convertToPoint(GeographicPosition position)
    {
        return null;
    }

    @Override
    public GeographicPosition convertToPosition(Vector2i point, ReferenceLevel altReference)
    {
        return null;
    }

    @Override
    public ViewBookmarkRegistry getViewBookmarkRegistry()
    {
        return null;
    }

    @Override
    public List<GeographicPosition> getVisibleBoundaries()
    {
        return null;
    }

    @Override
    public GeographicBoundingBox getVisibleBoundingBox()
    {
        return null;
    }
}
