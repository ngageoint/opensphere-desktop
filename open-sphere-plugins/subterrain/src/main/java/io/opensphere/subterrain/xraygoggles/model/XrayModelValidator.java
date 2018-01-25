package io.opensphere.subterrain.xraygoggles.model;

import io.opensphere.core.model.ScreenPosition;

/**
 * Validates new values for the {@link XrayGogglesModel}.
 */
public interface XrayModelValidator
{
    /**
     * Validates a new screen position for the xray window.
     *
     * @param upperLeft The upper left screen position of the xray window.
     * @param upperRight The upper right screen position of the xray window.
     * @param lowerLeft The lower left screen position of the xray window.
     * @param lowerRight The lower right screen position of the xray window.
     * @return True if the values are valid, false otherwise.
     */
    boolean isValid(ScreenPosition upperLeft, ScreenPosition upperRight, ScreenPosition lowerLeft,
            ScreenPosition lowerRight);
}
