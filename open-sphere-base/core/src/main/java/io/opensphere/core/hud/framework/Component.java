package io.opensphere.core.hud.framework;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Base class for all custom HUD components. */
@SuppressWarnings("PMD.GodClass")
public abstract class Component implements ControlEventListener
{
    /** The lowest z-order allowed for this component. */
    private final int myBaseZOrder;

    /**
     * The location within the owning frame. The frame location will typically
     * be set by the layout manager.
     */
    private ScreenBoundingBox myFrameLocation;

    /** Horizontal alignment going from 0 (left) to 1 (right). */
    private float myHorizontalAlignment = 0.5f;

    /** Parent panel or null if this is at the top. */
    private Component myParent;

    /** Vertical alignment going from 0 (bottom) to 1 (top). */
    private float myVerticalAlignment = 0.5f;

    /**
     * Construct a HUDComponent.
     *
     * @param parent Component which owns me.
     */
    public Component(Component parent)
    {
        myParent = parent;
        // TODO enzio : z-order should be set by the creator of this component.
        myBaseZOrder = 0;
    }

    /**
     * Construct me.
     *
     * @param parent parent component.
     * @param location frame location.
     */
    public Component(Component parent, ScreenBoundingBox location)
    {
        this(parent);
        myFrameLocation = location;
    }

    /** Clear all geometries in me and my children. */
    public abstract void clearGeometries();

    /**
     * If I am the top level frame, remove all of the geometries. Otherwise,
     * call my parent to close.
     */
    public void closeWindow()
    {
        if (myParent != null)
        {
            myParent.closeWindow();
        }
    }

    /**
     * Get the location of this component within the Main window.
     *
     * @return the location
     */
    public ScreenBoundingBox getAbsoluteLocation()
    {
        ScreenBoundingBox relLoc = getFrameLocation();
        ScreenPosition offset = relLoc.getUpperLeft();
        Component frameComponent = this;
        Component parentFrame = null;
        while (null != (parentFrame = frameComponent.getContainingFrame()))
        {
            ScreenBoundingBox loc = parentFrame.getFrameLocation();
            offset = offset.add(loc.getUpperLeft());
            frameComponent = parentFrame;
        }

        ScreenPosition lowerRight = new ScreenPosition((int)relLoc.getWidth() + offset.getX(),
                (int)relLoc.getHeight() + offset.getY());
        return new ScreenBoundingBox(offset, lowerRight);
    }

    /**
     * Get the baseZOrder.
     *
     * @return the baseZOrder
     */
    public int getBaseZOrder()
    {
        return myBaseZOrder;
    }

    /**
     * Find the lowest level frame which contains this component.
     *
     * @return containing frame
     */
    public Frame<?, ?> getContainingFrame()
    {
        if (myParent != null)
        {
            if (myParent instanceof Frame)
            {
                return (Frame<?, ?>)myParent;
            }

            return myParent.getContainingFrame();
        }

        return null;
    }

    /**
     * Get allowable bounds for rendering this component. For most components,
     * this will be the offset position within the owning frame. For frames this
     * will be a box with the upper left corner at (0, 0) and lower right at the
     * frame extent.
     *
     * @return coordinate reference within the frame.
     */
    public ScreenBoundingBox getDrawBounds()
    {
        return myFrameLocation;
    }

    /**
     * If the component does not fill the entire bounds of its panel, get the
     * actual height it will fill. If the component is initialized, the default
     * behavior is to return the height of the draw bounds. If the component is
     * not initialized this will return 0 by default.
     *
     * @return The height which the component will fill.
     */
    public double getDrawHeight()
    {
        return getDrawBounds().getHeight();
    }

    /**
     * If the component does not fill the entire bounds of its panel, get the
     * actual width it will fill. If the component is initialized, the default
     * behavior is to return the width of the draw bounds. If the component is
     * not initialized this will return 0 by default.
     *
     * @return The width which the component will fill.
     */
    public double getDrawWidth()
    {
        return getDrawBounds().getWidth();
    }

    /**
     * Get the frameLocation.
     *
     * @return the frameLocation
     */
    public ScreenBoundingBox getFrameLocation()
    {
        return myFrameLocation;
    }

    /**
     * Create a tile builder with generic settings.
     *
     * @param <T> position type associated with the geometry.
     *
     * @return new tile builder.
     */
    public <T extends Position> TileGeometry.Builder<T> getGenericTileBuilder()
    {
        TileGeometry.Builder<T> tileBuilder = new TileGeometry.Builder<T>();
        tileBuilder.setDivider(null);
        tileBuilder.setParent(null);
        return tileBuilder;
    }

    /**
     * Aggregate all of the geometries which make up this component.
     *
     * @return Geometries of mine and my children's.
     */
    public abstract Set<Geometry> getGeometries();

    /**
     * Get the horizontalAlignment.
     *
     * @return the horizontalAlignment
     */
    public float getHorizontalAlignment()
    {
        return myHorizontalAlignment;
    }

    /**
     * Get the parent.
     *
     * @return the parent
     */
    public Component getParent()
    {
        return myParent;
    }

    /**
     * Create a non-displayable box for the picking area associated with this
     * component.
     *
     * @param bbox bounding box for the pick tile.
     * @param zOrder zOrder for the pick tile.
     * @return The pick tile.
     */
    public TileGeometry getPickTile(ScreenBoundingBox bbox, int zOrder)
    {
        TileGeometry.Builder<ScreenPosition> builder = getGenericTileBuilder();
        builder.setBounds(bbox);
        builder.setImageManager(null);
        return new TileGeometry(builder, new DefaultTileRenderProperties(zOrder, false, true), null);
    }

    /**
     * Get the transformer.
     *
     * @return the transformer
     */
    public TransformerHelper getTransformer()
    {
        if (myParent != null)
        {
            return myParent.getTransformer();
        }
        return null;
    }

    /**
     * Get the verticalAlignment.
     *
     * @return the verticalAlignment
     */
    public float getVerticalAlignment()
    {
        return myVerticalAlignment;
    }

    /**
     * Remove any listeners which I have created.
     */
    public void handleCleanupListeners()
    {
    }

    /**
     * Handle any necessary actions after the panel has been moved.
     */
    public void handleWindowMoved()
    {
    }

    /** Initialize the component. */
    public abstract void init();

    @Override
    public void mouseClicked(Geometry geom, MouseEvent event)
    {
        if (getParent() != null)
        {
            getParent().mouseClicked(geom, event);
        }
    }

    @Override
    public void mouseDragged(Geometry geom, Point dragStart, MouseEvent event)
    {
        if (getParent() != null)
        {
            getParent().mouseDragged(geom, dragStart, event);
        }
    }

    @Override
    public void mouseEntered(Geometry geom, Point location)
    {
        if (getParent() != null)
        {
            getParent().mouseEntered(geom, location);
        }
    }

    @Override
    public void mouseExited(Geometry geom, Point location)
    {
        if (getParent() != null)
        {
            getParent().mouseExited(geom, location);
        }
    }

    @Override
    public void mouseMoved(Geometry geom, MouseEvent event)
    {
        if (getParent() != null)
        {
            getParent().mouseMoved(geom, event);
        }
    }

    @Override
    public void mousePressed(Geometry geom, MouseEvent event)
    {
        if (getParent() != null)
        {
            getParent().mousePressed(geom, event);
        }
    }

    @Override
    public void mouseReleased(Geometry geom, MouseEvent event)
    {
        if (getParent() != null)
        {
            getParent().mouseReleased(geom, event);
        }
    }

    @Override
    public void mouseWheelMoved(Geometry geom, MouseWheelEvent event)
    {
        if (getParent() != null)
        {
            getParent().mouseWheelMoved(geom, event);
        }
    }

    /**
     * If I am the top level frame, move me. Otherwise, call my parent to move.
     *
     * @param delta change in position.
     */
    public void moveWindow(ScreenPosition delta)
    {
        if (myParent != null)
        {
            myParent.moveWindow(delta);
        }
    }

    /**
     * If I am the top level frame, resize me. Otherwise, call my parent to
     * resize.
     *
     * @param delta change in size.
     */
    public void resizeWindow(ScreenPosition delta)
    {
        if (myParent != null)
        {
            myParent.resizeWindow(delta);
        }
    }

    /**
     * Set the frameLocation.
     *
     * @param frameLocation the frameLocation to set
     */
    public void setFrameLocation(ScreenBoundingBox frameLocation)
    {
        myFrameLocation = frameLocation;
    }

    /**
     * Set the horizontal alignment going from 0 (left) to 1 (right).
     *
     * @param horizontalAlignment the horizontalAlignment to set
     */
    public void setHorizontalAlignment(float horizontalAlignment)
    {
        myHorizontalAlignment = horizontalAlignment;
    }

    /**
     * Set the parent.
     *
     * @param parent the parent to set
     */
    public void setParent(Component parent)
    {
        myParent = parent;
    }

    /**
     * Set the vertical alignment going from 0 (bottom) to 1 (top).
     *
     * @param verticalAlignment the verticalAlignment to set
     */
    public void setVerticalAlignment(float verticalAlignment)
    {
        myVerticalAlignment = verticalAlignment;
    }

    /**
     * Add, Remove or Replace geometries which are being displayed.
     *
     * @param adds Geometries to add.
     * @param removes Geometries to remove.
     */
    public void updateGeometries(Set<? extends Geometry> adds, Set<? extends Geometry> removes)
    {
        if (myParent != null)
        {
            myParent.updateGeometries(adds, removes);
        }
    }
}
