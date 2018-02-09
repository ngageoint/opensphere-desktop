package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;

/** Tests for {@link SpeedErrorKey}. */
public class SpeedErrorKeyTest
{
    /** Test for {@link SpeedErrorKey#markSpecialColumn(MetaDataInfo, String)}. */
    @Test
    public void testMarkSpecialColumn()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Speed Error (+/-mi/hr)";
        metaData.addKey(column, Double.class, this);
        SpeedErrorKey.DEFAULT.markSpecialColumn(metaData, column);
        SpeedErrorKey actual = (SpeedErrorKey)metaData.getSpecialTypeForKey(column);
        SpeedErrorKey expected = new SpeedErrorKey(SpeedUnit.MILES_PER_HOUR);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }
}
