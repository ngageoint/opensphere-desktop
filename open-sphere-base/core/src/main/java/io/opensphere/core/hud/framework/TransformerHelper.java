package io.opensphere.core.hud.framework;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.RenderToTextureGeometry;
import io.opensphere.core.geometry.TileGeometry;

/** Helper class for publishing geometries. */
public class TransformerHelper
{
    /** The pixel distance to inset frames within the main frame. */
    private int myFramesInset;

    /** When true frames will stick to the edges of the main frame. */
    private boolean myFramesSticky;

    /** Tool box. */
    private final Toolbox myToolbox;

    /** The actual transformer. */
    private final Transformer myTransformer;

    /** Example frame. */
    private final Set<Window<?, ?>> myWindows = Collections.synchronizedSet(new HashSet<Window<?, ?>>());

    /**
     * Constructor.
     *
     * @param transformer The actual transformer.
     * @param toolbox toolbox
     */
    public TransformerHelper(Transformer transformer, Toolbox toolbox)
    {
        myTransformer = transformer;
        myToolbox = toolbox;
    }

    /**
     * Cleanup all of my windows.
     */
    public void cleanup()
    {
        for (Window<?, ?> window : myWindows)
        {
            window.closeWindow();
        }
    }

    /**
     * Remove the geometries for a frame.
     *
     * @param frame frame to close
     */
    public void closeFrame(Window<?, ?> frame)
    {
        Set<RenderToTextureGeometry> rttgAdds = Collections.<RenderToTextureGeometry>emptySet();
        Set<RenderToTextureGeometry> rttgRemoves = Collections.singleton(frame.getRenderToTextureGeometry());
        myTransformer.publishGeometries(rttgAdds, rttgRemoves);

        Set<TileGeometry> tileAdds = Collections.<TileGeometry>emptySet();
        Set<? extends TileGeometry> tileRemoves = Collections.singleton(frame.getRenderToTextureGeometry().getTileGeometry());
        myTransformer.publishGeometries(tileAdds, tileRemoves);

        myWindows.remove(frame);
    }

    /**
     * Publish the geometries for a frame.
     *
     * @param frame frame to display
     */
    public void displayFrame(Window<?, ?> frame)
    {
        Set<RenderToTextureGeometry> rttgAdds = Collections.singleton(frame.getRenderToTextureGeometry());
        Set<RenderToTextureGeometry> rttgRemoves = Collections.<RenderToTextureGeometry>emptySet();
        myTransformer.publishGeometries(rttgAdds, rttgRemoves);

        Set<? extends TileGeometry> tileAdds = Collections.singleton(frame.getRenderToTextureGeometry().getTileGeometry());
        Set<TileGeometry> tileRemoves = Collections.<TileGeometry>emptySet();
        myTransformer.publishGeometries(tileAdds, tileRemoves);
    }

    /**
     * Get the framesInset.
     *
     * @return the framesInset
     */
    public int getFramesInset()
    {
        return myFramesInset;
    }

    /**
     * Get the toolbox.
     *
     * @return the toolbox
     */
    public Toolbox getToolbox()
    {
        return myToolbox;
    }

    /**
     * Tell whether the frames are sticky.
     *
     * @return true when the frames are sticky.
     */
    public boolean isFramesSticky()
    {
        return myFramesSticky;
    }

    /**
     * Publish created geometries.
     *
     * @param adds New geometries.
     * @param removes Removed geometries.
     */
    public final void publishGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        myTransformer.publishGeometries(adds, removes);
    }

    /**
     * Replace a RenderToTextureGeomery with a new one.
     *
     * @param frame old geometry.
     * @param newFrame new geometry.
     */
    public void replaceFrame(RenderToTextureGeometry frame, RenderToTextureGeometry newFrame)
    {
        Set<RenderToTextureGeometry> rttgAdds = Collections.singleton(newFrame);
        Set<RenderToTextureGeometry> rttgRemoves = Collections.singleton(frame);
        myTransformer.publishGeometries(rttgAdds, rttgRemoves);

        Set<? extends TileGeometry> tileAdds = Collections.singleton(newFrame.getTileGeometry());
        Set<? extends TileGeometry> tileRemoves = Collections.singleton(frame.getTileGeometry());
        myTransformer.publishGeometries(tileAdds, tileRemoves);
    }

    /**
     * Replace a frame with a new one.
     *
     * @param frame original frame.
     * @param newFrame new frame.
     */
    public void replaceFrame(Window<?, ?> frame, Window<?, ?> newFrame)
    {
        myWindows.remove(frame);
        replaceFrame(frame.getRenderToTextureGeometry(), newFrame.getRenderToTextureGeometry());
    }

    /**
     * Add, remove, or replace a geometry. At least one argument should be
     * non-null.
     *
     * @param oldGeom original geometry.
     * @param newGeom replacement geometry.
     */
    public void replaceGeometry(Geometry oldGeom, Geometry newGeom)
    {
        Set<Geometry> removes = null;
        if (oldGeom == null)
        {
            removes = Collections.emptySet();
        }
        else
        {
            removes = Collections.singleton(oldGeom);
        }

        Set<Geometry> adds;
        if (newGeom == null)
        {
            adds = Collections.emptySet();
        }
        else
        {
            adds = Collections.singleton(newGeom);
        }

        myTransformer.publishGeometries(adds, removes);
    }
}
