package io.opensphere.stkterrain.transformer;

import io.opensphere.core.image.Image;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.terrain.util.ElevationImageReader;
import io.opensphere.core.terrain.util.ElevationImageReaderException;
import io.opensphere.stkterrain.model.mesh.GeographicQuantizedMeshReader;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.util.Constants;

/**
 * An {@link ElevationImageReader} that will read elevations for a
 * {@link QuantizedMesh}.
 */
public class STKElevationImageReader implements ElevationImageReader
{
    /**
     * The bounds of this reader.
     */
    private GeographicBoundingBox myBounds;

    /**
     * The order id.
     */
    private String myOrderId;

    /**
     * The projection of the elevation.
     */
    private String myProjection;

    /**
     * If this constructor is called, init() call must precede before use.
     */
    public STKElevationImageReader()
    {
    }

    /**
     * Constructs a new elevation image reader.
     *
     * @param bounds The bounds of this reader.
     * @param projection The projection of the elevation.
     * @param orderId The order id.
     */
    public STKElevationImageReader(GeographicBoundingBox bounds, String projection, String orderId)
    {
        myBounds = bounds;
        myProjection = projection;
        myOrderId = orderId;
    }

    @Override
    public GeographicBoundingBox getBoundingBox()
    {
        return myBounds;
    }

    @Override
    public String getCRS()
    {
        return myProjection;
    }

    @Override
    public String getElevationOrderId()
    {
        return myOrderId;
    }

    @Override
    public String getImageFormat()
    {
        return Constants.IMAGE_FORMAT;
    }

    @Override
    public double getMissingDataValue()
    {
        return -Short.MIN_VALUE;
    }

    @Override
    public void init(GeographicBoundingBox bounds, double missingDataValue, String crs, String orderId)
    {
        myBounds = bounds;
        myProjection = crs;
        myOrderId = orderId;
    }

    @Override
    public double readElevation(GeographicPosition position, Image image, GeographicBoundingBox bounds, boolean approximate)
        throws ElevationImageReaderException
    {
        double elevation = getMissingDataValue();

        if (image instanceof QuantizedMesh)
        {
            QuantizedMesh mesh = (QuantizedMesh)image;
            elevation = new GeographicQuantizedMeshReader(bounds).getElevationM(position, mesh);
        }

        return elevation;
    }
}
