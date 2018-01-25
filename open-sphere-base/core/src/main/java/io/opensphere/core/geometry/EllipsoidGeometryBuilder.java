package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.List;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.PolygonMeshGeometry.Builder;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.util.collections.New;

/**
 * Builds an ellipsoid geometry given specific parameters describing the
 * ellipsoid.
 *
 * @param <T> The type of the position.
 */
public class EllipsoidGeometryBuilder<T extends Position> extends AbstractGeometry.Builder
{
    /**
     * The length of the major axis in meters.
     */
    private double myAxisAMeters;

    /**
     * The length of horizontal semi minor axis.
     */
    private double myAxisBMeters;

    /**
     * The length of the vertical semi minor axis.
     */
    private double myAxisCMeters;

    /**
     * The color of the ellipsoid geometry.
     */
    private Color myColor = Color.WHITE;

    /**
     * The heading in degrees of the ellipsoid. 0 is north, 90 is east.
     */
    private double myHeading;

    /**
     * The location of the center of the ellipsoid.
     */
    private T myLocation;

    /**
     * Used to convert {@link GeographicPosition} into {@link ModelPosition}.
     */
    private final MapManager myMapManager;

    /**
     * Generates the normal vectors for the ellipsoid polygon mesh.
     */
    private final EllipsoidNormalsGenerator myNormalsGenerator = new EllipsoidNormalsGenerator();

    /**
     * The pitch of the ellipsoid in degrees.
     */
    private double myPitch;

    /**
     * Represents the number of triangles to render the ellipsoid, the higher
     * the number the more triangles and the smoother the ellipsoid.
     */
    private int myQuality = 10;

    /**
     * The roll angle of the ellipsoid in degrees.
     */
    private double myRoll;

    /**
     * If the position is a {@link GeographicPosition}, used to convert to model
     * coordinates.
     */
    private final EllipsoidTransformProvider myTransformProvider;

    /**
     * Generates an ellipsoid polygon mesh based on the given inputs.
     */
    private final EllipsoidVertexGenerator myVertexGenerator = new EllipsoidVertexGenerator();

    /**
     * Constructs a new {@link EllipsoidGeometryBuilder}.
     *
     * @param mapManager Used to convert {@link GeographicPosition} into
     *            {@link ModelPosition}.
     */
    public EllipsoidGeometryBuilder(MapManager mapManager)
    {
        myTransformProvider = new EllipsoidTransformProvider(mapManager);
        myMapManager = mapManager;
    }

    /**
     * Gets the length of the major axis in meters.
     *
     * @return The length of the major axis in meters..
     */
    public double getAxisAMeters()
    {
        return myAxisAMeters;
    }

    /**
     * Gets the length of horizontal semi minor axis.
     *
     * @return the axisBMeters The length of horizontal semi minor axis.
     */
    public double getAxisBMeters()
    {
        return myAxisBMeters;
    }

    /**
     * Gets the length of the vertical semi minor axis.
     *
     * @return The length of the vertical semi minor axis.
     */
    public double getAxisCMeters()
    {
        return myAxisCMeters;
    }

    /**
     * Gets the color of the ellipsoid.
     *
     * @return The color of the ellipsoid.
     */
    public Color getColor()
    {
        return myColor;
    }

    /**
     * Gets the heading in degrees of the ellipsoid. 0 is north, 90 is east.
     *
     * @return the heading The heading in degrees of the ellipsoid. 0 is north,
     *         90 is east.
     */
    public double getHeading()
    {
        return myHeading;
    }

    /**
     * Gets the location of the center of the ellipsoid.
     *
     * @return the location The location of the center of the ellipsoid.
     */
    public T getLocation()
    {
        return myLocation;
    }

    /**
     * Gets the system {@link MapManager}.
     *
     * @return The system's {@link MapManager}.
     */
    public MapManager getMapManager()
    {
        return myMapManager;
    }

    /**
     * Gets the pitch of the ellipsoid in degrees.
     *
     * @return the pitch.
     */
    public double getPitch()
    {
        return myPitch;
    }

    /**
     * Represents the number of triangles to render the ellipsoid, the higher
     * the number the more triangles and the smoother the ellipsoid.
     *
     * @return The quality of the ellipsoid to render.
     */
    public int getQuality()
    {
        return myQuality;
    }

    /**
     * Gets the roll angle of the ellipsoid in degrees.
     *
     * @return the roll.
     */
    public double getRoll()
    {
        return myRoll;
    }

    /**
     * Sets The length of the major axis in meters.
     *
     * @param axisAMeters The length of the major axis in meters.
     */
    public void setAxisAMeters(double axisAMeters)
    {
        myAxisAMeters = axisAMeters;
    }

    /**
     * Sets the length of horizontal semi minor axis.
     *
     * @param axisBMeters The length of horizontal semi minor axis.
     */
    public void setAxisBMeters(double axisBMeters)
    {
        myAxisBMeters = axisBMeters;
    }

    /**
     * Sets the length of the vertical semi minor axis.
     *
     * @param axisCMeters The length of the vertical semi minor axis.
     */
    public void setAxisCMeters(double axisCMeters)
    {
        myAxisCMeters = axisCMeters;
    }

    /**
     * Sets the color of the ellipsoid.
     *
     * @param color The color of the ellipsoid.
     */
    public void setColor(Color color)
    {
        myColor = color;
    }

    /**
     * Sets the heading in degrees of the ellipsoid. 0 is north, 90 is east.
     *
     * @param heading the heading to set.
     */
    public void setHeading(double heading)
    {
        myHeading = heading;
    }

    /**
     * Sets the location of the center of the ellipsoid.
     *
     * @param location the location to set.
     */
    public void setLocation(T location)
    {
        myLocation = location;
    }

    /**
     * Sets the pitch of the ellipsoid in degrees.
     *
     * @param pitch the pitch to set.
     */
    public void setPitch(double pitch)
    {
        myPitch = pitch;
    }

    /**
     * Sets the quality to render the ellipsoid.
     *
     * @param quality Represents the number of triangles to render the
     *            ellipsoid, the higher the number the more triangles and the
     *            smoother the ellipsoid.
     */
    public void setQuality(int quality)
    {
        myQuality = quality;
    }

    /**
     * Sets the roll angle of the ellipsoid in degrees.
     *
     * @param roll the roll to set.
     */
    public void setRoll(double roll)
    {
        myRoll = roll;
    }

    /**
     * Gets the {@link Builder} that builds an ellipsoid polygon mesh based on
     * the parameters specified to this builder.
     *
     * @return The {@link Builder} to build the Polygon mesh that represents the
     *         ellipsoid specified to this builder.
     */
    protected PolygonMeshGeometry.Builder<Position> generatePolygonMeshBuilder()
    {
        PolygonMeshGeometry.Builder<Position> builder = new PolygonMeshGeometry.Builder<>();

        List<List<Position>> vertices = myVertexGenerator.generateVertices(myQuality, myAxisAMeters, myAxisBMeters,
                myAxisCMeters);
        List<List<Vector3d>> normals = myNormalsGenerator.calculateNormals(vertices);

        List<Position> allVertices = New.list();
        for (List<Position> strip : vertices)
        {
            allVertices.addAll(strip);
        }

        List<Vector3d> allNormals = New.list();
        for (List<Vector3d> stripNormals : normals)
        {
            allNormals.addAll(stripNormals);
        }

        List<Color> colors = New.list();
        for (int i = 0; i < allNormals.size(); i++)
        {
            colors.add(myColor);
        }

        builder.setColors(colors);
        builder.setNormals(allNormals);
        builder.setPolygonVertexCount(AbstractRenderer.TRIANGLE_STRIP_VERTEX_COUNT);
        builder.setPositions(allVertices);
        builder.setDataModelId(getDataModelId());

        return builder;
    }

    /**
     * Generates the transform matrix so the ellipsoid will show correctly on
     * the globe.
     *
     * @return The transform matrix to use in the render properties.
     */
    protected Matrix4d generateTransform()
    {
        Matrix4d transform = null;
        if (myLocation instanceof GeographicPosition)
        {
            transform = myTransformProvider.provideTransform(((GeographicPosition)myLocation).getLatLonAlt(), myHeading, myPitch,
                    myRoll);
        }
        else
        {
            transform = myTransformProvider.provideTransform(myLocation.asVector3d(), myHeading, myPitch, myRoll);
        }

        return transform;
    }
}
