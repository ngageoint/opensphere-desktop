package io.opensphere.kml;

import io.opensphere.core.modulestate.ModuleStateController;

/**
 * The Class KMLStateConstants.
 */
public final class KMLStateConstants
{
    /** The Constant LAYER_NAME. */
    public static final String LAYER_NAME = "layer";

    /** The name of the layers element. */
    public static final String DATA_LAYERS_NAME = "layers";

    /** The Constant MODULE_NAME. */
    public static final String MODULE_NAME = "KML Files";

    /** The Constant KML_LAYER_NAME. */
    public static final String KML_LAYER_NAME = "KMLDataSource";

    /** The Constant KML_LAYER_TYPE. */
    public static final String KML_LAYER_TYPE = "kml";

    /** The Constant KML_LAYER_PATH. */
    public static final String KML_PATH = "/:" + LAYER_NAME + "[@type=\"" + KML_LAYER_TYPE + "\"]";

    /** The data layers path. */
    public static final String DATA_LAYERS_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + DATA_LAYERS_NAME
            + "[@type=\"data\"]";

    /** The Constant KML_DATA_LAYER_PATH. */
    public static final String KML_DATA_LAYER_PATH = DATA_LAYERS_PATH + KML_PATH;

    /** Not constructible. **/
    private KMLStateConstants()
    {
    }
}
