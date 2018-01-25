package io.opensphere.hud.dashboard.widget;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.Comparator;

import io.opensphere.core.metrics.MetricsProvider;
import io.opensphere.core.metrics.MetricsProvider.MetricsProviderListener;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * The Class DashboardWidget.
 */
public abstract class DashboardWidget extends AbstractHUDPanel implements MetricsProviderListener
{
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /** The Provider. */
    private final MetricsProvider myProvider;

    /**
     * Instantiates a new dashboard widget.
     *
     * @param provider the provider
     */
    public DashboardWidget(MetricsProvider provider)
    {
        super(new BorderLayout());
        setOpaque(false);
        myProvider = provider;
        myProvider.addListener(this);
    }

    /**
     * Gets the provider.
     *
     * @return the provider
     */
    public MetricsProvider getProvider()
    {
        return myProvider;
    }

    @Override
    public final void providerUpdated(final MetricsProvider provider)
    {
        EventQueueUtilities.runOnEDT(() -> updateFromProvider(provider));
    }

    /**
     * Update from provider.
     *
     * Called when the provider is updated.
     *
     * @param provider the provider
     */
    public abstract void updateFromProvider(MetricsProvider provider);

    /**
     * The Class CompareByPriorityThenLabel.
     */
    public static class CompareByPriorityThenLabel implements Comparator<DashboardWidget>, Serializable
    {
        /**
         * serialVersionUID.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DashboardWidget o1, DashboardWidget o2)
        {
            int dp1 = o1.getProvider().getDisplayPriority();
            int dp2 = o2.getProvider().getDisplayPriority();
            if (dp1 < dp2)
            {
                return -1;
            }
            else if (dp2 < dp1)
            {
                return 1;
            }
            else
            {
                String lb1 = o1.getProvider().getLabel();
                String lb2 = o2.getProvider().getLabel();
                if (lb1 == null && lb2 != null)
                {
                    return -1;
                }
                else if (lb2 == null && lb1 != null)
                {
                    return 1;
                }
                else if (lb1 == null && lb2 == null)
                {
                    return 0;
                }
                else
                {
                    return lb1 == null ? 0 : lb1.compareTo(lb2);
                }
            }
        }
    }
}
