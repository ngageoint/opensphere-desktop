package io.opensphere.wms.sld.event;

/**
 * Styled layer descriptor change listener.
 *
 * @see SldChangeEvent
 */
public interface SldChangeListener
{
    /**
     * Invoked when sld is created.
     *
     * @param evt the evt
     */
    void sldCreated(SldChangeEvent evt);

    /**
     * Invoked when sld is deleted.
     *
     * @param evt the evt
     */
    void sldDeleted(SldChangeEvent evt);
}
