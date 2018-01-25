package io.opensphere.kml.tree.controller;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Container;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.util.lang.Nulls;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.model.KMLMapController;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLToolboxUtils;

/**
 * Check box selection listener for the KML tree. Shows/hides elements in mantle
 * based on the check box selections.
 */
public class KMLCheckBoxSelectionListener implements TreeSelectionListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(KMLCheckBoxSelectionListener.class);

    /** The data registry. */
    private final DataRegistry myDataRegistry;

    /** Whether to ignore selections. */
    private volatile boolean myIgnoreSelections;

    /** The mantle controller. */
    private final KMLMapController myMantleController;

    /**
     * Gets a list of parent data sources that need to be activated/deactivated.
     *
     * @param feature The feature
     * @param isSelect True for select, false for deselect
     * @return The list of parent data sources
     */
    private static Collection<KMLDataSource> getParentDataSources(KMLFeature feature, boolean isSelect)
    {
        Collection<KMLDataSource> parentDataSources = new ArrayList<>();
        KMLDataSource parentDataSource = feature.getCreatingDataSource();
        while (parentDataSource != null)
        {
            // If a data source is active on an unselect, or a data source is
            // not active on a select, add it to the list of parent data sources
            // that need to be de/activated.
            if (parentDataSource.getCreatingFeature() != null && isSelect != parentDataSource.isActive())
            {
                if (isSelect)
                {
                    parentDataSources.add(parentDataSource);
                }
                else
                {
                    boolean hasVisibleChildren = parentDataSource.getAllFeatures().stream()
                            .anyMatch(f -> f.isVisibility().booleanValue()
                                    && !(f.getFeature() instanceof Container || f.getFeature() instanceof NetworkLink));
                    if (!hasVisibleChildren)
                    {
                        parentDataSources.add(parentDataSource);
                    }
                }
            }
            else
            {
                break;
            }

            parentDataSource = parentDataSource.getParentDataSource();
        }
        return parentDataSources;
    }

    /**
     * Logs a TreeSelectionEvent.
     *
     * @param msg The message
     * @param e The TreeSelectionEvent
     */
    private static void logTreeSelectionEvent(String msg, TreeSelectionEvent e)
    {
        if (LOGGER.isDebugEnabled())
        {
            StringBuilder sb = new StringBuilder(msg);
            TreePath[] paths = e.getPaths();
            for (int i = 0; i < paths.length; i++)
            {
                sb.append('\n').append(e.isAddedPath(i)).append(' ').append(paths[i].toString());
            }
            LOGGER.debug(sb.toString());
        }
    }

    /**
     * Constructor.
     *
     * @param mantleController The mantle controller
     * @param dataRegistry The data registry.
     */
    public KMLCheckBoxSelectionListener(final KMLMapController mantleController, DataRegistry dataRegistry)
    {
        myMantleController = mantleController;
        myDataRegistry = dataRegistry;
    }

    /**
     * Setter for ignoreSelections.
     *
     * @param ignoreSelections the ignoreSelections
     */
    public void setIgnoreSelections(boolean ignoreSelections)
    {
        myIgnoreSelections = ignoreSelections;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e)
    {
        if (!myIgnoreSelections)
        {
            handleCheckBoxSelectionEvent(e);
        }
    }

    /**
     * Handles a tree check box selection event by updating the mantle as
     * necessary.
     *
     * @param e The TreeSelectionEvent
     */
    private void handleCheckBoxSelectionEvent(TreeSelectionEvent e)
    {
        logTreeSelectionEvent("\nTreeSelectionEvent: ", e);

        // Get stuff out of the event
        final boolean isSelect = e.isAddedPath(0);
        TreePath[] paths = e.getPaths();

        // Get a list of the selected features plus all their children
        final Collection<KMLFeature> features = new ArrayList<>();
        final Collection<KMLFeature> rootFeatures = new ArrayList<>(paths.length);
        for (int i = 0; i < paths.length; i++)
        {
            TreePath path = paths[i];
            if (path.getLastPathComponent() instanceof DefaultMutableTreeNode)
            {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
                if (treeNode.getUserObject() instanceof KMLFeature)
                {
                    KMLFeature feature = (KMLFeature)treeNode.getUserObject();
                    features.addAll(feature.getAllFeatures());
                    rootFeatures.add(feature);
                }
            }
        }

        KMLToolboxUtils.getKmlToolbox().getPluginExecutor().execute(new Runnable()
        {
            @Override
            public void run()
            {
                // Handle selection of the features and their children
                handleFeatureSelection(isSelect, features);

                // Handle any parent internal data sources
                for (KMLFeature feature : rootFeatures)
                {
                    for (KMLDataSource parentDataSource : getParentDataSources(feature, isSelect))
                    {
                        parentDataSource.setActive(isSelect);
                    }
                }
            }
        });
    }

    /**
     * Handles selection of features.
     *
     * @param isSelect True for select, false for deselect
     * @param features The collection of features
     */
    private void handleFeatureSelection(boolean isSelect, Collection<KMLFeature> features)
    {
        if (!features.isEmpty())
        {
            for (KMLFeature feature : features)
            {
                feature.setVisibility(Boolean.valueOf(isSelect));
            }

            // Add any new features to the mantle
            if (isSelect)
            {
                myMantleController.addFeatures(features);
            }

            // Update feature visibility
            myMantleController.updateFeatureVisibility(features);

            // Handle any child internal data sources
            for (KMLFeature feature : features)
            {
                KMLDataSource dataSource = feature.getResultingDataSource();
                if (dataSource != null)
                {
                    // Set the activity of this data source and all its children
                    // so they will load or not load
                    dataSource.setActive(isSelect);

                    // Load the internal data source
                    if (isSelect && feature.isRegionActive() && !dataSource.isLoaded()
                            && !(feature.getFeature() instanceof Overlay))
                    {
                        KMLDataRegistryHelper.queryAndActivate(myDataRegistry, dataSource, dataSource.getPath(), Nulls.STRING);
                    }
                }
            }
        }
    }
}
