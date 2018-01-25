package io.opensphere.core.event;

/**
 * An event that indicates that the application has reached a stage in its
 * lifecycle.
 */
public class ApplicationLifecycleEvent extends AbstractSingleStateEvent
{
    /** The application stage of this event. */
    private final Stage myStage;

    /**
     * Publish a lifecycle event.
     *
     * @param manager An event manager.
     * @param stage The application stage.
     */
    public static void publishEvent(EventManager manager, Stage stage)
    {
        manager.publishEvent(new ApplicationLifecycleEvent(stage));
    }

    /**
     * Constructor.
     *
     * @param stage The applications stage that has been reached.
     */
    protected ApplicationLifecycleEvent(Stage stage)
    {
        myStage = stage;
    }

    @Override
    public String getDescription()
    {
        return "An event indicating that the application has reached a new stage in its lifecycle.";
    }

    /**
     * Get the application stage of this event.
     *
     * @return The stage.
     */
    public Stage getStage()
    {
        return myStage;
    }

    /**
     * Enumeration of the application stages.
     */
    public enum Stage
    {
        /** The look-and-feel has been installed. */
        LAF_INSTALLED,

        /** The main application frame has been made visible. */
        MAIN_FRAME_VISIBLE,

        /** The graphics pipeline has been initialized. */
        PIPELINE_INITIALIZED,

        /**
         * {@link io.opensphere.core.Plugin#initialize} has been called on all
         * plug-ins.
         */
        PLUGINS_INITIALIZED,

        /** The shutdown sequence has started. */
        BEGIN_SHUTDOWN,

        /** The envoys have been closed. */
        ENVOYS_CLOSED,

        /** The transformers have been closed. */
        TRANSFORMERS_CLOSED,

        /** The plugins have been closed. */
        PLUGINS_CLOSED,
    }
}
