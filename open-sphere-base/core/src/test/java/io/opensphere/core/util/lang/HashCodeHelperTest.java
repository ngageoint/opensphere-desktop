package io.opensphere.core.util.lang;

import org.junit.Assert;
import org.junit.Test;

/** Test for {@link HashCodeHelper}. */
public class HashCodeHelperTest
{
    /** Test for {@link HashCodeHelper#getHashCode(int, int, Object...)}. */
    @Test
    public void testGetHashCodeIntIntObject()
    {
        Object o1 = new HashCodeObject(67);
        Object o2 = null;
        Object o3 = new HashCodeObject(111);

        int startHash = 37;
        int prime = 31;
        Assert.assertEquals(startHash, HashCodeHelper.getHashCode(startHash, prime));
        Assert.assertEquals(1214, HashCodeHelper.getHashCode(startHash, prime, o1));
        Assert.assertEquals(1147, HashCodeHelper.getHashCode(startHash, prime, o2));
        Assert.assertEquals(37634, HashCodeHelper.getHashCode(startHash, prime, o1, o2));
        Assert.assertEquals(1166765, HashCodeHelper.getHashCode(startHash, prime, o1, o2, o3));
    }

    /**
     * Test for {@link HashCodeHelper#getHashCodeNoNulls(int, int, Object...)}.
     */
    @Test
    public void testGetHashCodeNoNulls()
    {
        Object o1 = new HashCodeObject(67);
        Object o2 = new HashCodeObject(0);
        Object o3 = new HashCodeObject(111);

        int startHash = 37;
        int prime = 31;
        Assert.assertEquals(startHash, HashCodeHelper.getHashCode(startHash, prime));
        Assert.assertEquals(1214, HashCodeHelper.getHashCode(startHash, prime, o1));
        Assert.assertEquals(1147, HashCodeHelper.getHashCode(startHash, prime, o2));
        Assert.assertEquals(37634, HashCodeHelper.getHashCode(startHash, prime, o1, o2));
        Assert.assertEquals(1166765, HashCodeHelper.getHashCode(startHash, prime, o1, o2, o3));
    }

    /**
     * Test for {@link HashCodeHelper#getHashCodeNoNulls(int, int, Object...)}.
     */
    @Test(expected = NullPointerException.class)
    public void testGetHashCodeNoNullsWithNull()
    {
        int startHash = 37;
        int prime = 31;
        HashCodeHelper.getHashCodeNoNulls(startHash, prime, (Object)null);
    }

    /** Class that allows specifying a hash code. */
    private static class HashCodeObject
    {
        /** The specified hash code. */
        private final int myHashCode;

        /**
         * Constructor.
         *
         * @param hashCode The hash code.
         */
        public HashCodeObject(int hashCode)
        {
            myHashCode = hashCode;
        }

        @Override
        public int hashCode()
        {
            return myHashCode;
        }
    }
}
