package io.opensphere.controlpanels.recording;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.control.ui.MenuBarRegistry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Transformer for video playing. */
public class VideoPlayerTransformer extends DefaultTransformer
{
    /** The default context menu provider. */
    private ContextMenuProvider<GeometryContextKey> myContextMenuProvider;

    /** The chooser for selecting video files to play. */
    private final MnemonicFileChooser myFileChooser;

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     */
    public VideoPlayerTransformer(Toolbox toolbox)
    {
        super(null);
        Utilities.checkNull(toolbox, "toolbox");
        myFileChooser = new MnemonicFileChooser(toolbox.getPreferencesRegistry(), "VideoFileDialog");

        myToolbox = toolbox;

        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myToolbox.getUIRegistry().getMenuBarRegistry().getMenu(MenuBarRegistry.MAIN_MENU_BAR, MenuBarRegistry.DEBUG_MENU)
                        .add(getDebugMenu());
            }
        });
    }

    @Override
    public void close()
    {
        if (myToolbox.getUIRegistry() != null && myContextMenuProvider != null)
        {
            ContextActionManager mgr = myToolbox.getUIRegistry().getContextActionManager();
            mgr.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                    myContextMenuProvider);
        }

        super.close();
    }

    /**
     * The the debug options for video playing.
     *
     * @return The menu which contains the debug options.
     */
    private JMenuItem getDebugMenu()
    {
        JMenu menu = new JMenu("Video");
        final JMenuItem geoVid = new JMenuItem("Allow videos on the globe");
        geoVid.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setupGeoVideos();
            }
        });
        menu.add(geoVid);

        final JMenuItem playVid = new JMenuItem("Play a video externally");
        playVid.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                playExternalVideo();
            }
        });
        menu.add(playVid);

        final JMenuItem playVidCorner = new JMenuItem("Play a video in the corner");
        playVidCorner.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                playCanvasVideo(new ScreenBoundingBox(new ScreenPosition(-300, 0), new ScreenPosition(-1, 200)));
            }
        });
        menu.add(playVidCorner);

        return menu;
    }

    /**
     * Play a video mapped to the given location.
     *
     * @param location The location of the video.
     */
    private void playCanvasVideo(BoundingBox<? extends Position> location)
    {
        CanvasVideoPlayer player = new CanvasVideoPlayer(this, location);
        playVideo(player);
    }

    /** Play a video in an external frame. */
    private void playExternalVideo()
    {
        ExternalVideoPlayer player = new ExternalVideoPlayer();
        playVideo(player);
    }

    /**
     * Helper method for selecting the file to send to the player.
     *
     * @param player The player which will be used to play the video.
     */
    private void playVideo(AbstractVideoPlayer player)
    {
        int returnVal = myFileChooser.showDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), "Play Video");
        String url = "";
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                url = myFileChooser.getSelectedFile().getAbsoluteFile().toURI().toURL().toString();
            }
            catch (MalformedURLException e)
            {
                url = null;
            }
        }

        url = (String)JOptionPane.showInputDialog(null, "Enter URL", "Enter URL", JOptionPane.INFORMATION_MESSAGE, null, null,
                url);
        if (url != null)
        {
            player.playVideo(url, myToolbox);
        }
    }

    /** Setup the menu provider to allow videos to be run on the globe. */
    private void setupGeoVideos()
    {
        if (myToolbox.getUIRegistry() != null)
        {
            myContextMenuProvider = new ContextMenuProvider<GeometryContextKey>()
            {
                @Override
                public List<? extends Component> getMenuItems(String contextId, final GeometryContextKey key)
                {
                    JMenuItem mi = new JMenuItem("Play a Video");
                    mi.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            if (key.getGeometry() instanceof PolygonGeometry)
                            {
                                PolygonGeometry geom = (PolygonGeometry)key.getGeometry();
                                if (geom.getPositionType() == GeographicPosition.class)
                                {
                                    double maxLat = -180.;
                                    double minLat = 180.;
                                    double maxLon = -180.;
                                    double minLon = 180.;
                                    for (Position pos : geom.getVertices())
                                    {
                                        GeographicPosition geoPos = (GeographicPosition)pos;
                                        double lat = geoPos.getLatLonAlt().getLatD();
                                        double lon = geoPos.getLatLonAlt().getLonD();

                                        maxLat = Math.max(lat, maxLat);
                                        minLat = Math.min(lat, minLat);
                                        maxLon = Math.max(lon, maxLon);
                                        minLon = Math.min(lon, minLon);
                                    }
                                    GeographicPosition ll = new GeographicPosition(LatLonAlt.createFromDegrees(minLat, minLon));
                                    GeographicPosition ur = new GeographicPosition(LatLonAlt.createFromDegrees(maxLat, maxLon));
                                    GeographicBoundingBox location = new GeographicBoundingBox(ll, ur);
                                    playCanvasVideo(location);
                                }
                            }
                        }
                    });
                    return Collections.singletonList(mi);
                }

                @Override
                public int getPriority()
                {
                    return 11001;
                }
            };
            ContextActionManager mgr = myToolbox.getUIRegistry().getContextActionManager();
            mgr.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT, GeometryContextKey.class,
                    myContextMenuProvider);
            mgr.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT, GeometryContextKey.class,
                    myContextMenuProvider);
        }
    }
}
