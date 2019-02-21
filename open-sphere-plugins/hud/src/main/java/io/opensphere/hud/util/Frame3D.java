package io.opensphere.hud.util;

import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.Frame;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;
import io.opensphere.core.viewer.impl.Viewer3D;

/** A frame which uses a 3d projection. */
public class Frame3D extends Frame<GridLayoutConstraints, GridLayout>
{
    /**
     * Child component which will either be renderable or contain the renderable
     * components.
     */
    private Component myChild;

    /** The viewers for the sub-frame. */
    private MapContext<Viewer3D> myMapContext;

    /**
     * Construct me.
     *
     * @param parent parent component. components.
     */
    public Frame3D(Component parent)
    {
        super(parent);
    }

    @Override
    public MapContext<Viewer3D> createMapContext()
    {
        ScreenBoundingBox frameBox = getAbsoluteLocation();

        myMapContext = new SimpleMapContext<>(new ScreenViewer(), new Viewer3D(new Viewer3D.Builder(), false));
        myMapContext.reshape((int)frameBox.getWidth(), (int)frameBox.getHeight());
        myMapContext.getStandardViewer().setViewOffset(frameBox.getUpperLeft());

        return myMapContext;
    }

    /**
     * Get the child.
     *
     * @return the child
     */
    public Component getChild()
    {
        return myChild;
    }

    /**
     * Get the mapContext.
     *
     * @return the mapContext
     */
    public MapContext<Viewer3D> getMapContext()
    {
        return myMapContext;
    }

    @Override
    public void handleWindowMoved()
    {
        ScreenBoundingBox frameBox = getAbsoluteLocation();
        myMapContext.getStandardViewer().setViewOffset(frameBox.getUpperLeft());
    }

    @Override
    public void init()
    {
        initBorder();

        // set the layout
        setLayout(new GridLayout(1, 1, this));

        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myChild, constr);

        getLayout().complete();

        // setup the mapmanager
        setupRenderToTexture();
    }

    /**
     * Set the child.
     *
     * @param child the child to set
     */
    public void setChild(Component child)
    {
        myChild = child;
    }
}
