package io.opensphere.core.quantify;

import io.opensphere.core.Toolbox;

/** A set of utility methods to ease the use of the quantify toolbox. */
public final class QuantifyToolboxUtils
{
    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     * @param key the metric key for which to collect the metric.
     * @deprecated use {@link Quantify#collectMetric(String)} instead.
     */
    @Deprecated
    public static void collectMetric(Toolbox toolbox, String key)
    {
        getQuantifyToolbox(toolbox).getQuantifyService().collectMetric(key);
    }

    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     * @param key the metric key for which to collect the metric.
     * @param condition the condition which must be true for the metric to be
     *            sent.
     * @deprecated use
     *             {@link Quantify#collectConditionalMetric(String, boolean)}
     *             instead.
     */
    @Deprecated
    public static void collectConditionalMetric(Toolbox toolbox, String key, boolean condition)
    {
        if (condition)
        {
            getQuantifyToolbox(toolbox).getQuantifyService().collectMetric(key);
        }
    }

    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     * @param keyPrefix the metric key for which to collect the metric.
     * @param condition the condition which must be true for the metric to be
     *            sent.
     * @deprecated use
     *             {@link Quantify#collectEnableDisableMetric(String, boolean)}
     *             instead.
     */
    @Deprecated
    public static void collectEnableDisableMetric(Toolbox toolbox, String keyPrefix, boolean condition)
    {
        if (condition)
        {
            getQuantifyToolbox(toolbox).getQuantifyService().collectMetric(keyPrefix + ".enabled");
        }
        else
        {
            getQuantifyToolbox(toolbox).getQuantifyService().collectMetric(keyPrefix + ".disabled");
        }
    }

    /**
     * Gets the quantify toolbox.
     *
     * @param toolbox the {@link Toolbox} through which application state is
     *            accessed.
     * @return the quantify toolbox.
     */
    public static QuantifyToolbox getQuantifyToolbox(Toolbox toolbox)
    {
        return toolbox.getPluginToolboxRegistry().getPluginToolbox(QuantifyToolbox.class);
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private QuantifyToolboxUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }
}
