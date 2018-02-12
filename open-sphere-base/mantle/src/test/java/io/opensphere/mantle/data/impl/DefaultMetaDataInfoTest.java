package io.opensphere.mantle.data.impl;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.mantle.data.impl.specialkey.HeadingKey;
import io.opensphere.mantle.data.impl.specialkey.SpeedKey;
import io.opensphere.mantle.data.impl.specialkey.SpeedUnit;

/** Tests for {@link DefaultMetaDataInfo}. */
public class DefaultMetaDataInfoTest
{
    /** Test for {@link DefaultMetaDataInfo#hasTypeForSpecialKey(io.opensphere.mantle.data.SpecialKey)}. */
    @Test
    public void testHasTypeForSpecialKey()
    {
        DefaultMetaDataInfo metaData = new DefaultMetaDataInfo();
        metaData.addKey("speed", Double.class, this);
        metaData.setSpecialKey("speed", new SpeedKey(SpeedUnit.KILOMETERS_PER_HOUR), this);
        Assert.assertTrue(metaData.hasTypeForSpecialKey(new SpeedKey(SpeedUnit.KILOMETERS_PER_HOUR)));
        Assert.assertTrue(metaData.hasTypeForSpecialKey(SpeedKey.DEFAULT));
        Assert.assertFalse(metaData.hasTypeForSpecialKey(HeadingKey.DEFAULT));
    }
}
