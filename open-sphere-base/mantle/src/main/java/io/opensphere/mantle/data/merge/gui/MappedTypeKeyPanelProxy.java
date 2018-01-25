package io.opensphere.mantle.data.merge.gui;

/**
 * The Class ProxyPanel.
 */
public class MappedTypeKeyPanelProxy
{
    /** The Type key panel. */
    private final MappedTypeKeyPanel myTypeKeyPanel;

    /**
     * Instantiates a new panel proxy.
     *
     * @param tkp the tkp
     */
    public MappedTypeKeyPanelProxy(MappedTypeKeyPanel tkp)
    {
        myTypeKeyPanel = tkp;
    }

    /**
     * Gets the panel.
     *
     * @return the panel
     */
    public MappedTypeKeyPanel getPanel()
    {
        return myTypeKeyPanel;
    }

    @Override
    public String toString()
    {
        return myTypeKeyPanel.getKeyName();
    }
}
