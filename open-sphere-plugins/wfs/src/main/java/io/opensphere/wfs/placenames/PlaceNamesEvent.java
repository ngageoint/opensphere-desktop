package io.opensphere.wfs.placenames;

import java.util.Collection;

import io.opensphere.core.event.AbstractMultiStateEvent;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.util.collections.New;

/**
 * An event describing a WFS request.
 */
public class PlaceNamesEvent extends AbstractMultiStateEvent
{
    /** Whether or not place names are currently active. */
    private boolean myActive = true;

    /** Place names to inject into the pipeline. */
    private final Collection<? extends LabelGeometry> myAdds;

    /** The layer name associated with place names. */
    private final String myLayerName;

    /** Place names to remove from the pipeline. */
    private final Collection<? extends LabelGeometry> myRemoves;

    /** The server name associated with place names. */
    private final String myServerName;

    /**
     * Constructor.
     *
     * @param adds Place names to inject into the pipeline.
     * @param removes Place names to remove from the pipeline.
     * @param serverName The server name.
     * @param layerName The layer name.
     * @param isActive Whether or not place names are currently active.
     */
    public PlaceNamesEvent(Collection<LabelGeometry> adds, Collection<LabelGeometry> removes, String serverName, String layerName,
            boolean isActive)
    {
        myAdds = New.unmodifiableCollection(adds);
        myRemoves = New.unmodifiableCollection(removes);
        myServerName = serverName;
        myLayerName = layerName;
        myActive = isActive;
    }

    /**
     * Get the adds.
     *
     * @return the adds
     */
    public Collection<? extends LabelGeometry> getAdds()
    {
        return myAdds;
    }

    @Override
    public String getDescription()
    {
        return "Event indicating that a WFS Placename request has been issued.";
    }

    /**
     * Get the layer name.
     *
     * @return The layer name.
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Get the removes.
     *
     * @return the removes
     */
    public Collection<? extends LabelGeometry> getRemoves()
    {
        return myRemoves;
    }

    /**
     * Get the server name.
     *
     * @return The server name.
     */
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Get whether or not provided place names are active.
     *
     * @return True if active, false otherwise.
     */
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Mutator for the active value.
     *
     * @param value Whether or not provided place names are active.
     */
    public void setActive(boolean value)
    {
        myActive = value;
    }
}
