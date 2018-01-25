package io.opensphere.csvcommon;

import io.opensphere.core.modulestate.ModuleStateController;

/**
 * CSV state constants.
 */
public final class CSVStateConstants
{
    /** The Constant LAYER_NAME. */
    public static final String LAYER_NAME = "layer";

    /** The name of the layers element. */
    public static final String DATA_LAYERS_NAME = "layers";

    /** The Constant MODULE_NAME. */
    public static final String MODULE_NAME = "CSV Files";

    /** The Constant CSV_LAYER_TYPE. */
    public static final String CSV_LAYER_TYPE = "csv";

    /** The Constant CSV_LAYER_PATH. */
    public static final String CSV_PATH = "/:" + LAYER_NAME + "[@type=\"" + CSV_LAYER_TYPE + "\"]";

    /** The data layers path. */
    public static final String DATA_LAYERS_PATH = "/" + ModuleStateController.STATE_QNAME + "/:" + DATA_LAYERS_NAME
            + "[@type=\"data\"]";

    /** The Constant CSV_DATA_LAYER_PATH. */
    public static final String CSV_DATA_LAYER_PATH = DATA_LAYERS_PATH + CSV_PATH;

    /** Not constructible. **/
    private CSVStateConstants()
    {
    }
}
