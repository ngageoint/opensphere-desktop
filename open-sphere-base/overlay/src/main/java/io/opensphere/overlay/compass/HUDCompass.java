package io.opensphere.overlay.compass;

import java.awt.Rectangle;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window3D;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.math.DefaultSphere;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ScreenViewer;
import io.opensphere.core.viewer.impl.SimpleMapContext;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.core.viewer.impl.Viewer3D.ViewerPosition3D;

/** Class for rendering of a HUD compass. */
public class HUDCompass extends Window3D<GridLayoutConstraints, GridLayout>
{
    /** Listen to events from the main viewer. */
    private final ViewChangeListener myMainViewListener = new ViewChangeListener()
    {
        @Override
        public void viewChanged(final Viewer viewer, final ViewChangeSupport.ViewChangeType type)
        {
            if (getRenderToTextureGeometry() == null)
            {
                // If the view changes before the frame has been initialized,
                // ignore it.
                return;
            }
            myViewChangeExecutor.execute(new Runnable()
            {
                @Override
                public void run()
                {
                    if (type != ViewChangeSupport.ViewChangeType.WINDOW_RESIZE)
                    {
                        MapContext<?> mapContext = getRenderToTextureGeometry().getMapContext();
                        if (mapContext != null)
                        {
                            Viewer privateViewer = mapContext.getStandardViewer();
                            if (privateViewer instanceof Viewer3D)
                            {
                                Viewer3D view3d = (Viewer3D)privateViewer;
                                if (viewer instanceof Viewer3D)
                                {
                                    view3d.setCenteredView(viewer);
                                }
                                else
                                {
                                    view3d.setPosition(new ViewerPosition3D(new Vector3d(25000, 0, 0), new Vector3d(-1., 0., 0.),
                                            Vector3d.UNIT_Z));
                                }
                            }
                        }
                    }
                }
            });
        }
    };

    /** Executor to handle view changes. */
    private final ProcrastinatingExecutor myViewChangeExecutor;

    /**
     * Construct me.
     *
     * @param hudTransformer The transformer.
     * @param executor Executor shared by HUD components.
     * @param size The bounding box we will use for size (but not location on
     *            screen).
     * @param location The predetermined location.
     * @param resize The resize behavior.
     */
    public HUDCompass(TransformerHelper hudTransformer, ScheduledExecutorService executor, ScreenBoundingBox size,
            ToolLocation location, ResizeOption resize)
    {
        super(hudTransformer, size, location, resize, ZOrderRenderProperties.TOP_Z - 20);
        myViewChangeExecutor = new ProcrastinatingExecutor(executor);
    }

    @Override
    public MapContext<Viewer3D> createMapContext()
    {
        ScreenBoundingBox frameBox = getAbsoluteLocation();

        Viewer3D modelViewer = new Viewer3D(new Viewer3D.Builder());

        MapContext<Viewer3D> mapContext = new SimpleMapContext<>(new ScreenViewer(), modelViewer);
        mapContext.reshape((int)frameBox.getWidth(), (int)frameBox.getHeight());
        mapContext.getStandardViewer().setViewOffset(frameBox.getUpperLeft());
        modelViewer.setPosition(new ViewerPosition3D(new Vector3d(25000, 0, 0), modelViewer.getPosition().getDir(),
                modelViewer.getPosition().getUp()));
        modelViewer.setModel(new DefaultSphere(Vector3d.ORIGIN, 6600));

        getTransformer().getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);
        Viewer mainViewer = getTransformer().getToolbox().getMapManager().getStandardViewer();
        mapContext.getStandardViewer().setCenteredView(mainViewer);

        return mapContext;
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    @Override
    public void handleCleanupListeners()
    {
        super.handleCleanupListeners();
        getTransformer().getToolbox().getMapManager().getViewChangeSupport().removeViewChangeListener(myMainViewListener);
    }

    @Override
    public void init()
    {
        initBorder();

        // set the layout
        setLayout(new GridLayout(1, 1, this));

        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(new CompassRenderable(this), constr);

        getLayout().complete();

        getTransformer().getToolbox().getMapManager().getViewChangeSupport().addViewChangeListener(myMainViewListener);
    }

    @Override
    public void repositionForInsets()
    {
    }
}
