package io.opensphere.mantle.data.impl.specialkey;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.impl.DefaultMetaDataInfo;
import io.opensphere.mantle.data.impl.specialkey.HeadingKey.HeadingUnit;

/** Tests for {@link HeadingErrorKey}. */
public class HeadingErrorKeyTest
{
    /** Test for {@link HeadingErrorKey#markSpecialColumn(MetaDataInfo, String)}. */
    @Test
    public void testDetectHeadingError()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        String column = "Heading Error (degN)";
        metaData.addKey(column, Double.class, this);
        HeadingErrorKey.DEFAULT.markSpecialColumn(metaData, column);
        HeadingErrorKey actual = (HeadingErrorKey)metaData.getSpecialTypeForKey(column);
        HeadingErrorKey expected = new HeadingErrorKey(HeadingUnit.DEGREES_CLOCKWISE_FROM_NORTH);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected.getKeyUnit(), actual.getKeyUnit());
    }
}
