package io.opensphere.core.geometry.constraint;

import org.junit.Test;

import io.opensphere.core.model.time.TimeSpan;
import org.junit.Assert;

/**
 * Test for {@link StrictTimeConstraint}.
 */
public class StrictTimeConstraintTest
{
    /**
     * Test for {@link StrictTimeConstraint#check(TimeSpan)}.
     */
    @Test
    public void testCheckTimeSpan()
    {
        StrictTimeConstraint con = new StrictTimeConstraint(null, TimeSpan.get(100L, 200L));

        Assert.assertFalse(con.check(TimeSpan.get(50L, 50L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 99L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 100L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 101L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 199L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(50L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(99L, 99L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 100L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 101L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 199L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(99L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(100L, 100L)));
        Assert.assertFalse(con.check(TimeSpan.get(100L, 101L)));
        Assert.assertFalse(con.check(TimeSpan.get(100L, 199L)));
        Assert.assertTrue(con.check(TimeSpan.get(100L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(100L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(100L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(101L, 101L)));
        Assert.assertFalse(con.check(TimeSpan.get(101L, 199L)));
        Assert.assertFalse(con.check(TimeSpan.get(101L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(101L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(101L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(199L, 199L)));
        Assert.assertFalse(con.check(TimeSpan.get(199L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(199L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(199L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(200L, 200L)));
        Assert.assertFalse(con.check(TimeSpan.get(200L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(200L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(201L, 201L)));
        Assert.assertFalse(con.check(TimeSpan.get(201L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.get(250L, 250L)));

        Assert.assertFalse(con.check(TimeSpan.TIMELESS));
    }
}
