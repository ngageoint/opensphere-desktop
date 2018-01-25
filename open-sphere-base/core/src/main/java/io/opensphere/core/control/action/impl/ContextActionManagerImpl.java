package io.opensphere.core.control.action.impl;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Map;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DefaultPickListener;
import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener.PickEvent;
import io.opensphere.core.control.action.ActionContext;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextActionProvider;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.ContextSingleActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.collections.New;

/**
 * Manages events related to context menus and subscriptions for menu button
 * providers.
 */
public class ContextActionManagerImpl implements ContextActionManager
{
    /**
     * A map of the currently available context types for which menus may be
     * generated.
     */
    private final Map<Class<?>, Map<String, ActionContextImpl<?>>> myContexts = New.map();

    /** The control registry. */
    private final ControlRegistry myControlRegistry;

    /** Listener for events from the control context. */
    private final DiscreteEventListener myMouseListener = new DiscreteEventAdapter("ContextMenu", "Context Menu Mouse",
            "Context Menu Mouse")
    {
        @Override
        public void eventOccurred(InputEvent event)
        {
            if (event instanceof MouseEvent)
            {
                MouseEvent mouseEvent = (MouseEvent)event;
                if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)
                {
                    if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mouseEvent.getClickCount() == 2)
                    {
                        PickEvent latestEvent = myPickListener.getLatestEvent();
                        if (latestEvent != null && latestEvent.getPickedGeometry() != null)
                        {
                            Geometry pickedGeom = latestEvent.getPickedGeometry();
                            ActionContext<GeometryContextKey> context = getActionContext(
                                    ContextIdentifiers.GEOMETRY_DOUBLE_CLICK_CONTEXT, GeometryContextKey.class);
                            context.doAction(new GeometryContextKey(pickedGeom), (Component)mouseEvent.getSource(),
                                    mouseEvent.getX(), mouseEvent.getY(), null);
                        }
                    }
                    else if (mouseEvent.getButton() == MouseEvent.BUTTON3)
                    {
                        PickEvent latestEvent = myPickListener.getLatestEvent();
                        boolean showDefaultContextMenu = false;
                        if (latestEvent != null && latestEvent.getPickedGeometry() != null)
                        {
                            Geometry pickedGeom = latestEvent.getPickedGeometry();
                            ActionContext<GeometryContextKey> context = getActionContext(
                                    ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class);
                            // If no menu is shown for the picked geometry, show
                            // the default menu instead.
                            showDefaultContextMenu = !context.doAction(new GeometryContextKey(pickedGeom),
                                    (Component)mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY(), null);
                        }
                        else
                        {
                            showDefaultContextMenu = true;
                        }

                        if (showDefaultContextMenu)
                        {
                            ActionContext<ScreenPositionContextKey> context = getActionContext(
                                    ContextIdentifiers.SCREEN_POSITION_CONTEXT, ScreenPositionContextKey.class);
                            context.doAction(
                                    new ScreenPositionContextKey(new ScreenPosition(mouseEvent.getX(), mouseEvent.getY())),
                                    (Component)mouseEvent.getSource(), mouseEvent.getX(), mouseEvent.getY(), null);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isReassignable()
        {
            return false;
        }
    };

    /** Listener for pick changes. */
    private final DefaultPickListener myPickListener = new DefaultPickListener();

    /**
     * Constructor.
     *
     * @param controlRegistry The control registry.
     */
    public ContextActionManagerImpl(ControlRegistry controlRegistry)
    {
        myControlRegistry = controlRegistry;
        register();
    }

    /** De-register listeners. */
    public void cleanupListeners()
    {
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.removePickListener(myPickListener);

        ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        globeCtx.removeListener(myMouseListener);
    }

    @Override
    public <R> void clearContextSingleActionProvider(String contextId, Class<R> keyType)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.clearSingleActionProvider();
        }
    }

    @Override
    public <R> void deregisterContextMenuItemProvider(String contextId, Class<R> keyType, ContextMenuProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.removeMenuOptionProvider(provider);
            checkForCleanup(contextId, keyType, context);
        }
    }

    @Override
    public <R> void deregisterContextSingleActionProvider(String contextId, Class<R> keyType,
            ContextSingleActionProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.removeSingleActionProvider(provider);
            checkForCleanup(contextId, keyType, context);
        }
    }

    @Override
    public <R> void deregisterDefaultContextActionProvider(String contextId, Class<R> keyType, ContextActionProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.removeDefaultActionProvider(provider);
            checkForCleanup(contextId, keyType, context);
        }
    }

    @Override
    public <R> ActionContext<R> getActionContext(String contextIdentifier, Class<R> keyType)
    {
        synchronized (myContexts)
        {
            Map<String, ActionContextImpl<?>> typeMap = myContexts.get(keyType);
            if (typeMap == null)
            {
                typeMap = New.map();
                myContexts.put(keyType, typeMap);
            }

            @SuppressWarnings("unchecked")
            ActionContextImpl<R> context = (ActionContextImpl<R>)typeMap.get(contextIdentifier);
            if (context == null)
            {
                context = new ActionContextImpl<>(contextIdentifier);
                typeMap.put(contextIdentifier, context);
            }
            return context;
        }
    }

    /** Initialize my listeners. */
    public final void register()
    {
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.addPickListener(myPickListener);

        ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);

        // create bindings for the events we are interested in.
        DefaultMouseBinding clicked1 = new DefaultMouseBinding(MouseEvent.MOUSE_CLICKED);

        globeCtx.addListener(myMouseListener, clicked1);
    }

    @Override
    public <R> void registerContextMenuItemProvider(String contextId, Class<R> keyType, ContextMenuProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.addMenuOptionProvider(provider);
        }
    }

    @Override
    public <R> void registerContextSingleActionProvider(String contextId, Class<R> keyType,
            ContextSingleActionProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.registerSingleActionProvider(provider);
        }
    }

    @Override
    public <R> void registerDefaultContextActionProvider(String contextId, Class<R> keyType, ContextActionProvider<R> provider)
    {
        synchronized (myContexts)
        {
            ActionContextImpl<R> context = (ActionContextImpl<R>)getActionContext(contextId, keyType);
            context.registerDefaultActionProvider(provider);
        }
    }

    /**
     * If the given context is no longer in use, remove it.
     *
     * @param contextId The identifier for the context.
     * @param keyType The type of the key associated with the context.
     * @param context The context associated with the id and type.
     * @param <R> a context menu key type.
     */
    private <R> void checkForCleanup(String contextId, Class<R> keyType, ActionContextImpl<R> context)
    {
        if (!context.isUsed())
        {
            Map<String, ActionContextImpl<?>> typeMap = myContexts.get(keyType);
            typeMap.remove(contextId);
            if (typeMap.isEmpty())
            {
                myContexts.remove(keyType);
            }
        }
    }
}
