package io.opensphere.stkterrain.transformer;

import java.nio.ByteBuffer;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageReader;
import io.opensphere.stkterrain.model.mesh.QuantizedMesh;
import io.opensphere.stkterrain.util.Constants;

/**
 * Responsible for reading quantized mesh bytes. This is used within the
 * GeoPackage plugin to introduce custom tile image formats.
 */
public class STKImageReader implements ImageReader
{
    @Override
    public String getImageFormat()
    {
        return Constants.IMAGE_FORMAT;
    }

    @Override
    public Image readImage(ByteBuffer imageBytes)
    {
        QuantizedMesh mesh = new QuantizedMesh(imageBytes);
        return mesh;
    }
}
