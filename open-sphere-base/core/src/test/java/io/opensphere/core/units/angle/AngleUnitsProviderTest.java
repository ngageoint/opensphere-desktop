package io.opensphere.core.units.angle;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.units.UnitsProvider.UnitsChangeListener;

/**
 * Test for {@link AngleUnitsProvider}.
 */
public class AngleUnitsProviderTest
{
    /**
     * Simple reference.
     *
     * @param <T> the generic value in the reference.
     */
    private static class Ref<T>
    {
        /** Nested generic value. */
        public T val;
    }

    /**
     * Test for {@link AngleUnitsProvider#addUnits(Class)} and
     * {@link AngleUnitsProvider#removeUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testAddAndRemoveAngleType() throws InvalidUnitsException
    {
        Ref<Collection<Class<? extends Angle>>> r = new Ref<>();
        UnitsChangeListener<Angle> listener = new UnitsChangeListener<Angle>()
        {
            @Override
            public void availableUnitsChanged(Class<Angle> superType, Collection<Class<? extends Angle>> newTypes)
            {
                r.val = new LinkedList<>(newTypes);
            }

            @Override
            public void preferredUnitsChanged(Class<? extends Angle> type)
            {
            }
        };

        AngleUnitsProvider units = new AngleUnitsProvider();
        units.addListener(listener);
        // Listener should not be called.
        units.addUnits(DecimalDegrees.class);
        Assert.assertEquals(null, r.val);

        Collection<Class<? extends Angle>> expected = new LinkedList<>();
        expected.add(DegDecimalMin.class);
        listener.availableUnitsChanged(Angle.class, expected);
        Assert.assertEquals(expected, r.val);
        expected.add(DecimalDegrees.class);
        units.removeUnits(DegreesMinutesSeconds.class);
        Assert.assertEquals(expected, r.val);

        expected.add(DegreesMinutesSeconds.class);
        listener.availableUnitsChanged(Angle.class, expected);
        Assert.assertEquals(expected, r.val);
        units.addUnits(DegreesMinutesSeconds.class);
        Assert.assertEquals(expected, r.val);
    }

    /**
     * Test for {@link AngleUnitsProvider#addUnits(Class)} with an invalid angle
     * type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidAngleType1() throws InvalidUnitsException
    {
        new AngleUnitsProvider().addUnits(InvalidAngle1.class);
    }

    /**
     * Test for {@link AngleUnitsProvider#addUnits(Class)} with an invalid angle
     * type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidAngleType2() throws InvalidUnitsException
    {
        new AngleUnitsProvider().addUnits(InvalidAngle2.class);
    }

    /**
     * Test for {@link AngleUnitsProvider#convert(Class, Angle)}.
     */
    @Test
    public void testConvert()
    {
        DecimalDegrees dd = new DecimalDegrees(34.5451235);
        Assert.assertSame(dd, new AngleUnitsProvider().convert(DecimalDegrees.class, dd));
        Assert.assertEquals(dd, new AngleUnitsProvider().convert(DecimalDegrees.class, new DegreesMinutesSeconds(34.5451235)));
    }

    /**
     * Test for {@link AngleUnitsProvider#fromUnitsAndMagnitude(Class, Number)}.
     */
    @Test
    public void testFromTypeAndMagnitude()
    {
        Assert.assertEquals(new DecimalDegrees(1.),
                new AngleUnitsProvider().fromUnitsAndMagnitude(DecimalDegrees.class, Double.valueOf(1.)));
    }

    /**
     * Test for {@link AngleUnitsProvider#getUnitsWithLongLabel(String)}.
     */
    @Test
    public void testGetAngleWithLongLabel()
    {
        Assert.assertEquals(DecimalDegrees.class,
                new AngleUnitsProvider().getUnitsWithLongLabel(DecimalDegrees.DEGREES_LONG_LABEL));
        Assert.assertEquals(DegreesMinutesSeconds.class,
                new AngleUnitsProvider().getUnitsWithLongLabel(DegreesMinutesSeconds.DMS_LONG_LABEL));
    }

    /**
     * Test for {@link AngleUnitsProvider#getUnitsWithShortLabel(String)}.
     */
    @Test
    public void testGetAngleWithShortLabel()
    {
        Assert.assertEquals(DecimalDegrees.class,
                new AngleUnitsProvider().getUnitsWithShortLabel(DecimalDegrees.DEGREES_SHORT_LABEL));
        Assert.assertEquals(DegreesMinutesSeconds.class,
                new AngleUnitsProvider().getUnitsWithShortLabel(DegreesMinutesSeconds.DMS_SHORT_LABEL));
    }

    /**
     * Test for {@link AngleUnitsProvider#setPreferredUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testSetPreferredUnits() throws InvalidUnitsException
    {
        AngleUnitsProvider units = new AngleUnitsProvider();
        units.setPreferredUnits(DecimalDegrees.class);
        @SuppressWarnings("unchecked")
        UnitsProvider.UnitsChangeListener<Angle> listener = EasyMock.createStrictMock(UnitsProvider.UnitsChangeListener.class);
        EasyMock.replay(listener);
        units.addListener(listener);
        // Listener should not be called.
        units.setPreferredUnits(DecimalDegrees.class);

        EasyMock.reset(listener);
        listener.preferredUnitsChanged(DegreesMinutesSeconds.class);
        EasyMock.replay(listener);
        units.setPreferredUnits(DegreesMinutesSeconds.class);
        EasyMock.verify(listener);
    }

    /**
     * Test for {@link AngleUnitsProvider#toShortLabelString(Angle)} and
     * {@link AngleUnitsProvider#fromShortLabelString(String)}.
     */
    @Test
    public void testToFromShortLabelString()
    {
        AngleUnitsProvider units = new AngleUnitsProvider();
        DecimalDegrees input = new DecimalDegrees(34.545124);
        String str = units.toShortLabelString(input);
        Angle output = units.fromShortLabelString(str);
        Assert.assertEquals(input, output);
    }

    /** Adapter for Angle class. */
    private abstract static class AngleAdapter extends Angle
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The magnitude.
         */
        public AngleAdapter(double magnitude)
        {
            super(magnitude);
        }

        @Override
        public String getLongLabel()
        {
            return null;
        }

        @Override
        public String getSelectionLabel()
        {
            return null;
        }

        @Override
        public String getShortLabel()
        {
            return null;
        }

        @Override
        public String toShortLabelString()
        {
            return null;
        }

        @Override
        public String toShortLabelString(char positive, char negative)
        {
            return null;
        }

        @Override
        public String toShortLabelString(int width, int precision)
        {
            return null;
        }

        @Override
        public String toShortLabelString(int width, int precision, char positive, char negative)
        {
            return null;
        }
    }

    /** An invalid angle class. */
    private static class InvalidAngle1 extends AngleAdapter
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The magnitude.
         */
        public InvalidAngle1(double magnitude)
        {
            super(magnitude);
        }
    }

    /** An invalid angle class. */
    private static class InvalidAngle2 extends AngleAdapter
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param angle The angle.
         */
        public InvalidAngle2(Angle angle)
        {
            super(angle.getMagnitude());
        }
    }
}
