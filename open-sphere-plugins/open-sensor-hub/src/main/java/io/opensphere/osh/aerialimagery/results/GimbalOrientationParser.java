package io.opensphere.osh.aerialimagery.results;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Parses the camera's orientation from open sensor hubs uav sensors.
 */
public class GimbalOrientationParser extends AbstractOrientationParser
{
    /**
     * Constructs a new gimbal orientation parser.
     *
     * @param uiRegistry Used to notify user of parse.
     */
    public GimbalOrientationParser(UIRegistry uiRegistry)
    {
        super(uiRegistry);
    }

    @Override
    protected void setValues(PlatformMetadata metadata, double yaw, double pitch, double roll)
    {
        metadata.setCameraPitchAngle(pitch);
        metadata.setCameraRollAngle(roll);
        metadata.setCameraYawAngle(yaw);
    }
}
