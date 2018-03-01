package io.opensphere.controlpanels;

import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import io.opensphere.core.PluginToolbox;
import io.opensphere.core.util.ObservableValue;

/**
 * Defines a toolbox for use with control panels.
 */
public interface ControlPanelToolbox extends PluginToolbox
{
    /**
     * Gets the registry for {@link DetailPanelProvider} implementations. This
     * allows for plugins to register additional providers without injecting
     * dependencies into the core project.
     * @param <T>
     *
     * @return the registry in which detail panel providers are registered.
     */
     GenericThing getDetailPanelProviderRegistry();

    /**
     * Gets the default {@link DetailPanelProvider} implementation.
     * @param <T>
     *
     * @return the provider to use when the none of the entries in the registry
     *         can handle the target data.
     */
     GenericThingProvider getDefaultDetailPanelProvider();
        

    /**
     * Gets the observable {@link TimeSpan} representing the visible portion of the timeline.
     *
     * @return the timespan of the visible portion of the timeline.
     */
    ObservableValue<TimeSpan> getUISpan();

    /**
     * Sets the observable {@link TimeSpan} representing the visible portion of the timeline.
     *
     * @param uiSpan the timespan of the visible portion of the timeline.
     */
    void setUISpan(ObservableValue<TimeSpan> uiSpan);
}
