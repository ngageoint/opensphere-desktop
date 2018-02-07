package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.units.length.NauticalMiles;
import io.opensphere.core.units.length.StatuteMiles;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/** Tests for {@link EllipseSemiMajorAxisKey}. */
public class EllipseSemiMajorAxisKeyTest
{
    /** Test for {@link EllipseSemiMajorAxisKey#detectUnit(String)}. */
    @Test
    public void testDetectUnit()
    {
        Assert.assertEquals(Kilometers.class, EllipseSemiMajorAxisKey.detectUnit("blah (km)"));
        Assert.assertEquals(Kilometers.class, EllipseSemiMajorAxisKey.detectUnit("blah (kilometers)"));
        Assert.assertEquals(Meters.class, EllipseSemiMajorAxisKey.detectUnit("blah (m)"));
        Assert.assertEquals(Meters.class, EllipseSemiMajorAxisKey.detectUnit("blah (meters)"));
        Assert.assertEquals(StatuteMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (mi)"));
        Assert.assertEquals(StatuteMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (miles)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nm)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nmi)"));
        Assert.assertEquals(NauticalMiles.class, EllipseSemiMajorAxisKey.detectUnit("blah (nautical miles)"));
    }

    /** Test for {@link EllipseSemiMajorAxisKey#detectSemiMajor(MetaDataInfo, String)}. */
    @Test
    public void testDetectSemiMajor()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Semi-Major 95 (km)";
        metaData.addKey(column, Double.class, this);
        EllipseSemiMajorAxisKey.detectSemiMajor(metaData, column);
        EllipseSemiMajorAxisKey actual = (EllipseSemiMajorAxisKey)metaData.getSpecialTypeForKey(column);
        EllipseSemiMajorAxisKey expected = new EllipseSemiMajorAxisKey(Kilometers.class);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }

    /** Test for {@link EllipseSemiMajorAxisKey#isSemiMajor(String)}. */
    @Test
    public void testIsSemiMajor()
    {
        Assert.assertFalse(EllipseSemiMajorAxisKey.isSemiMajor(""));
        Assert.assertFalse(EllipseSemiMajorAxisKey.isSemiMajor("SEMI_MINOR"));
        Assert.assertTrue(EllipseSemiMajorAxisKey.isSemiMajor("Semi-Major 95 (km)"));
        Assert.assertTrue(EllipseSemiMajorAxisKey.isSemiMajor("SEMI_MAJOR"));
        Assert.assertTrue(EllipseSemiMajorAxisKey.isSemiMajor("SMA"));
        Assert.assertTrue(EllipseSemiMajorAxisKey.isSemiMajor("SMJ_NM"));
        Assert.assertTrue(EllipseSemiMajorAxisKey.isSemiMajor("SMAJ"));
    }
}
