package io.opensphere.overlay.arc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.event.DataRemovalEvent;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolylineGeometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.callout.CalloutManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.Pair;

/**
 * Manager for dragging the label bubbles and handling context menus and units
 * changes.
 */
public class CompletedArcManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CompletedArcManager.class);

    /** The manager for actions associated with completed arcs. */
    private final ContextActionManager myActionManager;

    /** The arcs which I manage. */
    private final Collection<Arc> myArcs = New.collection();

    /**
     * Subscriber for geometries from the callout manager.
     */
    private final GenericSubscriber<Geometry> myCalloutSubscriber = new GenericSubscriber<Geometry>()
    {
        @Override
        public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
        {
            myTransformer.publishGeometries(adds, removes);
        }
    };

    /** The Delete context menu provider. */
    private final ContextMenuProvider<Void> myDeleteMenuProvider = new ContextMenuProvider<Void>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, Void key)
        {
            JMenuItem clearArcs = new JMenuItem("Clear Ruler Measurements");
            clearArcs.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    clearAll();
                }
            });
            return Collections.singletonList(clearArcs);
        }

        @Override
        public int getPriority()
        {
            return 9;
        }
    };

    /**
     * The menu provider for context menus for polylines. If this is a line
     * which I created, then I will provide options for acting against the line.
     */
    private final ContextMenuProvider<GeometryContextKey> myMenuProvider = new ContextMenuProvider<GeometryContextKey>()
    {
        @Override
        public List<JMenuItem> getMenuItems(String contextId, GeometryContextKey key)
        {
            if (!(key.getGeometry() instanceof PolylineGeometry))
            {
                return null;
            }

            final PolylineGeometry geom = (PolylineGeometry)key.getGeometry();
            List<JMenuItem> menuItems = null;
            final Arc associatedArc = getAssociatedArc(geom);
            if (associatedArc != null)
            {
                menuItems = New.list();

                JMenu unitsMenu = new JMenu("Change Measurement Units");
                addUnitsMenuItems(geom, unitsMenu);
                menuItems.add(unitsMenu);

                JMenuItem select = new JMenuItem("Remove Measurement");
                select.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        synchronized (myArcs)
                        {
                            myArcs.remove(associatedArc);
                        }
                        Collection<Geometry> removes = New.collection();
                        removes.addAll(associatedArc.getAllGeometries());
                        myCalloutManager.removeCallouts(associatedArc, removes);
                        myTransformer.publishGeometries(Collections.<Geometry>emptyList(), removes);
                    }
                });
                menuItems.add(select);

                JMenuItem selectEx = new JMenuItem("Clear All");
                selectEx.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        clearAll();
                    }
                });
                menuItems.add(selectEx);
            }

            return menuItems;
        }

        @Override
        public int getPriority()
        {
            return 11300;
        }
    };

    /** Event listener that triggers removal of all measurements. */
    private final EventListener<DataRemovalEvent> myRemoveAllMeasuresEventListener = new EventListener<DataRemovalEvent>()
    {
        @Override
        public void notify(DataRemovalEvent event)
        {
            clearAll();
        }
    };

    /** The tool box used by plugins to interact with the rest of the system. */
    private final Toolbox myToolbox;

    /** The transformer for publishing my associated geometries. */
    private final Transformer myTransformer;

    /** The listener to system units changes. */
    private final UnitsChangeListener<Length> myUnitsChangeListener = new UnitsChangeListener<Length>()
    {
        @Override
        public void availableUnitsChanged(Class<Length> superType, Collection<Class<? extends Length>> newTypes)
        {
        }

        @Override
        public void preferredUnitsChanged(final Class<? extends Length> units)
        {
            Collection<Geometry> adds = New.collection();
            Collection<Geometry> removes = New.collection();

            synchronized (myArcs)
            {
                Collection<Arc> addArcs = New.collection(myArcs.size());
                for (Arc arc : myArcs)
                {
                    if (arc.hasCustomUnits())
                    {
                        addArcs.add(arc);
                    }
                    else
                    {
                        removes.addAll(arc.getAllGeometries());
                        myCalloutManager.removeCallouts(arc, removes);
                        Arc addArc = new Arc(arc.getVertices(), units, true);
                        addArcs.add(addArc);
                        adds.addAll(addArc.getAllGeometries());
                        myCalloutManager.addCallouts(addArc, addArc.createCallouts(true, true), adds);
                    }
                }
                myArcs.clear();
                myArcs.addAll(addArcs);
            }

            myTransformer.publishGeometries(adds, removes);
        }
    };

    /** The callout manager. */
    private final CalloutManager<Arc> myCalloutManager;

    /**
     * Constructor.
     *
     * @param transformer The transformer for publishing my associated
     *            geometries.
     * @param toolbox The tool box used by plugins to interact with the rest of
     *            the system.
     */
    public CompletedArcManager(Transformer transformer, Toolbox toolbox)
    {
        myTransformer = transformer;
        myToolbox = toolbox;
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager = myToolbox.getUIRegistry().getContextActionManager();
            myActionManager.registerContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, myMenuProvider);
            myActionManager.registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT, Void.class, myDeleteMenuProvider);
        }
        else
        {
            myActionManager = null;
        }
        myToolbox.getUnitsRegistry().getUnitsProvider(Length.class).addListener(myUnitsChangeListener);
        myToolbox.getEventManager().subscribe(DataRemovalEvent.class, myRemoveAllMeasuresEventListener);

        myCalloutManager = new CalloutManager<Arc>(myToolbox.getControlRegistry(), "Overlay", new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("Overlay Plugin"), SuppressableRejectedExecutionHandler.getInstance()));
        myCalloutManager.addSubscriber(myCalloutSubscriber);
    }

    /**
     * Create a new arc from the positions and manage it.
     *
     * @param positions The positions which form the arc segments.
     */
    public void addCompletedArc(List<Pair<GeographicPosition, AbstractRenderableGeometry>> positions)
    {
        Arc arc = new Arc(positions, getPreferredUnits(), true);
        synchronized (myArcs)
        {
            myArcs.add(arc);
        }
        Collection<Geometry> adds = New.collection();
        adds.addAll(arc.getAllGeometries());
        myCalloutManager.addCallouts(arc, arc.createCallouts(true, true), adds);
        myTransformer.publishGeometries(adds, Collections.<Geometry>emptyList());
    }

    /** Perform any required cleanup. */
    public void close()
    {
        myCalloutManager.close();
        if (myToolbox.getUIRegistry() != null)
        {
            myActionManager.deregisterContextMenuItemProvider(ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT,
                    GeometryContextKey.class, myMenuProvider);
        }
        clearAll();
    }

    /**
     * Add all of the available units for the length to the menu.
     *
     * @param geom The geometry.
     * @param unitsMenu The unit change sub-menu.
     */
    private void addUnitsMenuItems(final PolylineGeometry geom, JMenu unitsMenu)
    {
        for (final Class<? extends Length> lengthType : myToolbox.getUnitsRegistry().getAvailableUnits(Length.class, true))
        {
            JMenuItem unitsItem;
            try
            {
                unitsItem = new JMenuItem(Length.getSelectionLabel(lengthType));
                unitsItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        handleUnitChange(geom, lengthType);
                    }
                });
                unitsMenu.add(unitsItem);
            }
            catch (InvalidUnitsException e)
            {
                LOGGER.warn("Could not use length type: " + e, e);
            }
        }
    }

    /**
     * Remove all of my arcs and un-publish the associated geometries.
     */
    private void clearAll()
    {
        Quantify.collectMetric("mist3d.overlay.ruler.clear-ruler-measurements");
        Collection<Geometry> removes = New.collection();
        synchronized (myArcs)
        {
            for (Arc arc : myArcs)
            {
                removes.addAll(arc.getAllGeometries());
                myCalloutManager.removeCallouts(arc, removes);
            }
            myArcs.clear();
        }
        myTransformer.publishGeometries(Collections.<Geometry>emptyList(), removes);
    }

    /**
     * Get the arc which owns the polyline.
     *
     * @param geom the polyline for which the owing arc is desired.
     * @return the arc which owns the polyline.
     */
    private Arc getAssociatedArc(PolylineGeometry geom)
    {
        synchronized (myArcs)
        {
            for (Arc arc : myArcs)
            {
                if (Utilities.sameInstance(geom, arc.getLine()))
                {
                    return arc;
                }
            }
        }
        return null;
    }

    /**
     * Get the currently preferred units.
     *
     * @return The units.
     */
    private Class<? extends Length> getPreferredUnits()
    {
        return myToolbox.getUnitsRegistry().getPreferredUnits(Length.class);
    }

    /**
     * Change the arc length label for the geometry to match the given units.
     *
     * @param geom The arc.
     * @param units The units in which to display length.
     */
    private void handleUnitChange(PolylineGeometry geom, Class<? extends Length> units)
    {
        Collection<Geometry> removes = New.collection();
        Collection<Geometry> adds = New.collection();
        synchronized (myArcs)
        {
            Arc removeArc = getAssociatedArc(geom);
            removes.addAll(removeArc.getAllGeometries());
            myCalloutManager.removeCallouts(removeArc, removes);
            Arc addArc = new Arc(removeArc.getVertices(), units, true);
            addArc.setHasCustomUnits(!units.equals(getPreferredUnits()));
            adds.addAll(addArc.getAllGeometries());
            myCalloutManager.addCallouts(addArc, addArc.createCallouts(true, true), adds);

            myArcs.remove(removeArc);
            myArcs.add(addArc);
        }

        myTransformer.publishGeometries(adds, removes);
    }
}
