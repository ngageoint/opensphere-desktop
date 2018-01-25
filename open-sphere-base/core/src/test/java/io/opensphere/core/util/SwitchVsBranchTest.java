package io.opensphere.core.util;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * A test that verifies that if/else branching is faster than using a switch for
 * enums.
 */
public class SwitchVsBranchTest
{
    /**
     * Perform the test.
     */
    @Test
    public void test()
    {
        // Try up to 5 times for some consistency.
        int failCount = 0;
        for (int j = 0; j < 5; j++)
        {
            int count = 5000000;
            int value = 0;
            long t0;
            long t1;
            Type[] types = new Type[count];

            Random rand = new Random();
            for (int i = 0; i < count; ++i)
            {
                types[i] = Type.values()[rand.nextInt(Type.values().length)];
            }

            t0 = System.nanoTime();
            value = useSwitch(types, value);
            t1 = System.nanoTime();
            long switchTime = t1 - t0;

            t0 = System.nanoTime();
            value = useBranch(types, value);
            t1 = System.nanoTime();
            long branchTime = t1 - t0;

            if (switchTime < branchTime)
            {
                failCount++;
            }
            else
            {
                break;
            }
        }

        Assert.assertTrue(failCount < 5);
    }

    /**
     * Branch on the types to add values to the input value.
     *
     * @param types The type array.
     * @param in The input value.
     * @return The return value.
     */
    protected int useBranch(Type[] types, int in)
    {
        int value = in;
        for (int i = 0; i < types.length; ++i)
        {
            if (types[i] == Type.TYPE1)
            {
                value += 1;
            }
            else if (types[i] == Type.TYPE2)
            {
                value += 2;
            }
            else if (types[i] == Type.TYPE3)
            {
                value += 3;
            }
            else if (types[i] == Type.TYPE4)
            {
                value += 4;
            }
            else if (types[i] == Type.TYPE5)
            {
                value += 5;
            }
            else if (types[i] == Type.TYPE6)
            {
                value += 6;
            }
            else if (types[i] == Type.TYPE7)
            {
                value += 7;
            }
            else if (types[i] == Type.TYPE8)
            {
                value += 8;
            }
            else if (types[i] == Type.TYPE9)
            {
                value += 9;
            }
            else if (types[i] == Type.TYPE10)
            {
                value += 10;
            }
            else
            {
                Assert.fail("Unexpected enum");
            }
        }
        return value;
    }

    /**
     * Switch on the types to add values to the input value.
     *
     * @param types The type array.
     * @param in The input value.
     *
     * @return The return value.
     */
    protected int useSwitch(Type[] types, int in)
    {
        int value = in;
        for (int i = 0; i < types.length; ++i)
        {
            switch (types[i])
            {
                case TYPE1:
                    value += 1;
                    break;
                case TYPE2:
                    value += 2;
                    break;
                case TYPE3:
                    value += 3;
                    break;
                case TYPE4:
                    value += 4;
                    break;
                case TYPE5:
                    value += 5;
                    break;
                case TYPE6:
                    value += 6;
                    break;
                case TYPE7:
                    value += 7;
                    break;
                case TYPE8:
                    value += 8;
                    break;
                case TYPE9:
                    value += 9;
                    break;
                case TYPE10:
                    value += 10;
                    break;
                default:
                    Assert.fail("Unexpected enum");
                    break;
            }
        }
        return value;
    }

    /** An enum for the test. */
    private enum Type
    {
        /** A type. */
        TYPE1,

        /** A type. */
        TYPE2,

        /** A type. */
        TYPE3,

        /** A type. */
        TYPE4,

        /** A type. */
        TYPE5,

        /** A type. */
        TYPE6,

        /** A type. */
        TYPE7,

        /** A type. */
        TYPE8,

        /** A type. */
        TYPE9,

        /** A type. */
        TYPE10,
    }
}
