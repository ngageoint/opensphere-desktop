package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.length.Kilometers;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/** Tests for {@link EllipseSemiMinorAxisKey}. */
public class EllipseSemiMinorAxisKeyTest
{
    /** Test for {@link EllipseSemiMinorAxisKey#detectSemiMinor(MetaDataInfo, String)}. */
    @Test
    public void testDetectSemiMinor()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Semi-Minor 95 (km)";
        metaData.addKey(column, Double.class, this);
        EllipseSemiMinorAxisKey.detectSemiMinor(metaData, column);
        EllipseSemiMinorAxisKey actual = (EllipseSemiMinorAxisKey)metaData.getSpecialTypeForKey(column);
        EllipseSemiMinorAxisKey expected = new EllipseSemiMinorAxisKey(Kilometers.class);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }

    /** Test for {@link EllipseSemiMinorAxisKey#isSemiMinor(String)}. */
    @Test
    public void testIsSemiMinor()
    {
        Assert.assertFalse(EllipseSemiMinorAxisKey.isSemiMinor(""));
        Assert.assertFalse(EllipseSemiMinorAxisKey.isSemiMinor("SEMI_MAJOR"));
        Assert.assertTrue(EllipseSemiMinorAxisKey.isSemiMinor("Semi-Minor 95 (km)"));
        Assert.assertTrue(EllipseSemiMinorAxisKey.isSemiMinor("SEMI_MINOR"));
        Assert.assertTrue(EllipseSemiMinorAxisKey.isSemiMinor("SMI"));
        Assert.assertTrue(EllipseSemiMinorAxisKey.isSemiMinor("SMI_NM"));
        Assert.assertTrue(EllipseSemiMinorAxisKey.isSemiMinor("SMIN"));
    }
}
