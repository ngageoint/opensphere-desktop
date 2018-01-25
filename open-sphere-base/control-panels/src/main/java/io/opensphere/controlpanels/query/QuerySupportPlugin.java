package io.opensphere.controlpanels.query;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.ScreenPositionContextKey;
import io.opensphere.core.event.ApplicationLifecycleEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionListener;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class QuerySupportPlugin.
 */
public class QuerySupportPlugin extends PluginAdapter implements QueryRegionListener
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(QuerySupportPlugin.class);

    /** The clear all search results provider. */
    private final ContextMenuProvider<Void> myClearQueryRegionsMenuProvider = new ContextMenuProvider<Void>()
    {
        @Override
        public List<? extends JMenuItem> getMenuItems(String contextId, Void key)
        {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show query regions", myFeaturesSelected);
            menuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myFeaturesSelected = ((JCheckBoxMenuItem)e.getSource()).isSelected();
                    setQueryRegionsVisible(myFeaturesSelected);
                }
            });
            return Collections.singletonList(menuItem);
        }

        @Override
        public int getPriority()
        {
            return 11900;
        }
    };

    /** The Feature selector check box. */
    private JCheckBox myFeatureSelectorCheckBox;

    /** Flag indicating if the query regions are visible. */
    private boolean myFeaturesSelected = true;

    /** The my life cycle event listener. */
    @SuppressWarnings("PMD.SingularField")
    private transient EventListener<ApplicationLifecycleEvent> myLifeCycleEventListener;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** Context menu provider for the whole world query. */
    private final ContextMenuProvider<ScreenPositionContextKey> myWorldQueryMenuProvider = new ContextMenuProvider<ScreenPositionContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, ScreenPositionContextKey key)
        {
            JMenuItem menuItem = new JMenuItem("Query entire world...");
            menuItem.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int opt = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                            "The whole earth query may return limited results.\n\n"
                                    + "This query is best used in conjunction with a filter.\n\nDo you want to proceed?",
                            "Whole Earth Query Notice", JOptionPane.YES_NO_OPTION);
                    if (opt == JOptionPane.OK_OPTION)
                    {
                        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<GeographicPosition>();
                        builder.setVertices(GeographicBoundingBox.WHOLE_GLOBE.getVertices());

                        PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(
                                ZOrderRenderProperties.TOP_Z, false, false);
                        PolygonGeometry geom = new PolygonGeometry(builder, renderProperties, (Constraints)null);

                        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
                        mantleToolbox.getQueryRegionManager().addQueryRegion(Collections.singleton(geom),
                                mantleToolbox.getDataGroupController().getQueryableDataTypeKeys());
                    }
                }
            });
            return Collections.singletonList(menuItem);
        }

        @Override
        public int getPriority()
        {
            return 11800;
        }
    };

    @Override
    public void allQueriesRemoved(boolean animationPlanCancelled)
    {
        queryRegionsChanged();
    }

    /**
     * Gets the feature selector checkbox.
     *
     * @return the feature selector checkbox
     */
    public final JCheckBox createFeatureSelectorCheckbox()
    {
        if (myFeatureSelectorCheckBox == null)
        {
            myFeatureSelectorCheckBox = new JCheckBox();
            myFeatureSelectorCheckBox.setSelected(true);
            myFeaturesSelected = true;
            try
            {
                myFeatureSelectorCheckBox
                        .setIcon(getImageIcon(QuerySupportPlugin.class.getResource("/images/featureSelectorUnselected.png")));
                myFeatureSelectorCheckBox.setSelectedIcon(
                        getImageIcon(QuerySupportPlugin.class.getResource("/images/featureSelectorSelected.png")));
            }
            catch (IOException e)
            {
                LOGGER.error("IOException reading images.", e);
            }
            myFeatureSelectorCheckBox.setBorder(null);
            myFeatureSelectorCheckBox.setFocusPainted(false);
            myFeatureSelectorCheckBox.setToolTipText("Show/Hide Feature Selectors");
            myFeatureSelectorCheckBox.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    myFeaturesSelected = myFeatureSelectorCheckBox.isSelected();
                    setQueryRegionsVisible(myFeatureSelectorCheckBox.isSelected());
                }
            });
        }
        return myFeatureSelectorCheckBox;
    }

    /**
     * Gets the image icon.
     *
     * @param pURL the uRL
     * @return the image icon
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public ImageIcon getImageIcon(URL pURL) throws IOException
    {
        ImageIcon icon = null;
        icon = new ImageIcon(ImageIO.read(pURL));
        return icon;
    }

    @Override
    public void initialize(PluginLoaderData plugindata, Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);
        myToolbox = toolbox;

        toolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(
                ContextIdentifiers.SCREEN_POSITION_CONTEXT, ScreenPositionContextKey.class, myWorldQueryMenuProvider);

        myLifeCycleEventListener = new EventListener<ApplicationLifecycleEvent>()
        {
            @Override
            public void notify(ApplicationLifecycleEvent event)
            {
                if (event.getStage() == ApplicationLifecycleEvent.Stage.PLUGINS_INITIALIZED)
                {
                    MantleToolboxUtils.getMantleToolbox(myToolbox).getQueryRegionManager()
                            .addQueryRegionListener(QuerySupportPlugin.this);
                }
            }
        };
        myToolbox.getEventManager().subscribe(ApplicationLifecycleEvent.class, myLifeCycleEventListener);

        myToolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myClearQueryRegionsMenuProvider);
    }

    @Override
    public void queryRegionAdded(QueryRegion region)
    {
        queryRegionsChanged();
    }

    @Override
    public void queryRegionRemoved(QueryRegion region)
    {
        queryRegionsChanged();
    }

    /**
     * Query regions changed.
     */
    public void queryRegionsChanged()
    {
        final int queryCount = MantleToolboxUtils.getMantleToolbox(myToolbox).getQueryRegionManager().getQueryRegions().size();
        if (queryCount > 0)
        {
            if (!myFeaturesSelected)
            {
                setQueryRegionsVisible(myFeaturesSelected);
            }
        }
        else
        {
            myFeaturesSelected = true;
            createFeatureSelectorCheckbox().setSelected(true);
        }
    }

    /**
     * Sets the query regions visible.
     *
     * @param visible the new query regions visible
     */
    private void setQueryRegionsVisible(boolean visible)
    {
        QueryRegionManager qManager = MantleToolboxUtils.getMantleToolbox(myToolbox).getQueryRegionManager();
        for (QueryRegion region : qManager.getQueryRegions())
        {
            for (PolygonGeometry pg : region.getGeometries())
            {
                if (visible)
                {
                    pg.getRenderProperties().setColorARGB(0xFF << 24 | pg.getRenderProperties().getColorARGB());
                }
                else
                {
                    pg.getRenderProperties().setColorARGB(0xFFFFFF & pg.getRenderProperties().getColorARGB());
                }
            }
        }
    }
}
