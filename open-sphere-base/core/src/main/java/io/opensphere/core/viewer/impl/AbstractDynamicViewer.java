package io.opensphere.core.viewer.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.viewer.Viewer;

/**
 * Common base class for viewers that support movement.
 */
public abstract class AbstractDynamicViewer extends AbstractViewer implements DynamicViewer
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractDynamicViewer.class);

    /** The preferences for this viewer. */
    private final Preferences myPreferences;

    /**
     * Factory method to create a viewer.
     *
     * @param type The type of the viewer.
     * @param builder The builder for the viewer.
     * @return The newly created viewer.
     */
    public static AbstractDynamicViewer create(Class<? extends AbstractDynamicViewer> type, Builder builder)
    {
        try
        {
            return type.getConstructor(Builder.class).newInstance(builder);
        }
        catch (SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException
                | IllegalAccessException | InvocationTargetException e)
        {
            LOGGER.error(e, e);
        }

        return null;
    }

    /**
     * Construct a viewer.
     *
     * @param builder An object that describes how the viewer should be created.
     * @param displayedViewer True if this user is used to display to the monitor, false if it is used
     * to render somewhere else such as frame buffers/textures.
     */
    public AbstractDynamicViewer(Builder builder, boolean displayedViewer)
    {
        super(displayedViewer);
        myPreferences = builder.getPreferences();
    }

    @Override
    public List<JMenuItem> getPolygonContexMenuItems(final Collection<? extends Vector3d> polygonPoints)
    {
        List<JMenuItem> menuItems = New.list();

        JMenu viewerMenu = new JMenu("Viewer Controls");
        menuItems.add(viewerMenu);

        JMenuItem zoom = new JMenuItem("Zoom");
        zoom.addActionListener(e ->
        {
            ViewerAnimator animator = new ViewerAnimator(AbstractDynamicViewer.this, polygonPoints, true);
            animator.start();
        });
        viewerMenu.add(zoom);

        JMenuItem center = new JMenuItem("Center");
        center.addActionListener(e ->
        {
            ViewerAnimator animator = new ViewerAnimator(AbstractDynamicViewer.this, polygonPoints, false);
            animator.start();
        });
        viewerMenu.add(center);

        return menuItems;
    }

    /**
     * Reset the viewer.
     *
     * @param builder A builder containing the viewer parameters.
     */
    public void reset(Builder builder)
    {
    }

    @Override
    public void setCenteredView(Viewer viewer)
    {
    }

    @Override
    public void startAnimationToPreferredPosition()
    {
    }

    @Override
    public void validateViewerPosition()
    {
    }

    /**
     * Get the preferences for this viewer.
     *
     * @return The preferences.
     */
    protected final Preferences getPreferences()
    {
        return myPreferences;
    }

    /**
     * This class describes how the viewer should be created.
     */
    public static class Builder
    {
        /** Default maximum zoom. */
        private static final double DEFAULT_MAX_ZOOM = 10.;

        /** Default minimum zoom. */
        private static final double DEFAULT_MIN_ZOOM = -10000.;

        /** The maximum zoom level. */
        private double myMaxZoom = DEFAULT_MAX_ZOOM;

        /** The minimum zoom level. */
        private double myMinZoom = DEFAULT_MIN_ZOOM;

        /** The model height. */
        private double myModelHeight = 2.;

        /** The model width. */
        private double myModelWidth = 2.;

        /** The preferences. */
        private Preferences myPreferences;

        /**
         * Get the maximum zoom.
         *
         * @return The maximum zoom level.
         */
        public double getMaxZoom()
        {
            return myMaxZoom;
        }

        /**
         * Get the minimum zoom.
         *
         * @return The minimum zoom level.
         */
        public double getMinZoom()
        {
            return myMinZoom;
        }

        /**
         * Get the height of the model.
         *
         * @return The height of the model in model coordinates.
         */
        public double getModelHeight()
        {
            return myModelHeight;
        }

        /**
         * Get the width of the model.
         *
         * @return The width of the model in model coordinates.
         */
        public double getModelWidth()
        {
            return myModelWidth;
        }

        /**
         * Get the view preferences.
         *
         * @return The view preferences.
         */
        public Preferences getPreferences()
        {
            return myPreferences;
        }

        /**
         * Set the maximum zoom level.
         *
         * @param maxZoom The maximum zoom.
         * @return This builder for convenience.
         */
        public Builder maxZoom(double maxZoom)
        {
            myMaxZoom = maxZoom;
            return this;
        }

        /**
         * Set the minimum zoom level.
         *
         * @param minZoom The minimum zoom.
         * @return This builder for convenience.
         */
        public Builder minZoom(double minZoom)
        {
            myMinZoom = minZoom;
            return this;
        }

        /**
         * Set the model height in model coordinates, used to limit panning.
         *
         * @param modelHeight The model height.
         * @return This builder for convenience.
         */
        public Builder modelHeight(double modelHeight)
        {
            myModelHeight = modelHeight;
            return this;
        }

        /**
         * Set the model width in model coordinates, used to limit panning.
         *
         * @param modelWidth The model width.
         * @return This builder for convenience.
         */
        public Builder modelWidth(double modelWidth)
        {
            myModelWidth = modelWidth;
            return this;
        }

        /**
         * Set the preferences object for the viewer.
         *
         * @param preferences The preferences.
         * @return The builder for convenience.
         */
        public Builder preferences(Preferences preferences)
        {
            myPreferences = preferences;
            return this;
        }
    }
}
