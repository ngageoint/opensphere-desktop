package io.opensphere.core.control.awt;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * Helper class for finding the target correct target for a mouse event.
 */
public final class AWTMouseTargetFinder
{
    /** Reference to {@code Container.getMouseEventTarget}. */
    private static final Method GET_MOUSE_EVENT_TARGET_METHOD;

    /** Copy of the value of {@code Container.INCLUDE_SELF}. */
    private static final Boolean INCLUDE_SELF;

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AWTMouseTargetFinder.class);

    /** The container which is block another container. */
    private static final Field MODAL_COMPONENT;

    static
    {
        Method mouseTargetMeth;
        Boolean includeSelf;
        Field modalComp;
        try
        {
            mouseTargetMeth = Container.class.getDeclaredMethod("getMouseEventTarget", Integer.TYPE, Integer.TYPE, Boolean.TYPE);
            mouseTargetMeth.setAccessible(true);

            Field field = Container.class.getDeclaredField("INCLUDE_SELF");
            field.setAccessible(true);
            includeSelf = Boolean.valueOf(field.getBoolean(null));

            modalComp = Container.class.getDeclaredField("modalComp");
            modalComp.setAccessible(true);
        }
        catch (SecurityException | NoSuchMethodException | IllegalArgumentException | NoSuchFieldException
                | IllegalAccessException e)
        {
            logReflectionException(e);
            mouseTargetMeth = null;
            includeSelf = null;
            modalComp = null;
        }

        GET_MOUSE_EVENT_TARGET_METHOD = mouseTargetMeth;
        INCLUDE_SELF = includeSelf;
        MODAL_COMPONENT = modalComp;
    }

    /**
     * Find the target component for the given container and mouse event. The
     * container's "getMouseEventTarget" method is called by reflection. This is
     * the same call that is made by Swing's LightweightEventDispatcher, so it
     * should give the target which would be used normally.
     *
     * @param container The container which may contain a target component for
     *            the event.
     * @param x The container adjusted x coordinate.
     * @param y The container adjusted y coordinate.
     * @return The component which is the target for the event or null if no
     *         target is found.
     */
    public static Component findMouseTarget(Container container, int x, int y)
    {
        try
        {
            return (Component)GET_MOUSE_EVENT_TARGET_METHOD.invoke(container, Integer.valueOf(x), Integer.valueOf(y),
                    INCLUDE_SELF);
        }
        catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
        {
            logReflectionException(e);
        }
        return null;
    }

    /**
     * Get the container which blocks the given container if any exists.
     *
     * @param container The container which might be blocked.
     * @return The blocking container if any exists.
     */
    public static Container getModalComponent(Container container)
    {
        try
        {
            return (Container)MODAL_COMPONENT.get(container);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            logReflectionException(e);
        }
        return null;
    }

    /**
     * Log a reflection exception.
     *
     * @param e The exception.
     */
    private static void logReflectionException(Exception e)
    {
        LOGGER.error("Reflection failure: " + e, e);
    }

    /** Disallow instantiation. */
    private AWTMouseTargetFinder()
    {
    }
}
