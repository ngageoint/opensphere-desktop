package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.HeadingKey.HeadingUnit;

/** Tests for {@link HeadingKey}. */
public class HeadingKeyTest
{
    /** Test for {@link HeadingKey#detectHeading(MetaDataInfo, String)}. */
    @Test
    public void testDetectHeading()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Heading (degN)";
        metaData.addKey(column, Double.class, this);
        HeadingKey.detectHeading(metaData, column);
        HeadingKey actual = (HeadingKey)metaData.getSpecialTypeForKey(column);
        HeadingKey expected = new HeadingKey(HeadingUnit.DEGREES_CLOCKWISE_FROM_NORTH);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }
}
