package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JFrame;

import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextActionProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.util.Service;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Registers for selection of query regions and displays an information popup
 * when selection occurs.
 */
public class QueryRegionSelectionHandler implements Service
{
    /** The context action manager. */
    private final ContextActionManager myContextActionManager;

    /** The data filter registry. */
    private final DataFilterRegistry myDataFilterRegistry;

    /** The data group controller. */
    private final DataGroupController myDataGroupController;

    /** The main frame supplier. */
    private final Supplier<? extends JFrame> myMainFrameSupplier;

    /** Supplier for the active query regions. */
    private final Supplier<Collection<? extends QueryRegion>> myQueryRegionSupplier;

    /** Handler for region selection. */
    private final ContextActionProvider<GeometryContextKey> myRegionSelectionHandler = new ContextActionProvider<>()
    {
        @Override
        public boolean doAction(String contextId, GeometryContextKey key, int x, int y)
        {
            Optional<? extends QueryRegion> region = myQueryRegionSupplier.get().stream()
                    .filter(r -> r.getGeometries().contains(key.getGeometry())).findAny();
            if (region.isPresent())
            {
                new QueryRegionInfoDialog(myMainFrameSupplier.get(), region.get(), myDataGroupController, myDataFilterRegistry)
                        .buildAndShow();
                return true;
            }
            else
            {
                return false;
            }
        }
    };

    /**
     * Constructor.
     *
     * @param contextActionManager The context action manager, used to registry
     *            for selection.
     * @param dataGroupController The data group controller, used to look up
     *            data types.
     * @param mainFrameSupplier The main frame supplier, used for the parent of
     *            the dialog.
     * @param dataFilterRegistry The data filter registry, used to look up
     *            filters.
     * @param queryRegionSupplier The query region supplier, used to look up
     *            query regions.
     */
    public QueryRegionSelectionHandler(ContextActionManager contextActionManager, DataGroupController dataGroupController,
            Supplier<? extends JFrame> mainFrameSupplier, DataFilterRegistry dataFilterRegistry,
            Supplier<Collection<? extends QueryRegion>> queryRegionSupplier)
    {
        myContextActionManager = contextActionManager;
        myDataGroupController = dataGroupController;
        myMainFrameSupplier = mainFrameSupplier;
        myDataFilterRegistry = dataFilterRegistry;
        myQueryRegionSupplier = queryRegionSupplier;
    }

    /**
     * Close.
     */
    @Override
    public void close()
    {
        myContextActionManager.deregisterDefaultContextActionProvider(ContextIdentifiers.GEOMETRY_DOUBLE_CLICK_CONTEXT,
                GeometryContextKey.class, myRegionSelectionHandler);
    }

    /**
     * Open.
     */
    @Override
    public void open()
    {
        myContextActionManager.registerDefaultContextActionProvider(ContextIdentifiers.GEOMETRY_DOUBLE_CLICK_CONTEXT,
                GeometryContextKey.class, myRegionSelectionHandler);
    }
}
