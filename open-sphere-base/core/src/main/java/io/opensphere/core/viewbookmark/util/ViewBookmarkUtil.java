package io.opensphere.core.viewbookmark.util;

import javax.swing.JOptionPane;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.ViewerAnimator;

/**
 * The Class ViewBookmarkUtil.
 */
public final class ViewBookmarkUtil
{
    /**
     * Reposition the viewer on the position within the view.
     *
     * @param toolbox the {@link Toolbox}
     * @param view the {@link ViewBookmark} to go to.
     */
    public static void gotoView(final Toolbox toolbox, final ViewBookmark view)
    {
        Utilities.checkNull(toolbox, "toolbox");
        if (view != null)
        {
            Viewer privateViewer = toolbox.getMapManager().getStandardViewer();
            if (privateViewer instanceof Viewer3D)
            {
                if (view.is3D())
                {
                    Viewer3D view3d = (Viewer3D)privateViewer;
                    new ViewerAnimator(view3d, view.getViewerPos3D()).start();
                }
                else
                {
                    if (JOptionPane.showConfirmDialog(toolbox.getUIRegistry().getMainFrameProvider().get(),
                            "The selected bookmark is for a 2D projection.  Would you like to switch to 2D to use the bookmark?",
                            "Projection Change Required", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                    {
                        toolbox.getMapManager().setProjection(Viewer2D.class);
                        EventQueueUtilities.runOnEDT(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Viewer viewer = toolbox.getMapManager().getStandardViewer();
                                if (viewer instanceof Viewer2D)
                                {
                                    Viewer2D view2D = (Viewer2D)viewer;
                                    new ViewerAnimator(view2D, view.getViewerPos2D()).start();
                                }
                            }
                        });
                    }
                }
            }
            else if (privateViewer instanceof Viewer2D)
            {
                if (view.is2D())
                {
                    Viewer2D view2d = (Viewer2D)privateViewer;
                    new ViewerAnimator(view2d, view.getViewerPos2D()).start();
                }
                else
                {
                    if (JOptionPane.showConfirmDialog(toolbox.getUIRegistry().getMainFrameProvider().get(),
                            "The selected bookmark is for a 3D projection.  Would you like to switch to 3D to use the bookmark?",
                            "Projection Change Required", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                    {
                        toolbox.getMapManager().setProjection(Viewer3D.class);
                        EventQueueUtilities.runOnEDT(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Viewer viewer = toolbox.getMapManager().getStandardViewer();
                                if (viewer instanceof Viewer3D)
                                {
                                    Viewer3D view3D = (Viewer3D)viewer;
                                    new ViewerAnimator(view3D, view.getViewerPos3D()).start();
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new view book mark utility.
     */
    private ViewBookmarkUtil()
    {
        // Don't allow instantiation.
    }
}
