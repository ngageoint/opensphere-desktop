package io.opensphere.core;

import io.opensphere.core.api.Envoy;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.event.EventManager;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.metrics.MetricsRegistry;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.order.OrderManagerRegistry;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.search.SearchRegistry;
import io.opensphere.core.server.ServerProviderRegistry;
import io.opensphere.core.util.registry.GenericRegistry;

/**
 * A collection of tools to be used by plug-ins to interact with the rest of the
 * application.
 */
public interface Toolbox
{
    /**
     * Gets the statistics manager. The statistics manager allows for various parts
     * of the application to contribute statistics for logging or other uses.
     *
     * @return an instance of the statistics manager.
     */
    StatisticsManager getStatisticsManager();

    /**
     * Gets the animation manager. The animation manager allows the plug-in to
     * setup and control time based animations, and subscribe for notifications
     * of when animation settings change. The animation manager works with the
     * TimeManager to control the currently displayed time interval.
     *
     * @return The animation manager.
     */
    AnimationManager getAnimationManager();

    /**
     * Get the control registry. This allows the plug-in to register for
     * notification of user input.
     *
     * @return The control registry.
     */
    ControlRegistry getControlRegistry();

    /**
     * Gets the data filter registry. This allows plug-in to submit meta data
     * filters and listen for meta data filters that would filter data within or
     * being retrieved for the application.
     *
     * @return the data filter registry
     */
    DataFilterRegistry getDataFilterRegistry();

    /**
     * Get the data registry. This allows the plug-in to register and retrieve
     * data models.
     *
     * @return The data registry.
     */
    DataRegistry getDataRegistry();

    /**
     * Get the envoy registry. The plug-in should adds its envoys here.
     *
     * @return The envoy registry.
     */
    GenericRegistry<Envoy> getEnvoyRegistry();

    /**
     * Get the event manager. This allows the plug-in to publish and subscribe
     * to arbitrary events in the system.
     *
     * @return The event manager.
     */
    EventManager getEventManager();

    /**
     * Get the manager for image captures of the frame buffer.
     *
     * @return The frame buffer capture manager.
     */
    FrameBufferCaptureManager getFrameBufferCaptureManager();

    /**
     * Get the geometry registry. This allows the plug-in to register and
     * retrieve geometries. Geometries are graphical models used by the pipeline
     * to draw shapes on the display.
     *
     * @return The geometry registry.
     */
    GeometryRegistry getGeometryRegistry();

    /**
     * Gets the importer registry.
     *
     * @return the importer registry
     */
    ImporterRegistry getImporterRegistry();

    /**
     * Get the map manager. The map manager allows the plug-in to register for
     * notification of map changes, and allows the plug-in to get information
     * about the current viewer and projection.
     *
     * @return The map manager.
     */
    MapManager getMapManager();

    /**
     * Gets the metrics registry.
     *
     * @return the metrics registry
     */
    MetricsRegistry getMetricsRegistry();

    /**
     * Get the manager responsible for loading and saving modules' states.
     *
     * @return The module state manager.
     */
    ModuleStateManager getModuleStateManager();

    /**
     * Gets the registry for order managers. This registry enables generic
     * ordering of keys along with notification of changed orders and
     * persistence of the order values. This facility is useful for plugins that
     * allow the user to select the order of elements with the expectation that
     * that order be preserved between execution.
     *
     * @return the order manager registry.
     */
    OrderManagerRegistry getOrderManagerRegistry();

    /**
     * Gets the plug-in toolbox registry. This allows plug-ins to register
     * extensions to the core toolbox, for use by other plug-ins.
     *
     * @return The plug-in toolbox registry.
     */
    PluginToolboxRegistry getPluginToolboxRegistry();

    /**
     * Gets the preferences registry. This allows plug-in to persist, retrieve,
     * and subscribe to changes for simple preferences.
     *
     * @return the preferences registry.
     */
    PreferencesRegistry getPreferencesRegistry();

    /**
     * Get the search providers registry. This allows plugins to register search
     * methods.
     *
     * @return The search registry.
     */
    SearchRegistry getSearchRegistry();

    /**
     * Get the security manager. The security manager allows plug-ins access to
     * the master password manager.
     *
     * @return The security manager.
     */
    SecurityManager getSecurityManager();

    /**
     * Get the system toolbox that provides interfaces for general system
     * facilities.
     *
     * @return The system toolbox.
     */
    SystemToolbox getSystemToolbox();

    /**
     * Get the time manager. The time manager allows the plug-in to set and get
     * the currently active time spans and subscribe for notifications of when
     * other plug-ins change the active time spans.
     *
     * @return The time manager.
     */
    TimeManager getTimeManager();

    /**
     * Get the transformer registry. The plug-in should add its transformers
     * here.
     *
     * @return The transformer registry.
     */
    GenericRegistry<Transformer> getTransformerRegistry();

    /**
     * Get the UI registry. This allows the plug-in to add user interaction
     * components to the application.
     *
     * @return The UI registry.
     */
    UIRegistry getUIRegistry();

    /**
     * Get the units registry. This allows the plug-in to access the available
     * and preferred units in the system.
     *
     * @return The units registry.
     */
    UnitsRegistry getUnitsRegistry();

    /**
     * Gets the registry that contains all server providers.
     *
     * @return The server provider registry.
     */
    ServerProviderRegistry getServerProviderRegistry();
}
