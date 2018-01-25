package io.opensphere.merge.layout;

/** Method of layout in the transverse direction. */
public enum LayMode
{
    /** Left or top. */
    MIN,
    /** Centered vertically or horizontally. */
    CENTER,
    /** Right or bottom. */
    MAX,
    /** Stretch to fill the available space. */
    STRETCH;
}