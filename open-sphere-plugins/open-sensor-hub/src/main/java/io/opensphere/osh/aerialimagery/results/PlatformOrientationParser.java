package io.opensphere.osh.aerialimagery.results;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.osh.aerialimagery.model.PlatformMetadata;

/**
 * Parses the vehicle's orientation from open sensor hubs uav sensors.
 */
public class PlatformOrientationParser extends AbstractOrientationParser
{
    /**
     * Constructs a new gimbal orientation parser.
     *
     * @param uiRegistry Used to notify user of parse.
     */

    public PlatformOrientationParser(UIRegistry uiRegistry)
    {
        super(uiRegistry);
    }

    @Override
    protected void setValues(PlatformMetadata metadata, double yaw, double pitch, double roll)
    {
        metadata.setPitchAngle(pitch);
        metadata.setRollAngle(roll);
        metadata.setYawAngle(yaw);
    }
}
