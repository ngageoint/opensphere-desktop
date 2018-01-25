package io.opensphere.core.util.lang;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;

import org.junit.Assert;

/** Tests for {@link Serialization}. */
public class SerializationTest
{
    /**
     * Test for serialization and de-serialization utilities.
     *
     * @throws IOException Indicates test failure.
     * @throws ClassNotFoundException Indicates test failure.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        Serializable expected = null;
        Serializable actual = Serialization.deserialize(Serialization.serialize(expected));
        Assert.assertNull(actual);

        expected = "Test string";
        actual = Serialization.deserialize(Serialization.serialize(expected));
        Assert.assertEquals(expected, actual);
    }
}
