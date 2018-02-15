package io.opensphere.wfs.util;

/**
 * The Class WFSConstants.
 */
public final class WFSConstants
{
    /** The Constant LAYERNAME_SEPARATOR. */
    public static final String LAYERNAME_SEPARATOR = "!!";

    /** The Constant TIME_FIELD. */
    public static final String DEFAULT_TIME_FIELD = "TIME";

    /** The time query key to use when the time column is defaulted. */
    public static final String DEFAULT_TIME_QUERY_KEY = "validTime";

    /** The Constant MAX_FEATURES_PREFERENCE. */
    public static final String MAX_FEATURES_PREFERENCE = "OGCServer.WFS.MaxRequestFeatures";

    /** The Constant DEFAULT_MAX_FEATURES. */
    public static final int DEFAULT_MAX_FEATURES = 100_000;

    /** The maximum number of maximum features. */
    public static final int MAX_MAX_FEATURES = 2_000_000;

    /** Forbid instantiation of utility class. */
    private WFSConstants()
    {
    }
}
