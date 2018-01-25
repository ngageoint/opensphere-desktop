package io.opensphere.myplaces.specific.points;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.mappoint.impl.MapPointTransformer;
import io.opensphere.mantle.mp.MutableMapAnnotationPoint;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.OpenListener;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.specific.points.renderercontrollers.CalloutDragger;
import io.opensphere.myplaces.specific.points.utils.PointUtils;
import io.opensphere.myplaces.util.ExtendedDataUtils;

/**
 * Renders my points on the map.
 *
 */
public class PointRenderer implements Renderer, Transformer
{
    /**
     * The callouts being currently rendered.
     */
    private final Map<String, Callout> myCallouts = New.map();

    /**
     * Call out ids mapped to their placemarks.
     */
    private final Map<Long, Placemark> myCalloutsToPlacemarks = New.map();

    /**
     * Call out ids mapped to their point.
     */
    private final Map<Long, MutableMapAnnotationPoint> myCalloutsToPoints = New.map();

    /**
     * Allows the user to reposition callouts.
     */
    private final CalloutDragger myDragger;

    /** TODO: Executor should come from Core. */
    private final ScheduledExecutorService myExecutor = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(3,
            new NamedThreadFactory("MyPlacesPoints"), SuppressableRejectedExecutionHandler.getInstance()));

    /** The map point transformer. */
    private final MapPointTransformer myMapPointTransformer;

    /** The Model. */
    private final MyPlacesModel myModel;

    /**
     * Is notified when this transformer is opened.
     */
    private OpenListener myOpenListener;

    /**
     * The points being currently rendered.
     */
    private final Map<String, Long> myPoints = New.map();

    /**
     * Constructs a new point renderer.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     *
     */
    public PointRenderer(Toolbox toolbox, MyPlacesModel model)
    {
        myModel = model;
        myMapPointTransformer = new MapPointTransformer(toolbox, myExecutor);
        myDragger = new CalloutDragger(toolbox, model, myMapPointTransformer, myCalloutsToPlacemarks);
    }

    @Override
    public void addSubscriber(GenericSubscriber<io.opensphere.core.geometry.Geometry> subscriber)
    {
        myMapPointTransformer.addSubscriber(subscriber);
    }

    @Override
    public boolean canRender()
    {
        return myMapPointTransformer.isOpen();
    }

    @Override
    public void close()
    {
        myMapPointTransformer.close();
        myDragger.close();
    }

    @Override
    public String getDescription()
    {
        return myMapPointTransformer.getDescription();
    }

    @Override
    public MapVisualizationType getRenderType()
    {
        return MapVisualizationType.ANNOTATION_POINTS;
    }

    @Override
    public Transformer getTransformer()
    {
        return this;
    }

    @Override
    public void open()
    {
        myMapPointTransformer.open();
        if (myOpenListener != null)
        {
            myOpenListener.opened(this);
        }
    }

    @Override
    public void publishGeometries(Collection<? extends io.opensphere.core.geometry.Geometry> adds,
            Collection<? extends io.opensphere.core.geometry.Geometry> removes)
    {
        myMapPointTransformer.publishGeometries(adds, removes);
    }

    @Override
    public void removeSubscriber(GenericSubscriber<io.opensphere.core.geometry.Geometry> subscriber)
    {
        myMapPointTransformer.removeSubscriber(subscriber);
    }

    @Override
    public synchronized void render(RenderGroup group)
    {
        HashSet<String> knownPlacemarks = new HashSet<>();

        renderDisplayed(group, knownPlacemarks);
        removeHidden(group, knownPlacemarks);
        handleAnyDeletes(knownPlacemarks);
    }

    @Override
    public void setOpenListener(OpenListener openListener)
    {
        myOpenListener = openListener;
    }

    /**
     * Removes any points from the display that has been deleted.
     *
     * @param knownPlacemarks All existing points.
     */
    private void handleAnyDeletes(Set<String> knownPlacemarks)
    {
        HashSet<String> deletedPoints = new HashSet<>();
        // Now check for deleted ones. They will be in my points but not known.
        for (Entry<String, Long> entry : myPoints.entrySet())
        {
            String key = entry.getKey();
            if (!knownPlacemarks.contains(key))
            {
                deletedPoints.add(key);
                myMapPointTransformer.removeDot(entry.getValue().longValue());
            }
        }

        Set<String> deletedCallouts = New.set();
        for (Entry<String, Callout> entry : myCallouts.entrySet())
        {
            String key = entry.getKey();
            if (!knownPlacemarks.contains(key))
            {
                Callout callout = entry.getValue();
                myMapPointTransformer.removeCallOut(callout);
                deletedCallouts.add(key);
                myCalloutsToPoints.remove(Long.valueOf(callout.getId()));
                myCalloutsToPlacemarks.remove(Long.valueOf(callout.getId()));
                myModel.getGeomIdToPlaceMarks().remove(Long.valueOf(callout.getId()));
            }
        }

        for (String deletedCallout : deletedCallouts)
        {
            myCallouts.remove(deletedCallout);
        }

        for (String deletedPoint : deletedPoints)
        {
            Long pointId = myPoints.remove(deletedPoint);
            myModel.getGeomIdToPlaceMarks().remove(pointId);
        }
    }

    /**
     * Removes any visible points that have just become hidden.
     *
     * @param group Contains the hidden points.
     * @param knownPlacemarks The set to add to in order to handle deletes.
     */
    private void removeHidden(RenderGroup group, Set<String> knownPlacemarks)
    {
        for (Placemark placemark : group.getHiddenFeatures())
        {
            knownPlacemarks.add(placemark.getId());
            String key = placemark.getId();
            Callout co = myCallouts.get(key);

            if (co != null)
            {
                myMapPointTransformer.removeCallOut(co);
                myCallouts.remove(key);
                myCalloutsToPoints.remove(Long.valueOf(co.getId()));
                myCalloutsToPlacemarks.remove(Long.valueOf(co.getId()));
                myModel.getGeomIdToPlaceMarks().remove(Long.valueOf(co.getId()));
            }

            Long pointId = myPoints.get(key);
            if (pointId != null)
            {
                myMapPointTransformer.removeDot(pointId.longValue());
                myPoints.remove(key);
                myModel.getGeomIdToPlaceMarks().remove(pointId);
            }
        }
    }

    /**
     * Renders the visible points.
     *
     * @param group Contains the points to render.
     * @param knownPlacemarks The set to add to in order to handle deletes.
     */
    private void renderDisplayed(RenderGroup group, Set<String> knownPlacemarks)
    {
        for (Placemark placemark : group.getFeaturesToRender())
        {
            knownPlacemarks.add(placemark.getId());
            MutableMapAnnotationPoint point = PointUtils.fromKml(placemark, this);

            if (point != null)
            {
                TimeSpan pointTime = point.getTime();
                if (pointTime != null && !ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_ANIMATE, true))
                {
                    pointTime = null;
                }

                if (point.getAnnoSettings().isDotOn() && !myPoints.containsKey(placemark.getId()))
                {
                    myMapPointTransformer.drawDot(point, pointTime);
                    myPoints.put(placemark.getId(), Long.valueOf(point.getId()));
                    myModel.getGeomIdToPlaceMarks().put(Long.valueOf(point.getId()), placemark);
                }
                else if (!point.getAnnoSettings().isDotOn() && myPoints.containsKey(placemark.getId()))
                {
                    Long pointId = myPoints.get(placemark.getId());
                    myMapPointTransformer.removeDot(pointId.longValue());
                    myPoints.remove(placemark.getId());
                }

                if (!point.getAnnoSettings().isAnnohide() && !myCallouts.containsKey(placemark.getId()))
                {
                    Callout co = MapPointTransformer.createCallOut(point);

                    myMapPointTransformer.displayCallOut(co, pointTime);
                    myCallouts.put(placemark.getId(), co);
                    myCalloutsToPoints.put(Long.valueOf(co.getId()), point);
                    myCalloutsToPlacemarks.put(Long.valueOf(co.getId()), placemark);
                    myModel.getGeomIdToPlaceMarks().put(Long.valueOf(co.getId()), placemark);
                }
                else if (point.getAnnoSettings().isAnnohide() && myCallouts.containsKey(placemark.getId()))
                {
                    Callout co = myCallouts.get(placemark.getId());
                    myMapPointTransformer.removeCallOut(co);
                    myCallouts.remove(placemark.getId());
                    myCalloutsToPoints.remove(Long.valueOf(co.getId()));
                    myCalloutsToPlacemarks.remove(Long.valueOf(co.getId()));
                }
            }
        }
    }
}
