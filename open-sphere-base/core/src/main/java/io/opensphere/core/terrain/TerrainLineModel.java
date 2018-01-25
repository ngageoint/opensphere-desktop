package io.opensphere.core.terrain;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/** Model a line along the terrain. */
public class TerrainLineModel
{
    /** Location by which to order. */
    private final TerrainVertex myBase;

    /** The geographic location of the base vertex. */
    private final Vector2d myBaseGeo;

    /** The body onto which this line is being generated. */
    private final GeographicBody3D myCelestialBody;

    /** The collection of vertices which make up the line. */
    private final Set<TerrainVertex> myVertices = new TreeSet<>(new VertexComparator());

    /** The geographic location of the containment vertex. */
    private final Vector2d myContainGeo;

    /**
     * The containment point is used to bound the valid range for vertices.
     * Points are not contained unless they are between the base and containment
     * points.
     */
    private final TerrainVertex myContainment;

    /** The distance between my base and containment points. */
    private final double myGeoSpan;

    /**
     * The origin of the model coordinate space for the line. Values which this
     * line model receives will already be adjusted to account for the model
     * center. Any calculations which require world coordinates will need to be
     * adjusted back.
     */
    private final Vector3d myModelCenter;

    /**
     * Constructor.
     *
     * @param celestialBody The body onto which this line is being generated.
     * @param base Location by which to order.
     * @param containment If non-null a point can be checked to determine
     *            whether it is within a valid range.
     * @param modelCenter The origin of the model coordinate space for the line.
     */
    public TerrainLineModel(GeographicBody3D celestialBody, TerrainVertex base, TerrainVertex containment, Vector3d modelCenter)
    {
        if (LatLonAlt.crossesAntimeridian(base.getCoordinates().getLatLonAlt(), containment.getCoordinates().getLatLonAlt()))
        {
            throw new IllegalArgumentException("The line model may not cross the antimeridian");
        }

        // The terrain vertices are already offset by the model center, so they
        // need to be adjusted back for any calculations.
        if (modelCenter == null)
        {
            myModelCenter = new Vector3d(0., 0., 0.);
        }
        else
        {
            myModelCenter = modelCenter;
        }
        myBase = base;
        myContainment = containment;
        myCelestialBody = celestialBody;
        if (myBase.getCoordinates() == null)
        {
            myBaseGeo = myCelestialBody
                    .convertToPosition(myBase.getModelCoordinates().add(myModelCenter), ReferenceLevel.ELLIPSOID).getLatLonAlt()
                    .asVec2d();
        }
        else
        {
            myBaseGeo = myBase.getGeographicPositionAsVector();
        }

        if (myContainment.getCoordinates() == null)
        {
            myContainGeo = myCelestialBody
                    .convertToPosition(myContainment.getModelCoordinates().add(myModelCenter), ReferenceLevel.ELLIPSOID)
                    .getLatLonAlt().asVec2d();
        }
        else
        {
            myContainGeo = myContainment.getGeographicPositionAsVector();
        }

        myGeoSpan = myBaseGeo.subtract(myContainGeo).getLength();
    }

    /**
     * Add a vertex to the model.
     *
     * @param vertex vertex to add
     * @param adjustAltitude when true, the altitude will be adjusted to be
     *            within the altitude span.
     */
    public void add(TerrainVertex vertex, boolean adjustAltitude)
    {
        Vector2d location;
        if (vertex.getCoordinates() == null)
        {
            location = myCelestialBody
                    .convertToPosition(vertex.getModelCoordinates().add(myModelCenter), ReferenceLevel.ELLIPSOID).getLatLonAlt()
                    .asVec2d();
        }
        else
        {
            location = vertex.getGeographicPositionAsVector();
        }
        if (contains(vertex, location))
        {
            if (adjustAltitude)
            {
                Vector3d modelCoords = vertex.getModelCoordinates().add(myModelCenter);

                double dist = myBaseGeo.subtract(location).getLength();

                double baseAlt = myBase.getCoordinates().getLatLonAlt().getAltM();
                double contAlt = myContainment.getCoordinates().getLatLonAlt().getAltM();

                double pct;
                if (baseAlt > contAlt)
                {
                    pct = 1 - dist / myGeoSpan;
                }
                else
                {
                    pct = dist / myGeoSpan;
                }
                double altChange = Math.abs(contAlt - baseAlt);
                double adjust = Math.min(baseAlt, contAlt) + altChange * pct;

                Vector3d direction = modelCoords.getNormalized();
                vertex.setModelCoordinates(modelCoords.add(direction.multiply(adjust)).subtract(myModelCenter));
            }

            myVertices.add(vertex);
        }
    }

    /**
     * Add all of the vertices.
     *
     * @param vertices The vertices to add.
     * @param adjustAltitude when true, the altitude will be adjusted to be
     *            within the altitude span.
     */
    public void addAll(Collection<TerrainVertex> vertices, boolean adjustAltitude)
    {
        for (TerrainVertex vertex : vertices)
        {
            add(vertex, adjustAltitude);
        }
    }

    /**
     * Get the vertices which form this line.
     *
     * @return the vertices which form this line
     */
    public List<TerrainVertex> getVertices()
    {
        return New.list(myVertices);
    }

    /**
     * Check to see whether the point is between my base and containment points.
     *
     * @param vertex vertex to check for containment.
     * @param location the geographic location of the vertex.
     * @return true when the vertex is contained.
     */
    private boolean contains(TerrainVertex vertex, Vector2d location)
    {
        // TODO should we check to see of the position has already been added?
        double distBase = location.subtract(myBaseGeo).getLength();
        if (distBase > myGeoSpan)
        {
            return false;
        }

        double distCont = location.subtract(myContainGeo).getLength();
        return distCont <= myGeoSpan;
    }

    /**
     * Compare vertices based on their distance from the base location.
     */
    public class VertexComparator implements Comparator<TerrainVertex>
    {
        @Override
        public int compare(TerrainVertex o1, TerrainVertex o2)
        {
            if (Utilities.sameInstance(o1, o2))
            {
                return 0;
            }

            double dist1 = o1.getModelCoordinates().subtract(myBase.getModelCoordinates()).getLength();
            double dist2 = o2.getModelCoordinates().subtract(myBase.getModelCoordinates()).getLength();

            if (MathUtil.isZero(dist1 - dist2, MathUtil.DBL_LARGE_EPSILON))
            {
                return 0;
            }

            return dist1 < dist2 ? -1 : 1;
        }
    }
}
