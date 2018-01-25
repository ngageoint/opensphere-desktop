package io.opensphere.core.util.lang.enums;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

/** Test {@link EnumUtilities}. */
public class EnumUtilitiesTest
{
    /** Test {@link EnumUtilities#fromString(Class, String)}. */
    @Test
    public void testFromStringClass()
    {
        Assert.assertEquals(TestEnum.ALPHA, EnumUtilities.fromString(TestEnum.class, "alpha"));
        Assert.assertEquals(TestEnum.BRAVO, EnumUtilities.fromString(TestEnum.class, "bravo"));
        Assert.assertEquals(TestEnum.CHARLIE, EnumUtilities.fromString(TestEnum.class, "charlie"));
        Assert.assertNull(EnumUtilities.fromString(TestEnum.class, "bad"));
    }

    /**
     * A predicate which compares the last letter of the given value to the last
     * letter of the toString() of the enum values.
     */
    public static class TestPredicate implements Predicate<TestEnum>
    {
        /** The value used by this predicate. */
        private final String myValue;

        /**
         * Constructor.
         *
         * @param value The value used by this predicate.
         */
        public TestPredicate(String value)
        {
            myValue = value;
        }

        @Override
        public boolean test(TestEnum input)
        {
            String last = myValue.substring(myValue.length() - 1, myValue.length());
            String inputString = input.toString();
            String oLast = inputString.substring(inputString.length() - 1, inputString.length());
            return last.equals(oLast);
        }
    }

    /** Test enum. */
    private enum TestEnum
    {
        /** Enum value. */
        ALPHA,

        /** Enum value. */
        BRAVO,

        /** Enum value. */
        CHARLIE,

        ;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }
}
