package io.opensphere.core.viewer.impl;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.Projection.ProjectionCursor;
import io.opensphere.core.util.lang.Pair;

/**
 * Helper class that handles conversions between different coordinate systems.
 */
public class PositionConverter
{
    /** Reference to my map context. */
    private final MapContext<?> myMapContext;

    /**
     * Constructor.
     *
     * @param mapContext The map context to use.
     */
    public PositionConverter(MapContext<?> mapContext)
    {
        myMapContext = mapContext;
    }

    /**
     * Convert geographic lines to model coordinates.
     *
     * @param positions The positions that make up the lines.
     * @param limit The number of positions to use in the list.
     * @param type The type of line desired.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The list of model coordinates.
     */
    public List<Vector3d> convertLinesToModel(List<? extends GeographicPosition> positions, int limit, LineType type,
            Projection projection, Vector3d modelCenter)
    {
        Projection projToUse = projection;
        if (projToUse == null)
        {
            projToUse = myMapContext.getProjection();
        }
        return projToUse.convertLinesToModel(positions, limit, type, modelCenter);
    }

    /**
     * Convert lines to model coordinates. If the lines are in geographic
     * coordinates, the lines will be tessellated appropriately.
     *
     * @param vertices The vertices of the lines.
     * @param positionType The position type for the vertices.
     * @param lineType The type of line desired.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The list of model coordinates.
     */
    public List<Vector3d> convertLinesToModel(List<? extends Position> vertices, Class<? extends Position> positionType,
            LineType lineType, Projection projection, Vector3d modelCenter)
    {
        if (GeographicPosition.class.isAssignableFrom(positionType))
        {
            @SuppressWarnings("unchecked")
            List<? extends GeographicPosition> geographicVertices = (List<? extends GeographicPosition>)vertices;
            return convertLinesToModel(geographicVertices, vertices.size() + 1, lineType, projection, modelCenter);
        }
        return convertPositionsToModel(vertices, projection, modelCenter);
    }

    /**
     * Convert a geographic line to a {@link Tessera}.
     *
     * @param start The start of the line.
     * @param end The end of the line.
     * @param type The type of line desired.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return A Tessera and a Projection cursor.
     */
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
            GeographicPosition end, LineType type, Projection projection, Vector3d modelCenter)
    {
        Projection projToUse = projection;
        if (projToUse == null)
        {
            projToUse = myMapContext.getProjection();
        }
        return projToUse.convertLineToModel(start, end, type, modelCenter);
    }

    /**
     * Convert a geographic line to a {@link Tessera}.
     *
     * @param start The start of the line.
     * @param end The end of the line.
     * @param type The type of line desired.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return A Tessera and a Projection cursor.
     */
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Projection projection, Vector3d modelCenter)
    {
        Projection projToUse = projection;
        if (projToUse == null)
        {
            projToUse = myMapContext.getProjection();
        }
        return projToUse.convertLineToModel(start, end, type, modelCenter);
    }

    /**
     * Convert 3-D model coordinates to window coordinates.
     *
     * @param model The model coordinates.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The window coordinates.
     */
    public Vector3d convertModelToWindow(Vector3d model, Vector3d modelCenter)
    {
        if (model == null)
        {
            return Vector3d.ORIGIN;
        }
        return myMapContext.getStandardViewer().modelToWindowCoords(model.add(modelCenter));
    }

    /**
     * Convert a list of positions to a list of model vectors.
     *
     * @param positions The positions.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The list of model vectors.
     */
    public List<Vector3d> convertPositionsToModel(List<? extends Position> positions, Projection projection, Vector3d modelCenter)
    {
        List<Vector3d> positionList = new ArrayList<>(positions.size());
        for (Position pos : positions)
        {
            positionList.add(convertPositionToModel(pos, projection, modelCenter));
        }

        return positionList;
    }

    /**
     * Convert a position to a model vector.
     *
     * @param position The position.
     * @param projection The projection to used for conversion.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The model vector.
     */
    public Vector3d convertPositionToModel(Position position, Projection projection, Vector3d modelCenter)
    {
        if (position instanceof ScreenPosition)
        {
            return convertPositionToModel((ScreenPosition)position, modelCenter);
        }
        else if (position instanceof ModelPosition)
        {
            if (modelCenter == null)
            {
                return position.asVector3d();
            }
            else
            {
                return position.asVector3d().add(modelCenter);
            }
        }
        else if (position instanceof GeographicPosition)
        {
            Projection projToUse = projection;
            if (projToUse == null)
            {
                projToUse = myMapContext.getProjection();
            }
            if (projToUse == null)
            {
                // TODO this doesn't happen on trunk right now.
                return null;
            }
            return projToUse.convertToModel((GeographicPosition)position, modelCenter);
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized position type: " + position.getClass().getName());
        }
    }

    /**
     * Convert a screen position to a model vector.
     *
     * @param position The position.
     * @param modelCenter The model center used for conversion. This may not be
     *            the model center for the projection in some cases. For
     *            example, some calculations are always done in world
     *            coordinates to allow compatible calculations with the viewer.
     * @return The model vector.
     */
    public Vector3d convertPositionToModel(ScreenPosition position, Vector3d modelCenter)
    {
        double x = position.getX();
        // Flip the y.
        double y = -position.getY();
        if (x < 0.)
        {
            x += myMapContext.getScreenViewer().getViewportWidth() + 1;
        }
        if (y <= 0.)
        {
            y += myMapContext.getScreenViewer().getViewportHeight();
        }
        else
        {
            --y;
        }
        // TODO does the modelCenter need to be flipped before being applied?
        return new Vector3d(x, y, 0.);
    }

    /**
     * Convert a position to window coordinates.
     *
     * @param position The position.
     * @param projection The projection to used for conversion.
     * @return The window coordinates.
     */
    public Vector3d convertPositionToWindow(Position position, Projection projection)
    {
        return convertModelToWindow(convertPositionToModel(position, projection, Vector3d.ORIGIN), Vector3d.ORIGIN);
    }
}
