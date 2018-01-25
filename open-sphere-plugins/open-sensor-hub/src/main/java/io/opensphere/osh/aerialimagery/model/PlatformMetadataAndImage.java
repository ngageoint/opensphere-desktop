package io.opensphere.osh.aerialimagery.model;

import java.nio.ByteBuffer;

/**
 * Contains an aerial image with the platforms metadata at the time the image
 * was taken.
 */
public class PlatformMetadataAndImage extends PlatformMetadata
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The image bytes.
     */
    private final transient ByteBuffer myImageBytes;

    /**
     * Constructs a new metadata and image model.
     *
     * @param metadata The metadata of the UAV at the time this image was taken.
     * @param imageBytes The image data.
     */
    public PlatformMetadataAndImage(PlatformMetadata metadata, ByteBuffer imageBytes)
    {
        setCameraPitchAngle(metadata.getCameraPitchAngle());
        setCameraRollAngle(metadata.getCameraRollAngle());
        setCameraYawAngle(metadata.getCameraYawAngle());
        setFootprint(metadata.getFootprint());
        setLocation(metadata.getLocation());
        setPitchAngle(metadata.getPitchAngle());
        setRollAngle(metadata.getRollAngle());
        setTime(metadata.getTime());
        setYawAngle(metadata.getYawAngle());
        myImageBytes = imageBytes;
    }

    /**
     * Gets the image bytes.
     *
     * @return The image data.
     */
    public ByteBuffer getImageBytes()
    {
        return myImageBytes;
    }
}
