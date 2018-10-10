package io.opensphere.core.projection;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LineType;
import io.opensphere.core.model.Tessera;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.util.lang.Pair;

/**
 * A geographic projection which is mutable.
 */
public abstract class AbstractMutableGeographicProjection extends AbstractGeographicProjection
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractMutableGeographicProjection.class);

    /** Message for trying to illegally access a mutable projection. */
    protected static final String MUTABLE_PROJECTION_MSG = "Cannot project using non-snapshot mutable projection for terrain based positions.";

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(GeographicPosition start,
            GeographicPosition end, LineType type, Vector3d modelCenter)
    {
        if (start.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && end.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertLineToModel(start, end, type, modelCenter);
        }
        throw new UnsupportedOperationException(MUTABLE_PROJECTION_MSG);
    }

    @Override
    public Pair<Tessera<GeographicPosition>, ProjectionCursor> convertLineToModel(ProjectionCursor start, GeographicPosition end,
            LineType type, Vector3d modelCenter)
    {
        if (start.getVertex().getCoordinates().getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && end.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertLineToModel(start, end, type, modelCenter);
        }
        LOGGER.error(MUTABLE_PROJECTION_MSG);
        return null;
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        if (vert1.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && vert2.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && vert3.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && vert4.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertQuadToModel(vert1, vert2, vert3, vert4, modelCenter);
        }
        LOGGER.error(MUTABLE_PROJECTION_MSG);
        return null;
    }

    @Override
    public Vector3d convertToModel(GeographicPosition inPos, Vector3d modelCenter)
    {
        if (inPos.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertToModel(inPos, modelCenter);
        }
        LOGGER.error(MUTABLE_PROJECTION_MSG);
        return null;
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        if (altReference != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertToPosition(inPos, altReference);
        }
        LOGGER.error(MUTABLE_PROJECTION_MSG);
        return null;
    }

    @Override
    public TesseraList<? extends GeographicProjectedTesseraVertex> convertTriangleToModel(GeographicPosition vert1,
            GeographicPosition vert2, GeographicPosition vert3, Vector3d modelCenter)
    {
        if (vert1.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && vert2.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN
                && vert3.getLatLonAlt().getAltitudeReference() != ReferenceLevel.TERRAIN)
        {
            return getSnapshot().convertTriangleToModel(vert1, vert2, vert3, modelCenter);
        }
        LOGGER.error(MUTABLE_PROJECTION_MSG);
        return null;
    }
}
