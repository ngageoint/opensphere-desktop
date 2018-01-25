package io.opensphere.osh.aerialimagery.results;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.osh.aerialimagery.model.LinkedLayer;
import io.opensphere.osh.aerialimagery.model.LinkedLayers;

/**
 * Class responsible for managing the linking of OSH layers.
 */
public class LayerLinker
{
    /**
     * The linkd layers preference key.
     */
    private static final String ourLinkedLayersKey = "linkedlayers";

    /**
     * The linked layers preferences.
     */
    private final Preferences myPrefs;

    /**
     * Constructs a new LayerLinker.
     *
     * @param prefsRegistry The preferences registry.
     */
    public LayerLinker(PreferencesRegistry prefsRegistry)
    {
        myPrefs = prefsRegistry.getPreferences(getClass());
    }

    /**
     * Gets the id of the layer linked to the specified layer, or null if there
     * isn't one.
     *
     * @param layerId The id of the layer to find a link for.
     * @return The id of the linked layer, or null if there isn't a link.
     */
    public String getLinkedLayerId(String layerId)
    {
        LinkedLayers linkedLayers = myPrefs.getJAXBObject(LinkedLayers.class, ourLinkedLayersKey, (LinkedLayers)null);
        String linkedLayerId = null;

        if (linkedLayers != null)
        {
            for (LinkedLayer linkedLayer : linkedLayers.getLinkedLayers())
            {
                if (layerId.equals(linkedLayer.getLinkedLayersTypeKey()))
                {
                    linkedLayerId = linkedLayer.getOtherLinkedLayersTypeKey();
                    break;
                }
                else if (layerId.equals(linkedLayer.getOtherLinkedLayersTypeKey()))
                {
                    linkedLayerId = linkedLayer.getLinkedLayersTypeKey();
                    break;
                }
            }
        }

        return linkedLayerId;
    }
}
