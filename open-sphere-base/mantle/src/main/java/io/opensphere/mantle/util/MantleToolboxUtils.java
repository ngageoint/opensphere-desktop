package io.opensphere.mantle.util;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.util.DataElementActionUtils;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.data.util.DataElementUpdateUtils;

/**
 * The Class MantleToolboxUtils.
 */
public final class MantleToolboxUtils
{
    /**
     * Gets the {@link DataElementActionUtils} with a convenience method from
     * the {@link MantleToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link DataElementActionUtils} from the {@link MantleToolbox}
     */
    public static DataElementActionUtils getDataElementActionUtils(Toolbox tb)
    {
        return getMantleToolbox(tb).getDataElementActionUtils();
    }

    /**
     * Gets the {@link DataElementLookupUtils} with a convenience method from
     * the {@link MantleToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link DataElementLookupUtils} from the {@link MantleToolbox}
     */
    public static DataElementLookupUtils getDataElementLookupUtils(Toolbox tb)
    {
        return getMantleToolbox(tb).getDataElementLookupUtils();
    }

    /**
     * Gets the {@link DataElementUpdateUtils} with a convenience method from
     * the {@link MantleToolbox}.
     *
     * @param tb the {@link Toolbox}
     * @return the {@link DataElementUpdateUtils} from the {@link MantleToolbox}
     */
    public static DataElementUpdateUtils getDataElementUpdateUtils(Toolbox tb)
    {
        return getMantleToolbox(tb).getDataElementUpdateUtils();
    }

    /**
     * Gets the mantle toolbox.
     *
     * @param tb the {@link Toolbox}
     * @return the mantle toolbox
     */
    public static MantleToolbox getMantleToolbox(Toolbox tb)
    {
        return tb.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
    }

    /**
     * Instantiates a new Mantle toolbox utils.
     */
    private MantleToolboxUtils()
    {
        // Nothing here.
    }
}
