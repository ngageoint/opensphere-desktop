package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/** Tests for {@link SpeedKey}. */
public class SpeedKeyTest
{
    /** Test for {@link SpeedKey#markSpecialColumn(MetaDataInfo, String)}. */
    @Test
    public void testMarkSpecialColumn()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Speed (m/s)";
        metaData.addKey(column, Double.class, this);
        SpeedKey.DEFAULT.markSpecialColumn(metaData, column);
        SpeedKey actual = (SpeedKey)metaData.getSpecialTypeForKey(column);
        SpeedKey expected = new SpeedKey(SpeedUnit.METERS_PER_SECOND);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());

        metaData = new DefaultMetaDataInfo();
        column = "Speed (km/hr)";
        metaData.addKey(column, Double.class, this);
        SpeedKey.DEFAULT.markSpecialColumn(metaData, column);
        actual = (SpeedKey)metaData.getSpecialTypeForKey(column);
        expected = new SpeedKey(SpeedUnit.KILOMETERS_PER_HOUR);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }
}
