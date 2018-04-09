package io.opensphere.core.cache.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import io.opensphere.core.model.time.TimeSpan;

/**
 * Test for {@link IntervalPropertyValueSet}.
 */
public class IntervalPropertyValueSetTest
{
    /** Property descriptor for geometries. */
    private static final PropertyDescriptor<Geometry> GEOMETRY_DESC = new PropertyDescriptor<>("geom", Geometry.class);

    /** Property descriptor for time spans. */
    private static final PropertyDescriptor<TimeSpan> TIMESPAN_DESC = new PropertyDescriptor<>("time", TimeSpan.class);

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts a set that eclipses another.
     */
    @Test
    public void testSubtractConsumed()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        Geometry bbox2 = factory.toGeometry(new Envelope(-10, 20., -10., 20.));
        builder.add(GEOMETRY_DESC, bbox2);
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        testEmpty(result1);

        builder.populate(set1);
        TimeSpan time2 = TimeSpan.get(50L, 250L);
        builder.add(TIMESPAN_DESC, time2);
        IntervalPropertyValueSet set3 = builder.create();

        Collection<IntervalPropertyValueSet> result2 = set1.subtract(set3);
        testEmpty(result2);
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts two sets, one with more property types than the other.
     */
    @Test
    public void testSubtractDifferentPropertyTypes()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);

        IntervalPropertyValueSet set1 = builder.create();
        builder.add(GEOMETRY_DESC, bbox1);
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        testEmpty(result1);

        Collection<IntervalPropertyValueSet> result2 = set2.subtract(set1);
        testEmpty(result2);
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts two sets with disjoint geometries.
     */
    @Test
    public void testSubtractDisjoint()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));
        Geometry bbox2 = factory.toGeometry(new Envelope(20., 30., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);
        builder.add(GEOMETRY_DESC, bbox2);

        IntervalPropertyValueSet set1 = builder.create();

        Geometry bbox3 = factory.toGeometry(new Envelope(10., 20., 0., 10.));
        Geometry bbox4 = factory.toGeometry(new Envelope(0., 10., 10., 20.));
        builder.clear(GEOMETRY_DESC);
        builder.add(GEOMETRY_DESC, bbox3);
        builder.add(GEOMETRY_DESC, bbox4);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        Assert.assertEquals(1, result1.size());
        Assert.assertEquals(set1, result1.get(0));

        List<IntervalPropertyValueSet> result2 = set2.subtract(set1);
        Assert.assertEquals(1, result2.size());
        Assert.assertEquals(set2, result2.get(0));
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts two sets with no properties.
     */
    @Test
    public void testSubtractEmpty()
    {
        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();

        IntervalPropertyValueSet set1 = builder.create();
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        testEmpty(result1);
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts a couple of polygons from an error case.
     *
     * @throws ParseException If the test fails to create geometries.
     */
    @Test
    public void testSubtractErrorCase1() throws ParseException
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        WKTReader wktReader = new WKTReader();
        Geometry poly1 = wktReader.read(
                "POLYGON ((64.55128417884413 30.73247621708865, 63.29411581765459 30.73247621708865, 63.29411581765459 31.55986591864089, "
                        + "64.45870405358856 31.55986591864089, 64.40841288399751 31.50432569934371, 64.35253336319998 31.413239984689955, "
                        + "64.316128938156 31.315248163225977, 64.30024972923475 31.213344133075182, 64.30531276660341 31.110631453736527, "
                        + "64.33109731434726 31.010228262831628, 64.37675960695007 30.915172960918618, 64.4408655944464 30.828333452359132, "
                        + "64.52143989004699 30.752322491285277, 64.55128417884413 30.73247621708865))");

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, poly1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear(GEOMETRY_DESC);
        Geometry poly2 = wktReader.read(
                "POLYGON ((63.69346672053501 31.31496501021435, 64.30895870380434 31.269233542397977, 64.30024972923475 31.213344133075182, "
                        + "64.30531276660341 31.110631453736527, 64.33109731434726 31.010228262831628, 64.37675960695007 30.915172960918618, "
                        + "64.44042391158344 30.828931766561226, 64.38325845877026 30.516939477526595, 64.21784375059352 29.996167790621975, "
                        + "63.52627459655317 30.067668909897748, 63.613317688865244 31.01794168867168, 63.69346672053501 31.31496501021435))");
        builder.add(GEOMETRY_DESC, poly2);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result = set1.subtract(set2);
        Assert.assertEquals(1, result.size());

        List<? extends TimeSpan> timeValues = result.get(0).getValues(TIMESPAN_DESC);
        Assert.assertEquals(1, timeValues.size());
        Assert.assertEquals(time1, timeValues.get(0));

        List<? extends Geometry> geomValues = result.get(0).getValues(GEOMETRY_DESC);
        Assert.assertEquals(2, geomValues.size());
        Assert.assertEquals(wktReader.read(
                "POLYGON ((64.55128417884413 30.73247621708865, 64.42275063773745 30.73247621708865, 64.44042391158344 30.828931766561226, "
                        + "64.4408655944464 30.828333452359132, 64.52143989004699 30.752322491285277, 64.55128417884413 30.73247621708865))"),
                geomValues.get(0));
        Assert.assertEquals(wktReader.read(
                "POLYGON ((63.587169620700045 30.73247621708865, 63.29411581765459 30.73247621708865, 63.29411581765459 31.55986591864089, "
                        + "64.45870405358856 31.55986591864089, 64.40841288399751 31.50432569934371, 64.35253336319998 31.413239984689955, "
                        + "64.316128938156 31.315248163225977, 64.30024972923475 31.213344133075182, 64.30895870380434 31.269233542397977, "
                        + "63.69346672053501 31.31496501021435, 63.613317688865244 31.01794168867168, 63.587169620700045 30.73247621708865))"),
                geomValues.get(1));
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * performs a subtraction that will result in a problematic if it wasn't for
     * our heroics.
     *
     * @throws ParseException If the test fails to create geometries.
     */
    @Test
    public void testSubtractErrorCase2() throws ParseException
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        WKTReader wktReader = new WKTReader();
        Geometry poly1 = wktReader.read(
                "POLYGON ((39.74014737334169 29.57267172841399, 39.65698127568833 29.56916980952858, 39.53212882282782 29.58282141346952, "
                        + "39.41183202611622 29.61500955013382, 39.29968746425937 29.66477332836825, 39.19906068114374 29.7306254501901, "
                        + "39.11298893185401 29.81059452656339, 39.04409144256399 29.90228152435198, 38.99448957800802 30.00292901809895, "
                        + "38.96573931252748 30.10950156238839, 38.95877834867712 30.21877514837864, 38.97389008915079 30.32743337063771, "
                        + "39.01068641079537 30.4321676213116, 39.06811078560724 30.52977836643114, 39.1444627253815 30.61727436651371, "
                        + "39.23744379375319 30.69196660560983, 39.34422455370793 30.75155371406734, 39.4615308470035 30.7941958306088, "
                        + "39.5857468041129 30.81857416037243, 39.71303104550396 30.82393394673173, 39.83944174903064 30.81010917097729, "
                        + "39.96106570750726 30.77752799580015, 40.07414624693557 30.72719873422331, 40.17520494862959 30.66067690571989, "
                        + "40.26115250917839 30.58001468449982, 40.32938473615418 30.48769470461947, 40.37786054173216 30.38655072590529, "
                        + "40.40515977201209 30.2796780604425, 40.41051970594799 30.17033690307216, 40.39384999432898 30.06185180609166, "
                        + "40.35572662609236 29.95751050397001, 40.29736617064343 29.86046515090019, 40.29485809992029 29.85762906217206, "
                        + "39.74014737334169 29.85762906217206, 39.74014737334169 29.57267172841399))");

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, poly1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear(GEOMETRY_DESC);
        Geometry poly2 = wktReader.read(
                "POLYGON ((39.09796458962061 29.83058853283741, 39.04409144256399 29.90228152435198, 38.99448957800802 30.00292901809895, "
                        + "38.96573931252748 30.10950156238839, 38.95877834867712 30.21877514837864, 38.97389008915079 30.32743337063771, "
                        + "39.01068641079537 30.4321676213116, 39.06811078560724 30.52977836643114, 39.1444627253815 30.61727436651371, "
                        + "39.23744379375319 30.69196660560983, 39.34422455370793 30.75155371406734, 39.4615308470035 30.7941958306088, "
                        + "39.5857468041129 30.81857416037243, 39.71303104550396 30.82393394673173, 39.83944174903064 30.81010917097729, "
                        + "39.96106570750726 30.77752799580015, 40.07414624693557 30.72719873422331, 40.17520494862959 30.66067690571989, "
                        + "40.26115250917839 30.58001468449982, 40.32938473615418 30.48769470461947, 40.37786054173216 30.38655072590529, "
                        + "40.40515977201209 30.2796780604425, 40.41051970594799 30.17033690307216, 40.39384999432898 30.06185180609166, "
                        + "40.35572662609236 29.95751050397001, 40.29736617064343 29.86046515090019, 40.29485809992029 29.85762906217206, "
                        + "39.97976598538794 29.85762906217206, 39.83903968752042 30.6931426479591, 39.09796458962061 29.83058853283741))");
        builder.add(GEOMETRY_DESC, poly2);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result = set1.subtract(set2);
        Assert.assertEquals(1, result.size());

        List<? extends TimeSpan> timeValues = result.get(0).getValues(TIMESPAN_DESC);
        Assert.assertEquals(1, timeValues.size());
        Assert.assertEquals(time1, timeValues.get(0));

        List<? extends Geometry> geomValues = result.get(0).getValues(GEOMETRY_DESC);
        Assert.assertEquals(1, geomValues.size());
        Assert.assertEquals(wktReader.read(
                "POLYGON ((39.74014737334169 29.57267172841399, 39.65698127568833 29.56916980952858, 39.53212882282782 29.58282141346952, "
                        + "39.41183202611622 29.61500955013382, 39.29968746425937 29.66477332836825, 39.19906068114374 29.7306254501901, "
                        + "39.11298893185401 29.81059452656339, 39.09796458962061 29.83058853283741, 39.83903968752042 30.6931426479591, "
                        + "39.97976598538794 29.85762906217206, 39.74014737334169 29.85762906217206, 39.74014737334169 29.57267172841399))"),
                geomValues.get(0));
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} with
     * two sets that are equal in one property and intersect in the other.
     */
    @Test
    public void testSubtractIntersect1()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        Geometry bbox2 = factory.toGeometry(new Envelope(2., 3., 0., 10.));
        Geometry bbox3 = factory.toGeometry(new Envelope(5., 6., 0., 10.));
        builder.clear(GEOMETRY_DESC);
        builder.add(GEOMETRY_DESC, bbox2);
        builder.add(GEOMETRY_DESC, bbox3);
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        Assert.assertEquals(1, result1.size());
        Map<PropertyDescriptor<?>, List<? extends Object>> map = result1.iterator().next().getMap();
        List<? extends Object> geoms = map.get(GEOMETRY_DESC);
        Assert.assertEquals(3, geoms.size());
        Assert.assertTrue(factory.toGeometry(new Envelope(0., 2., 0., 10.)).equalsTopo((Geometry)geoms.get(0)));
        Assert.assertTrue(factory.toGeometry(new Envelope(3., 5., 0., 10.)).equalsTopo((Geometry)geoms.get(1)));
        Assert.assertTrue(factory.toGeometry(new Envelope(6., 10., 0., 10.)).equalsTopo((Geometry)geoms.get(2)));
        List<? extends Object> times = map.get(TIMESPAN_DESC);
        Assert.assertEquals(1, times.size());
        Assert.assertEquals(time1, times.get(0));

        Collection<IntervalPropertyValueSet> result2 = set2.subtract(set1);
        testEmpty(result2);
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} with
     * two sets that intersect in two properties.
     */
    @Test
    public void testSubtractIntersect2()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);
        TimeSpan time2 = TimeSpan.get(150L, 250L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear();
        Geometry bbox2 = factory.toGeometry(new Envelope(2., 3., 0., 10.));
        builder.add(TIMESPAN_DESC, time2);
        builder.add(GEOMETRY_DESC, bbox2);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        List<IntervalPropertyValueSet> result2 = set2.subtract(set1);

        validateSubtractResults(factory, bbox1, bbox2, result1, result2);
    }

    /**
     * Test for {@link IntervalPropertyValueSet#subtract(List, Collection)} with
     * two sets that intersect in two properties.
     */
    @Test
    public void testSubtractIntersectCollections()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);
        TimeSpan time2 = TimeSpan.get(150L, 250L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear();
        Geometry bbox2 = factory.toGeometry(new Envelope(2., 3., 0., 10.));
        builder.add(TIMESPAN_DESC, time2);
        builder.add(GEOMETRY_DESC, bbox2);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result1 = new ArrayList<>();
        result1.add(set1);
        Assert.assertTrue(IntervalPropertyValueSet.subtract(result1, Collections.singleton(set2)));
        List<IntervalPropertyValueSet> result2 = new ArrayList<>();
        result2.add(set2);
        Assert.assertTrue(IntervalPropertyValueSet.subtract(result2, Collections.singleton(set1)));

        validateSubtractResults(factory, bbox1, bbox2, result1, result2);
    }

    /**
     * Test for {@link IntervalPropertyValueSet#subtract(List, Collection)} with
     * two sets that do not intersect.
     */
    @Test
    public void testSubtractIntersectCollectionsNoIntersection()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);
        TimeSpan time2 = TimeSpan.get(200L, 250L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear();
        Geometry bbox2 = factory.toGeometry(new Envelope(10., 11., 0., 10.));
        builder.add(TIMESPAN_DESC, time2);
        builder.add(GEOMETRY_DESC, bbox2);
        IntervalPropertyValueSet set2 = builder.create();

        List<IntervalPropertyValueSet> result1 = new ArrayList<>();
        result1.add(set1);
        Assert.assertFalse(IntervalPropertyValueSet.subtract(result1, Collections.singleton(set2)));
        List<IntervalPropertyValueSet> result2 = new ArrayList<>();
        result2.add(set2);
        Assert.assertFalse(IntervalPropertyValueSet.subtract(result2, Collections.singleton(set1)));
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} with
     * two sets that do not intersect.
     */
    @Test
    public void testSubtractNoIntersection()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();

        builder.clear();
        TimeSpan time2 = TimeSpan.get(300L, 400L);
        Geometry bbox2 = factory.toGeometry(new Envelope(0., 10., 10., 20.));
        builder.add(TIMESPAN_DESC, time2);
        builder.add(GEOMETRY_DESC, bbox2);
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        Assert.assertEquals(1, result1.size());
        Assert.assertEquals(set1, result1.iterator().next());

        Collection<IntervalPropertyValueSet> result2 = set2.subtract(set1);
        Assert.assertEquals(1, result2.size());
        Assert.assertEquals(set2, result2.iterator().next());
    }

    /**
     * Test for
     * {@link IntervalPropertyValueSet#subtract(IntervalPropertyValueSet)} that
     * subtracts two identical sets.
     */
    @Test
    public void testSubtractSame()
    {
        TimeSpan time1 = TimeSpan.get(100L, 200L);

        GeometryFactory factory = new GeometryFactory();
        Geometry bbox1 = factory.toGeometry(new Envelope(0., 10., 0., 10.));

        IntervalPropertyValueSet.Builder builder = new IntervalPropertyValueSet.Builder();
        builder.add(TIMESPAN_DESC, time1);
        builder.add(GEOMETRY_DESC, bbox1);

        IntervalPropertyValueSet set1 = builder.create();
        IntervalPropertyValueSet set2 = builder.create();

        Collection<IntervalPropertyValueSet> result1 = set1.subtract(set2);
        testEmpty(result1);
    }

    /**
     * Validate some subtraction results.
     *
     * @param factory A geometry factory.
     * @param bbox1 The first box.
     * @param bbox2 The second box.
     * @param result1 The results of the first subtraction.
     * @param result2 The results of the second subtraction.
     */
    public void validateSubtractResults(GeometryFactory factory, Geometry bbox1, Geometry bbox2,
            List<IntervalPropertyValueSet> result1, List<IntervalPropertyValueSet> result2)
    {
        Assert.assertEquals(2, result1.size());
        Map<PropertyDescriptor<?>, List<? extends Object>> map1 = result1.get(0).getMap();
        List<? extends Object> geoms1 = map1.get(GEOMETRY_DESC);
        Assert.assertEquals(1, geoms1.size());
        Assert.assertEquals(bbox1, geoms1.get(0));
        List<? extends Object> times1 = map1.get(TIMESPAN_DESC);
        Assert.assertEquals(1, times1.size());
        Assert.assertEquals(TimeSpan.get(100L, 150L), times1.get(0));

        Map<PropertyDescriptor<?>, List<? extends Object>> map2 = result1.get(1).getMap();
        List<? extends Object> geoms2 = map2.get(GEOMETRY_DESC);
        Assert.assertEquals(2, geoms2.size());
        Assert.assertTrue(factory.toGeometry(new Envelope(0., 2., 0., 10.)).equalsTopo((Geometry)geoms2.get(0)));
        Assert.assertTrue(factory.toGeometry(new Envelope(3., 10., 0., 10.)).equalsTopo((Geometry)geoms2.get(1)));
        List<? extends Object> times2 = map2.get(TIMESPAN_DESC);
        Assert.assertEquals(1, times2.size());
        Assert.assertEquals(TimeSpan.get(150L, 200L), times2.get(0));

        Assert.assertEquals(1, result2.size());
        Map<PropertyDescriptor<?>, List<? extends Object>> map3 = result2.get(0).getMap();
        List<? extends Object> geoms3 = map3.get(GEOMETRY_DESC);
        Assert.assertEquals(1, geoms3.size());
        Assert.assertEquals(bbox2, geoms3.get(0));
        List<? extends Object> times3 = map3.get(TIMESPAN_DESC);
        Assert.assertEquals(1, times3.size());
        Assert.assertEquals(TimeSpan.get(200L, 250L), times3.get(0));
    }

    /**
     * Test that a result is empty.
     *
     * @param result The result.
     */
    private void testEmpty(Collection<IntervalPropertyValueSet> result)
    {
        Assert.assertTrue("Result should have been empty.", result.isEmpty());
    }
}
