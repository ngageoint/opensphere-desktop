package io.opensphere.core.pipeline.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.geometry.ConstrainableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.core.viewer.impl.MapContext;
import org.junit.Assert;

/** Test for {@link SpatialTemporalGeometryComparator}. */
public class SpatialTemporalGeometryComparatorTest
{
    /** A position for testing. */
    private static final GeographicPosition POS1 = new GeographicPosition(
            LatLonAlt.createFromDegrees(0, 0, ReferenceLevel.ELLIPSOID));

    /** A position for testing. */
    private static final GeographicPosition POS2 = new GeographicPosition(
            LatLonAlt.createFromDegrees(1, 0, ReferenceLevel.ELLIPSOID));

    /** A position for testing. */
    private static final GeographicPosition POS3 = new GeographicPosition(
            LatLonAlt.createFromDegrees(2, 0, ReferenceLevel.ELLIPSOID));

    /** A position for testing. */
    private static final GeographicPosition POS4 = new GeographicPosition(
            LatLonAlt.createFromDegrees(3, 0, ReferenceLevel.ELLIPSOID));

    /** A map of positions to vectors. */
    private static final Map<GeographicPosition, Vector3d> POSITION_MAP = new HashMap<>();

    /**
     * Active time spans for testing.
     */
    private final ActiveTimeSpans myActive = new ActiveTimeSpans()
    {
        @Override
        public int getDirection()
        {
            return 0;
        }

        @Override
        public Fade getFade()
        {
            return null;
        }

        @Override
        public TimeSpanList getPrimary()
        {
            return myTimeSpanList;
        }

        @Override
        public Map<Object, Collection<? extends TimeSpan>> getSecondary()
        {
            return Collections.emptyMap();
        }
    };

    /** The center point of the view. */
    private Vector3d myCenterPoint;

    /** The comparator to be tested. */
    private SpatialTemporalGeometryComparator myComparator;

    /** The time listener. */
    private ActiveTimeSpanChangeListener myTimeListener;

    /** The primary active time spans. */
    private TimeSpanList myTimeSpanList;

    /** The view change support. */
    private final ViewChangeSupport myViewChangeSupport = new ViewChangeSupport();

    /** The mock viewer. */
    private Viewer myViewer;

    static
    {
        POSITION_MAP.put(POS1, new Vector3d(0., 0., 0.));
        POSITION_MAP.put(POS2, new Vector3d(1., 0., 0.));
        POSITION_MAP.put(POS3, new Vector3d(2., 0., 0.));
        POSITION_MAP.put(POS4, new Vector3d(3., 0., 0.));
    }

    /**
     * Initialize the test.
     */
    @Before
    public void setUp()
    {
        TimeManager timeManager = EasyMock.createNiceMock(TimeManager.class);
        EasyMock.expect(timeManager.getPrimaryActiveTimeSpans()).andAnswer(new IAnswer<TimeSpanList>()
        {
            @Override
            public TimeSpanList answer()
            {
                return myTimeSpanList;
            }
        }).anyTimes();
        timeManager.addActiveTimeSpanChangeListener(EasyMock.<ActiveTimeSpanChangeListener>anyObject());
        EasyMock.expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer()
            {
                myTimeListener = (ActiveTimeSpanChangeListener)EasyMock.getCurrentArguments()[0];
                return null;
            }
        }).anyTimes();
        EasyMock.replay(timeManager);

        Projection proj = EasyMock.createNiceMock(Projection.class);
        EasyMock.expect(proj.convertToModel(EasyMock.<GeographicPosition>anyObject(), EasyMock.eq(Vector3d.ORIGIN)))
                .andAnswer(new IAnswer<Vector3d>()
                {
                    @Override
                    public Vector3d answer()
                    {
                        return POSITION_MAP.get(EasyMock.getCurrentArguments()[0]);
                    }
                }).anyTimes();
        EasyMock.replay(proj);

        myViewer = EasyMock.createNiceMock(Viewer.class);
        EasyMock.expect(myViewer.getModelIntersection()).andAnswer(new IAnswer<Vector3d>()
        {
            @Override
            public Vector3d answer()
            {
                return myCenterPoint;
            }
        }).anyTimes();
        EasyMock.replay(myViewer);

        @SuppressWarnings("unchecked")
        MapContext<Viewer> mapContext = EasyMock.createNiceMock(MapContext.class);
        EasyMock.expect(mapContext.getStandardViewer()).andReturn(myViewer).anyTimes();
        EasyMock.expect(mapContext.getViewChangeSupport()).andReturn(myViewChangeSupport).anyTimes();
        EasyMock.expect(mapContext.getProjection()).andReturn(proj).anyTimes();
        EasyMock.replay(mapContext);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(150L, 150L));

        myComparator = new SpatialTemporalGeometryComparator(mapContext, timeManager);
    }

    /** Test with geographic geometries. */
    @Test
    public void testGeographic()
    {
        Geometry geom1 = EasyMock.createNiceMock(Geometry.class);
        EasyMock.expect(geom1.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.replay(geom1);
        Geometry geom2 = EasyMock.createNiceMock(Geometry.class);
        EasyMock.expect(geom2.getReferencePoint()).andReturn(POS2).anyTimes();
        EasyMock.replay(geom2);
        Geometry geom3 = EasyMock.createNiceMock(Geometry.class);
        EasyMock.expect(geom3.getReferencePoint()).andReturn(POS3).anyTimes();
        EasyMock.replay(geom3);
        Geometry geom4 = EasyMock.createNiceMock(Geometry.class);
        EasyMock.expect(geom4.getReferencePoint()).andReturn(POS4).anyTimes();
        EasyMock.replay(geom4);

        List<Geometry> list = new ArrayList<>();
        list.add(geom4);
        list.add(geom3);
        list.add(geom2);
        list.add(geom1);

        myCenterPoint = POSITION_MAP.get(POS1);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom1, geom2, geom3, geom4), list);

        myCenterPoint = POSITION_MAP.get(POS2);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom2, geom1, geom3, geom4), list);

        myCenterPoint = POSITION_MAP.get(POS3);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom3, geom2, geom4, geom1), list);

        myCenterPoint = POSITION_MAP.get(POS4);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom4, geom3, geom2, geom1), list);
    }

    /** Test geographic geometries with temporal constraints. */
    @Test
    public void testSpatialTemporal()
    {
        Constraints cons1 = Constraints.createTimeOnlyConstraint(TimeSpan.get(100L, 200L));
        Constraints cons2 = Constraints.createTimeOnlyConstraint(TimeSpan.get(200L, 300L));

        ConstrainableGeometry geom1 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom1.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom1.getConstraints()).andReturn(cons1).anyTimes();
        EasyMock.replay(geom1);
        ConstrainableGeometry geom2 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom2.getReferencePoint()).andReturn(POS2).anyTimes();
        EasyMock.expect(geom2.getConstraints()).andReturn(cons1).anyTimes();
        EasyMock.replay(geom2);
        ConstrainableGeometry geom3 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom3.getReferencePoint()).andReturn(POS3).anyTimes();
        EasyMock.expect(geom3.getConstraints()).andReturn(cons2).anyTimes();
        EasyMock.replay(geom3);
        ConstrainableGeometry geom4 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom4.getReferencePoint()).andReturn(POS4).anyTimes();
        EasyMock.expect(geom4.getConstraints()).andReturn(cons2).anyTimes();
        EasyMock.replay(geom4);

        List<Geometry> list = new ArrayList<>();
        list.add(geom4);
        list.add(geom3);
        list.add(geom2);
        list.add(geom1);

        myCenterPoint = POSITION_MAP.get(POS3);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(151L, 151L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom2, geom1, geom3, geom4), list);

        myCenterPoint = POSITION_MAP.get(POS3);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(251L, 251L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom3, geom4, geom2, geom1), list);

        myCenterPoint = POSITION_MAP.get(POS1);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(151L, 151L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom1, geom2, geom3, geom4), list);

        myCenterPoint = POSITION_MAP.get(POS1);
        myViewChangeSupport.notifyViewChangeListeners(myViewer, null, ViewChangeType.VIEW_CHANGE);
        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(251L, 251L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom3, geom4, geom1, geom2), list);
    }

    /** Test geometries with temporal constraints. */
    @Test
    public void testTemporal()
    {
        Constraints cons0 = Constraints.createTimeOnlyConstraint(TimeSpan.newUnboundedStartTimeSpan(100L));
        Constraints cons1 = Constraints.createTimeOnlyConstraint(TimeSpan.get(100L, 200L));
        Constraints cons2 = Constraints.createTimeOnlyConstraint(TimeSpan.get(200L, 300L));
        Constraints cons3 = Constraints.createTimeOnlyConstraint(TimeSpan.get(300L, 400L));
        Constraints cons4 = Constraints.createTimeOnlyConstraint(TimeSpan.get(400L, 500L));
        Constraints cons5 = Constraints.createTimeOnlyConstraint(TimeSpan.newUnboundedEndTimeSpan(500L));

        ConstrainableGeometry geom0 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom0.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom0.getConstraints()).andReturn(cons0).anyTimes();
        EasyMock.replay(geom0);
        ConstrainableGeometry geom1 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom1.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom1.getConstraints()).andReturn(cons1).anyTimes();
        EasyMock.replay(geom1);
        ConstrainableGeometry geom2 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom2.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom2.getConstraints()).andReturn(cons2).anyTimes();
        EasyMock.replay(geom2);
        ConstrainableGeometry geom3 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom3.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom3.getConstraints()).andReturn(cons3).anyTimes();
        EasyMock.replay(geom3);
        ConstrainableGeometry geom4 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom4.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom4.getConstraints()).andReturn(cons4).anyTimes();
        EasyMock.replay(geom4);
        ConstrainableGeometry geom5 = EasyMock.createNiceMock(ConstrainableGeometry.class);
        EasyMock.expect(geom5.getReferencePoint()).andReturn(POS1).anyTimes();
        EasyMock.expect(geom5.getConstraints()).andReturn(cons5).anyTimes();
        EasyMock.replay(geom5);

        List<Geometry> list = new ArrayList<>();
        list.add(geom5);
        list.add(geom4);
        list.add(geom3);
        list.add(geom2);
        list.add(geom1);
        list.add(geom0);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(51L, 51L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom0, geom1, geom2, geom3, geom4, geom5), list);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(151L, 151L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom1, geom2, geom0, geom3, geom4, geom5), list);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(251L, 251L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom2, geom3, geom1, geom4, geom0, geom5), list);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(351L, 351L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom3, geom4, geom2, geom5, geom1, geom0), list);

        myTimeSpanList = TimeSpanList.singleton(TimeSpan.get(451L, 451L));
        myTimeListener.activeTimeSpansChanged(myActive);
        Collections.sort(list, myComparator);
        Assert.assertEquals(Arrays.asList(geom4, geom5, geom3, geom2, geom1, geom0), list);
    }
}
