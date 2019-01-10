package io.opensphere.core.util.fx.tabpane.skin;

/** An enumeration over the phases of edit available for a tab. */
public enum TabEditPhase
{
    /** The default, non-editing state. */
    NOT_EDITING,

    /** The editing state. */
    EDITING,

    /** The post-editing state where changes are being saved. */
    PERSISTING,

    /** The post-editing state where changes are being discarded. */
    CANCELLING;
}
