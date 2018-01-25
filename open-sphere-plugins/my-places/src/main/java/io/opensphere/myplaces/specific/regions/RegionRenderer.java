package io.opensphere.myplaces.specific.regions;

import java.awt.Color;
import java.awt.Font;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.callout.Callout;
import io.opensphere.core.callout.CalloutDragListener;
import io.opensphere.core.callout.CalloutImpl;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.mgrs.UTM;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.BoundingBoxes;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.units.angle.Angle;
import io.opensphere.core.units.angle.DecimalDegrees;
import io.opensphere.core.units.angle.DegreesMinutesSeconds;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.callout.CalloutManager;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.WrappedMapSet;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.OpenListener;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.specific.regions.utils.RegionUtils;
import io.opensphere.myplaces.util.ExtendedDataUtils;
import io.opensphere.myplaces.util.PlacemarkUtils;

/**
 * Renders my points on the map.
 *
 */
public class RegionRenderer extends DefaultTransformer implements Renderer
{
    /** The manager for the callouts. */
    private final CalloutManager<PlacemarkWrapper> myCalloutManager;

    /**
     * Subscriber for geometries from the callout manager.
     */
    private final GenericSubscriber<Geometry> myCalloutSubscriber = (source, adds, removes) -> publishGeometries(adds, removes);

    /** The listener for callout dragging. */
    private final CalloutDragListener<PlacemarkWrapper> myDragListener = new CalloutDragListener<PlacemarkWrapper>()
    {
        @Override
        public void calloutDragged(PlacemarkWrapper key, Vector2i offset, int index)
        {
            Placemark placemark = key.getPlacemark();

            ExtendedData extendedData = placemark.getExtendedData();

            ExtendedDataUtils.putInt(extendedData, Constants.X_OFFSET_ID, offset.getX());
            ExtendedDataUtils.putInt(extendedData, Constants.Y_OFFSET_ID, offset.getY());

            myModel.notifyObservers();
        }
    };

    /** The places model. */
    private final MyPlacesModel myModel;

    /** Listener for when this transformer is opened. */
    private OpenListener myOpenListener;

    /** Map of models to geometries. */
    private final Map<Placemark, PolygonGeometry> myPlacemarkGeometryMap = new IdentityHashMap<>();

    /**
     * Constructs a new point renderer.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     *
     */
    public RegionRenderer(Toolbox toolbox, MyPlacesModel model)
    {
        super(toolbox.getDataRegistry());

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("Region Callout Manager"), SuppressableRejectedExecutionHandler.getInstance());
        myCalloutManager = new CalloutManager<>(toolbox.getControlRegistry(), "My Places Plugin", executor,
                myDragListener);

        myCalloutManager.addSubscriber(myCalloutSubscriber);

        myModel = model;
    }

    @Override
    public boolean canRender()
    {
        return isOpen();
    }

    @Override
    public void close()
    {
        super.close();

        myCalloutManager.close();
    }

    @Override
    public MapVisualizationType getRenderType()
    {
        return MapVisualizationType.ANNOTATION_REGIONS;
    }

    @Override
    public Transformer getTransformer()
    {
        return this;
    }

    @Override
    public void open()
    {
        super.open();
        if (myOpenListener != null)
        {
            myOpenListener.opened(this);
        }
    }

    @Override
    public synchronized void render(RenderGroup group)
    {
        Collection<Geometry> adds = New.collection();

        List<Placemark> featuresToRender = group.getFeaturesToRender();
        for (Placemark placemark : featuresToRender)
        {
            if (!myPlacemarkGeometryMap.containsKey(placemark))
            {
                PolygonGeometry geom = RegionUtils.createGeometry(placemark);
                myPlacemarkGeometryMap.put(placemark, geom);
                myModel.getGeomIdToPlaceMarks().put(Long.valueOf(geom.getDataModelId()), placemark);
                if (ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_FEATURE_ON_ID, true))
                {
                    adds.add(geom);
                }

                if (!ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_ANNOHIDE_ID, false))
                {
                    addCallout(geom, placemark, adds);
                }
            }
        }

        Collection<Geometry> removes = New.collection();
        Set<Placemark> featureSet = new WrappedMapSet<>(new IdentityHashMap<Placemark, Void>(), null);
        featureSet.addAll(featuresToRender);

        for (Iterator<Entry<Placemark, PolygonGeometry>> iter = myPlacemarkGeometryMap.entrySet().iterator(); iter.hasNext();)
        {
            Entry<Placemark, PolygonGeometry> entry = iter.next();
            Placemark placemark = entry.getKey();
            if (!featureSet.contains(placemark))
            {
                removes.add(entry.getValue());
                iter.remove();

                myCalloutManager.removeCallouts(new PlacemarkWrapper(placemark), removes);
            }
        }

        publishGeometries(adds, removes);
    }

    @Override
    public void setOpenListener(OpenListener openListener)
    {
        myOpenListener = openListener;
    }

    /**
     * Add text representation of the coordinates.
     *
     * @param southwest The south-west coordinates.
     * @param northeast The north-east coordinates.
     * @param units The units to display.
     * @param text The output list of text.
     */
    protected void addCoordinates(LatLonAlt southwest, LatLonAlt northeast, Class<? extends Angle> units,
            List<? super String> text)
    {
        text.add(StringUtilities.concat("SW: ", Angle.create(units, southwest.getLatD()).toShortLabelString('N', 'S'), " ",
                Angle.create(units, southwest.getLonD()).toShortLabelString('E', 'W')));
        text.add(StringUtilities.concat("NE: ", Angle.create(units, northeast.getLatD()).toShortLabelString('N', 'S'), " ",
                Angle.create(units, northeast.getLonD()).toShortLabelString('E', 'W')));
    }

    /**
     * Get the anchor point for the callout associated with a geometry.
     *
     * @param geom The geometry.
     * @return The geographic anchor.
     */
    protected LatLonAlt getCalloutAnchor(PolygonGeometry geom)
    {
        LatLonAlt anchor = null;
        for (Position position : geom.getVertices())
        {
            GeographicPosition geoPos = (GeographicPosition)position;
            LatLonAlt lla = geoPos.getLatLonAlt();

            if (anchor == null || lla.getLatD() >= anchor.getLatD() && lla.getLonD() >= anchor.getLonD())
            {
                anchor = lla;
            }
        }
        return anchor;
    }

    /**
     * Get the text for a callout associated with a placemark.
     *
     * @param geom The geometry.
     * @param placemark The placemark.
     * @return The text lines.
     */
    protected List<? extends String> getCalloutText(PolygonGeometry geom, Placemark placemark)
    {
        List<String> text = New.list();
        if (ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_TITLE, false))
        {
            text.add(placemark.getName());
        }
        if (ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_DESC_ID, false))
        {
            text.add(StringUtilities.addHTMLLineBreaks(placemark.getDescription(), 25));
        }
        boolean mgrs = ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_MGRS, false);
        boolean dms = ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_DMS_ID, false);
        boolean lla = ExtendedDataUtils.getBoolean(placemark.getExtendedData(), Constants.IS_LAT_LON_ID, false);
        if (mgrs || dms || lla)
        {
            @SuppressWarnings("unchecked")
            BoundingBox<GeographicPosition> bbox = BoundingBoxes
                    .getMinimumBoundingBox((Collection<? extends GeographicPosition>)geom.getVertices());
            LatLonAlt lla1 = bbox.getLowerLeft().getLatLonAlt();
            LatLonAlt lla2 = bbox.getUpperRight().getLatLonAlt();
            if (mgrs)
            {
                MGRSConverter converter = new MGRSConverter();

                String mgrs1 = converter.createString(new UTM(lla1.getLatD(), lla1.getLonD()));
                String mgrs2 = converter.createString(new UTM(lla2.getLatD(), lla2.getLonD()));
                if (mgrs1.equals(mgrs2))
                {
                    text.add(mgrs1);
                }
                else
                {
                    text.add(StringUtilities.concat("SW: ", mgrs1));
                    text.add(StringUtilities.concat("NE: ", mgrs2));
                }
            }
            if (dms)
            {
                addCoordinates(lla1, lla2, DegreesMinutesSeconds.class, text);
            }
            if (lla)
            {
                addCoordinates(lla1, lla2, DecimalDegrees.class, text);
            }
        }

        return text;
    }

    /**
     * Create a callout and add it to the callout manager.
     *
     * @param geom The geometry.
     * @param placemark The placemark.
     * @param adds The return collection of added geometries.
     */
    private void addCallout(PolygonGeometry geom, Placemark placemark, Collection<? super Geometry> adds)
    {
        LatLonAlt anchor = getCalloutAnchor(geom);
        if (anchor == null)
        {
            return;
        }

        List<? extends String> text = getCalloutText(geom, placemark);

        if (!text.isEmpty())
        {
            Font font = PlacemarkUtils.getPlacemarkFont(placemark);

            Color lineColor = PlacemarkUtils.getPlacemarkColor(placemark);
            Color textColor = PlacemarkUtils.getPlacemarkTextColor(placemark);

            Callout callout = new CalloutImpl(geom.getDataModelId(), text, anchor, font);
            callout.setBorderColor(lineColor);
            callout.setTextColor(textColor);
            callout.setCornerRadius(10);

            ExtendedData extendedData = placemark.getExtendedData();
            int xOffset = ExtendedDataUtils.getInt(extendedData, Constants.X_OFFSET_ID, 10);
            int yOffset = ExtendedDataUtils.getInt(extendedData, Constants.Y_OFFSET_ID, 10);

            callout.setAnchorOffset(new Vector2i(xOffset, yOffset));

            boolean filled = ExtendedDataUtils.getBoolean(extendedData, Constants.IS_BUBBLE_FILLED_ID, true);
            if (filled)
            {
                callout.setBackgroundColor(lineColor);
            }

            if (placemark.getTimePrimitive() != null && ExtendedDataUtils.getBoolean(extendedData, Constants.IS_ANIMATE, true))
            {
                callout.setTime(KMLSpatialTemporalUtils.timeSpanFromTimePrimitive(placemark.getTimePrimitive()));
            }

            myCalloutManager.addCallout(new PlacemarkWrapper(placemark), callout, adds);
        }
    }

    /**
     * A wrapper for a {@link Placemark} that uses {@link Placemark} identity
     * for {@link #equals(Object)} and {@link #hashCode()}.
     */
    private static class PlacemarkWrapper
    {
        /** The wrapped placemark. */
        private final Placemark myPlacemark;

        /**
         * Construct the placemark wrapper.
         *
         * @param placemark The wrapped placemark.
         */
        public PlacemarkWrapper(Placemark placemark)
        {
            myPlacemark = placemark;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || getClass() != obj.getClass())
            {
                return false;
            }
            PlacemarkWrapper other = (PlacemarkWrapper)obj;
            return Utilities.sameInstance(myPlacemark, other.myPlacemark);
        }

        /**
         * Get the wrapped placemark.
         *
         * @return The placemark.
         */
        public Placemark getPlacemark()
        {
            return myPlacemark;
        }

        @Override
        public int hashCode()
        {
            return System.identityHashCode(myPlacemark);
        }
    }
}
