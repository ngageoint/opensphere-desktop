package io.opensphere.wms.sld;

/**
 * The Class WMSUserDefinedSymbolization.
 */
public class WMSUserDefinedSymbolization
{
    /** Whether this supports user layers. */
    private boolean mySupportsUserLayer;

    /** Whether this supports user styles. */
    private boolean mySupportsUserStyle;

    /** Whether this supports remote WFS. */
    private boolean mySupportsRemoteWFS;

    /**
     * Sets the supports remote wfs.
     *
     * @param supportsRemoteWFS the new supports remote wfs
     */
    public void setSupportsRemoteWFS(boolean supportsRemoteWFS)
    {
        mySupportsRemoteWFS = supportsRemoteWFS;
    }

    /**
     * Sets the supports user layer.
     *
     * @param supportsUserLayer the new supports user layer
     */
    public void setSupportsUserLayer(boolean supportsUserLayer)
    {
        mySupportsUserLayer = supportsUserLayer;
    }

    /**
     * Sets the supports user style.
     *
     * @param supportsUserStyle the new supports user style
     */
    public void setSupportsUserStyle(boolean supportsUserStyle)
    {
        mySupportsUserStyle = supportsUserStyle;
    }

    /**
     * Supports remote wfs.
     *
     * @return true, if successful
     */
    public boolean supportsRemoteWFS()
    {
        return mySupportsRemoteWFS;
    }

    /**
     * Supports user layer.
     *
     * @return true, if successful
     */
    public boolean supportsUserLayer()
    {
        return mySupportsUserLayer;
    }

    /**
     * Supports user style.
     *
     * @return true, if successful
     */
    public boolean supportsUserStyle()
    {
        return mySupportsUserStyle;
    }
}
