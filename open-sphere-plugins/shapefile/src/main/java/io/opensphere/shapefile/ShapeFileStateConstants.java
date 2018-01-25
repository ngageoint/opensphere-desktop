package io.opensphere.shapefile;

import io.opensphere.core.modulestate.ModuleStateController;

/**
 * Shapefile state constants.
 */
public final class ShapeFileStateConstants
{
    /** The Constant LAYER_NAME. */
    public static final String LAYER_NAME = "layer";

    /** The name of the layers element. */
    public static final String DATA_LAYERS_NAME = "layers";

    /** The Constant MODULE_NAME. */
    public static final String MODULE_NAME = "Shape Files";

    /** The Constant SHAPE_LAYER_NAME. */
    public static final String SHAPE_LAYER_NAME = "ShapeFileSource";

    /** The Constant SHAPE_LAYER_TYPE. */
    public static final String SHAPE_LAYER_TYPE = "shp";

    /** The Constant SHAPE_PATH. */
    public static final String SHAPE_PATH = "/:" + LAYER_NAME + "[@type=\"" + SHAPE_LAYER_TYPE + "\"]";

    /** The data layers path. */
    public static final String DATA_LAYERS_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + DATA_LAYERS_NAME
            + "[@type=\"data\"]";

    /** The Constant SHAPE_DATA_LAYER_PATH. */
    public static final String SHAPE_DATA_LAYER_PATH = DATA_LAYERS_PATH + SHAPE_PATH;

    /** Not constructible. **/
    private ShapeFileStateConstants()
    {
    }
}
