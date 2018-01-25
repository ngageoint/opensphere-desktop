package io.opensphere.core.model;

/**
 * An enumerated type set that represents all the possible relationships of two
 * one-dimensional intervals.
 */
public enum RangeRelationType
{
    /** One interval is before the other, no touching or overlap. */
    BEFORE,

    /** One interval is before the other, touching ends. */
    BORDERS_BEFORE,

    /**
     * One interval overlaps the front edge of the other (but not the back
     * edge).
     */
    OVERLAPS_FRONT_EDGE,

    /** One interval is a subset of the other (does not overlap either edge). */
    SUBSET,

    /** One interval is a superset of the other (overlaps both edges). */
    SUPERSET,

    /** One interval is equal to the other. */
    EQUAL,

    /**
     * One interval overlaps the back edge of the other (but not the front
     * edge).
     */
    OVERLAPS_BACK_EDGE,

    /** One interval is after the other, touching ends. */
    BORDERS_AFTER,

    /** One interval is after the other, no touching or overlap. */
    AFTER
}
