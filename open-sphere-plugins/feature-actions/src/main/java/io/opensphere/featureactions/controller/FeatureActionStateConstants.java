package io.opensphere.featureactions.controller;

import io.opensphere.core.modulestate.ModuleStateController;

/**
 * State constants to use with Feature Actions.
 */
public final class FeatureActionStateConstants
{
    /** The name of the base of the feature actions tree. */
    public static final String BASE_NAME = "featureActions";

    /** The name of base for a feature action. */
    public static final String FEATURE_ACTION_NAME = "featureAction";

    /** The path for the feature actions tree. */
    public static final String BASE_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + BASE_NAME;

    /** The path for a feature action. */
    public static final String FEATURE_ACTION_PATH = BASE_PATH + "/:" + FEATURE_ACTION_NAME;

    /** The path for the type of a feature action. */
    public static final String TYPE_PATH = FEATURE_ACTION_PATH + "[@type=\"type\"]";

    /** Not constructible. */
    private FeatureActionStateConstants()
    {
    }
}
