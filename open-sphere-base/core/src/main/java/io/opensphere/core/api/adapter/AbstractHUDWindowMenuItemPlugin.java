package io.opensphere.core.api.adapter;

import java.awt.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;

/**
 * Simple plug-in adapter that registers a toggle menu item that displays a HUD
 * window when selected.
 */
public abstract class AbstractHUDWindowMenuItemPlugin extends AbstractLocationSaveMenuItemPlugin
{
    /** TODO: Executor should come from Core. */
    private final ScheduledExecutorService myExecutor = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(3,
            new NamedThreadFactory(getClass().getSimpleName()), SuppressableRejectedExecutionHandler.getInstance()));

    /** A helper for the transformer. */
    private TransformerHelper myHelper;

    /** The transformer that publishes the geometries. */
    private Transformer myTransformer;

    /**
     * The window. This is <code>null</code> when the window is not displayed.
     */
    private Window<?, ?> myWindow;

    /**
     * Constructor with remember visibility state flag.
     *
     * @param title The title of the window.
     * @param rememberVisibilityState Indicates if the visibility state should
     *            be persisted in the preferences.
     * @param rememberLocation Indicates if the location should be persisted in
     *            the preferences.
     */
    public AbstractHUDWindowMenuItemPlugin(String title, boolean rememberVisibilityState, boolean rememberLocation)
    {
        super(title, rememberVisibilityState, rememberLocation);
    }

    @Override
    public void close()
    {
        myExecutor.shutdown();
    }

    @Override
    public Collection<? extends Transformer> getTransformers()
    {
        return Collections.singleton(myTransformer);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);

        myTransformer = new DefaultTransformer(toolbox.getDataRegistry());

        myHelper = new TransformerHelper(myTransformer, toolbox);
    }

    @Override
    protected void buttonDeselected()
    {
        if (myWindow != null)
        {
            myWindow.closeWindow();
            myWindow = null;
        }
    }

    @Override
    protected void buttonSelected()
    {
        if (myWindow == null)
        {
            myWindow = createWindow(myHelper, myExecutor);
            myWindow.init();
            myWindow.display();
            setPreferredLocation();
        }
    }

    /**
     * Create the window.
     *
     * @param helper The transformer helper.
     * @param executor The executor.
     *
     * @return The window.
     */
    protected abstract Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor);

    /**
     * Get the executor.
     *
     * @return The executor.
     */
    protected ScheduledExecutorService getExecutor()
    {
        return myExecutor;
    }

    /**
     * Get the transformer helper.
     *
     * @return The transformer helper.
     */
    protected TransformerHelper getHelper()
    {
        return myHelper;
    }

    @Override
    protected Point getLocation()
    {
        return myWindow.getFrameLocation().getUpperLeft().asPoint();
    }

    @Override
    protected void setLocation(int xLoc, int yLoc)
    {
        if (myWindow != null)
        {
            ScreenPosition upperLeft = myWindow.getFrameLocation().getUpperLeft();
            ScreenPosition delta = new ScreenPosition(xLoc - upperLeft.getX(), yLoc - upperLeft.getY());
            myWindow.moveWindow(delta);
        }
    }
}
