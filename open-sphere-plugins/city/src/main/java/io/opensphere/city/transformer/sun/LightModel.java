package io.opensphere.city.transformer.sun;

import java.util.Observable;

import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;

/**
 * Contains the current light model.
 */
public class LightModel extends Observable
{
    /**
     * The light property.
     */
    public static final String LIGHT_PROP = "light";

    /**
     * The light model.
     */
    private LightingModelConfigGL myLight;

    /**
     * Gets the light.
     *
     * @return the light
     */
    public LightingModelConfigGL getLight()
    {
        return myLight;
    }

    /**
     * Sets the light.
     *
     * @param light the light to set
     */
    public void setLight(LightingModelConfigGL light)
    {
        myLight = light;
        setChanged();
        notifyObservers(LIGHT_PROP);
    }
}
