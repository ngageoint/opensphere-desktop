package io.opensphere.core.quantify;

import io.opensphere.core.Toolbox;

/**
 * A utility class meant to simplify the transmission of metrics. The toolbox is
 * initialized from the kernel, which allows for clients to avoid passing it.
 */
public class Quantify
{
    /** A singleton initialized toolbox used to access application state. */
    private static volatile Toolbox ourToolbox;

    /**
     * Private constructor to prevent instantation.
     */
    private Quantify()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not supported.");
    }

    /**
     * Sets the toolbox.
     *
     * @param toolbox the toolbox
     */
    public static void setToolbox(Toolbox toolbox)
    {
        ourToolbox = toolbox;
    }

    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     * 
     * @param key the metric key for which to collect the metric.
     */
    public static void collectMetric(String key)
    {
        if (ourToolbox != null)
        {
            QuantifyToolboxUtils.getQuantifyToolbox(ourToolbox).getQuantifyService().collectMetric(key);
        }
    }

    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     *
     * @param key the metric key for which to collect the metric.
     * @param condition the condition which must be true for the metric to be
     *            sent.
     */
    public static void collectConditionalMetric(String key, boolean condition)
    {
        if (ourToolbox != null && condition)
        {
            QuantifyToolboxUtils.getQuantifyToolbox(ourToolbox).getQuantifyService().collectMetric(key);
        }
    }

    /**
     * Collects the named metric in the quantify service defined in the toolbox.
     *
     * @param keyPrefix the metric key for which to collect the metric.
     * @param condition the condition which must be true for the metric to be
     *            sent.
     */
    public static void collectEnableDisableMetric(String keyPrefix, boolean condition)
    {
        if (ourToolbox != null)
        {
            if (condition)
            {
                QuantifyToolboxUtils.getQuantifyToolbox(ourToolbox).getQuantifyService().collectMetric(keyPrefix + ".enabled");
            }
            else
            {
                QuantifyToolboxUtils.getQuantifyToolbox(ourToolbox).getQuantifyService().collectMetric(keyPrefix + ".disabled");
            }
        }
    }

}
