package io.opensphere.core.model;

/**
 * When drawing geographic lines, there are multiple ways to define the line
 * between two points.
 */
public enum LineType
{
    /** Draw lines along the great circle between the vertices. */
    GREAT_CIRCLE,

    /**
     * Draw lines as straight between the vertices where latitude and longitude
     * are equally weighted and there is no skew as the values progress.
     */
    STRAIGHT_LINE,

    /**
     * Draw a straight lines between the vertices and making no attempt to
     * follow any intermediate model terrain (including base model curvature).
     */
    STRAIGHT_LINE_IGNORE_TERRAIN,

    ;
}
