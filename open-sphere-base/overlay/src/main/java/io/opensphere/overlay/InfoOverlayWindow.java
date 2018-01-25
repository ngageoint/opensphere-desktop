package io.opensphere.overlay;

import java.awt.Point;
import java.awt.event.MouseEvent;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.ControlEventSupport;
import io.opensphere.core.hud.framework.Renderable;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Utilities;

/**
 * The Class InfoOverlayWindow.
 */
abstract class InfoOverlayWindow extends Window<GridLayoutConstraints, GridLayout>
{
    /** The Info over lay renderable. */
    private InfoOverLayRenderable myInfoOverLayRenderable;

    /**
     * Instantiates a new info overlay window.
     *
     * @param helper the helper
     * @param bbox the bbox
     * @param loc the loc
     * @param resizeKeepFixedSize the resize keep fixed size
     * @param zOrder the z order
     */
    public InfoOverlayWindow(TransformerHelper helper, ScreenBoundingBox bbox, ToolLocation loc, ResizeOption resizeKeepFixedSize,
            int zOrder)
    {
        super(helper, bbox, loc, ResizeOption.RESIZE_KEEP_FIXED_SIZE, zOrder);
    }

    /**
     * Gets the info over lay renderable.
     *
     * @return the info over lay renderable
     */
    public InfoOverLayRenderable getInfoOverLayRenderable()
    {
        if (myInfoOverLayRenderable == null)
        {
            myInfoOverLayRenderable = new InfoOverLayRenderable(this);
        }
        return myInfoOverLayRenderable;
    }

    /**
     * Adds the labels.
     */
    abstract void addLabels();

    /**
     * Adds the renderable.
     */
    abstract void addRenderable();

    /**
     * The Class CursorWindowRenderable.
     */
    static class InfoOverLayRenderable extends Renderable
    {
        /** Support class for events from the control context. */
        private final ControlEventSupport myMouseSupport;

        /** The Pick tile. */
        private TileGeometry myPickTile;

        /**
         * Instantiates a new cursor window renderable.
         *
         * @param parent the parent
         */
        public InfoOverLayRenderable(Component parent)
        {
            super(parent);
            myMouseSupport = new ControlEventSupport(this, getTransformer().getToolbox().getControlRegistry());
        }

        @Override
        public void handleCleanupListeners()
        {
            myMouseSupport.cleanupListeners();
        }

        @Override
        public void init()
        {
            myPickTile = getPickTile(getParent().getDrawBounds(), 20);
            getGeometries().add(myPickTile);
            myMouseSupport.setActionGeometry(myPickTile);
        }

        @Override
        public void mouseDragged(Geometry geom, Point dragStart, MouseEvent evt)
        {
            if (Utilities.sameInstance(geom, myPickTile))
            {
                Point end = evt.getPoint();
                moveWindow(new ScreenPosition((int)(end.getX() - dragStart.getX()), (int)(end.getY() - dragStart.getY())));
            }
        }
    }
}
