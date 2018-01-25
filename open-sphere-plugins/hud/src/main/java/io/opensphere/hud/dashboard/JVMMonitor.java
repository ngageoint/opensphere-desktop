package io.opensphere.hud.dashboard;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import io.opensphere.core.MemoryManager.Status;
import io.opensphere.core.Toolbox;
import io.opensphere.core.metrics.impl.AbstractMetricsProvider.EventStrategy;
import io.opensphere.core.metrics.impl.DefaultMetricsProvider;
import io.opensphere.core.metrics.impl.DefaultNumberMetricsProvider;
import io.opensphere.core.metrics.impl.DefaultPercentageMetricsProvider;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class JVMMetricProvider.
 */
public class JVMMonitor implements ActionListener
{
    /** The Constant MEMORY. */
    private static final String MEMORY = "Memory";

    /** The Constant JVM. */
    private static final String JVM = "JVM";

    /** The Update timer. */
    private final Timer myUpdateTimer;

    /** The Num processors provider. */
    private final DefaultNumberMetricsProvider myNumProcessorsProvider;

    /** The Max mem provider. */
    private final DefaultNumberMetricsProvider myMaxMemProvider;

    /** The In use mem provider. */
    private final DefaultNumberMetricsProvider myInUseMemProvider;

    /** The Percent used provider. */
    private final DefaultPercentageMetricsProvider myPercentUsedProvider;

    /** The Memory state provider. */
    private final DefaultMetricsProvider myMemoryStateProvider;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Last state color. */
    private Color myLastStateColor = Color.GREEN;

    /**
     * Instantiates a new jVM metric provider.
     *
     * @param tb the {@link Toolbox}
     */
    public JVMMonitor(Toolbox tb)
    {
        myToolbox = tb;
        myUpdateTimer = new Timer(1000, this);
        myUpdateTimer.setRepeats(true);

        myNumProcessorsProvider = new DefaultNumberMetricsProvider(0, JVM, "Machine", "#Processors");
        myMaxMemProvider = new DefaultNumberMetricsProvider(0, JVM, MEMORY, "Max");
        myMaxMemProvider.setPostfix("M");
        myInUseMemProvider = new DefaultNumberMetricsProvider(1, JVM, MEMORY, "Used");
        myInUseMemProvider.setEventStrategy(EventStrategy.EVENT_ON_ALL_UPDATES);
        myInUseMemProvider.setPostfix("M");
        myPercentUsedProvider = new DefaultPercentageMetricsProvider(2, JVM, MEMORY, "Usage");
        myPercentUsedProvider.setEventStrategy(EventStrategy.EVENT_ON_ALL_UPDATES);
        myMemoryStateProvider = new DefaultMetricsProvider(3, JVM, MEMORY, "State");
        myMemoryStateProvider.setEventStrategy(EventStrategy.EVENT_ON_CHANGES_ONLY);
        tb.getMetricsRegistry().addMetricsProvider(myNumProcessorsProvider);
        tb.getMetricsRegistry().addMetricsProvider(myMaxMemProvider);
        tb.getMetricsRegistry().addMetricsProvider(myInUseMemProvider);
        tb.getMetricsRegistry().addMetricsProvider(myPercentUsedProvider);
        tb.getMetricsRegistry().addMetricsProvider(myMemoryStateProvider);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long inUseMem = totalMem - freeMem;
        double percent = (double)inUseMem / (double)maxMem;

        myNumProcessorsProvider.setValue(Runtime.getRuntime().availableProcessors());
        myMaxMemProvider.setValue(maxMem);
        myInUseMemProvider.setValue(inUseMem);
        myPercentUsedProvider.setValue(percent);

        Color stateColor = Color.white;
        Status stat = myToolbox.getSystemToolbox().getMemoryManager().getMemoryStatus();
        if (stat != null)
        {
            switch (stat)
            {
                case CRITICAL:
                    stateColor = Color.red;
                    break;
                case WARNING:
                    stateColor = Color.YELLOW;
                    break;
                default:
                    stateColor = Color.GREEN;
                    break;
            }
            myMemoryStateProvider.setValue(stat.toString());
        }
        else
        {
            myMemoryStateProvider.setValue("UNKNOWN");
        }

        if (!EqualsHelper.equals(myLastStateColor, stateColor))
        {
            myLastStateColor = stateColor;
            myPercentUsedProvider.setColor(stateColor);
            myMemoryStateProvider.setColor(stateColor);
        }
    }

    /**
     * Start.
     */
    public void start()
    {
        myUpdateTimer.start();
    }

    /**
     * Stop.
     */
    public void stop()
    {
        myUpdateTimer.stop();
    }
}
