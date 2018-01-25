package io.opensphere.server.state;

import io.opensphere.core.modulestate.ModuleStateController;

/**
 * Contains constant values specific to the WMS state classes.
 *
 */
public final class StateConstants
{
    /**
     * The element name for the WMS layer elements.
     */
    public static final String LAYER_NAME = "layer";

    /** The default name of the WMS layer type. */
    public static final String WMS_LAYER_TYPE = "wms";

    /** The default name of the WFS layer type. */
    public static final String WFS_LAYER_TYPE = "wfs";

    /** The name of the layers element. */
    public static final String LAYERS_NAME = "layers";

    /** Beginning of the type qualifier. */
    public static final String TYPE_BEGIN = "[@type=\"";

    /** End of the type qualifier. */
    public static final String TYPE_END = "\"]";

    /** Type for data layers. */
    public static final String DATA_LAYERS_TYPE = "data";

    /** The data layers path. */
    public static final String DATA_LAYERS_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + LAYERS_NAME + TYPE_BEGIN
            + DATA_LAYERS_TYPE + TYPE_END;

    /** Type for map layers. */
    public static final String MAP_LAYERS_TYPE = "map";

    /**
     * The XPath for the map layers layer group element.
     */
    public static final String MAP_LAYERS_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + LAYERS_NAME + TYPE_BEGIN
            + MAP_LAYERS_TYPE + TYPE_END;

    /**
     * The XPath defining a WMS layer element.
     */
    public static final String WMS_LAYER_PATH = "/:" + LAYER_NAME + TYPE_BEGIN + WMS_LAYER_TYPE + TYPE_END;

    /**
     * The XPath defining a WMS layer element within the data layer group.
     */
    public static final String WMS_DATA_LAYER_PATH = DATA_LAYERS_PATH + WMS_LAYER_PATH;

    /**
     * The XPath defining a WMS layer element within the map layer group.
     */
    public static final String WMS_MAP_LAYERS_PATH = MAP_LAYERS_PATH + WMS_LAYER_PATH;

    /** The Constant MODULE_NAME. */
    public static final String MODULE_NAME = "Layers";

    /**
     * The XPath prefix for a layer's location within the OpenSphere Saved State
     * XML structure.
     */
    public static final String XPATH_PREFIX = StateConstants.DATA_LAYERS_PATH + "/:" + StateConstants.LAYER_NAME
            + StateConstants.TYPE_BEGIN;

    /**
     * The XPath suffix for a layer's location within the OpenSphere Saved State
     * XML structure.
     */
    public static final String XPATH_SUFFIX = StateConstants.TYPE_END;

    /**
     * Not constructible.
     */
    private StateConstants()
    {
    }
}
