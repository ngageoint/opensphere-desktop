package io.opensphere.core.units.length;

import java.util.Arrays;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;

import io.opensphere.core.units.InvalidUnitsException;
import io.opensphere.core.units.UnitsProvider;
import io.opensphere.core.util.collections.New;
import org.junit.Assert;

/**
 * Test for {@link LengthUnitsProvider}.
 */
public class LengthUnitsProviderTest
{
    /**
     * Test for {@link LengthUnitsProvider#addUnits(Class)} and
     * {@link LengthUnitsProvider#removeUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testAddAndRemoveLengthType() throws InvalidUnitsException
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        @SuppressWarnings("unchecked")
        UnitsProvider.UnitsChangeListener<Length> listener = EasyMock.createStrictMock(UnitsProvider.UnitsChangeListener.class);
        EasyMock.replay(listener);
        units.addListener(listener);
        units.addUnits(AutoscaleMetric.class);
        units.addUnits(AutoscaleImperial.class);
        units.addUnits(AutoscaleNautical.class);
        // Listener should not be called.
        units.addUnits(Meters.class);

        EasyMock.reset(listener);
        Collection<Class<? extends Length>> expected = New.collection();
        expected.add(AutoscaleMetric.class);
        expected.add(AutoscaleImperial.class);
        expected.add(AutoscaleNautical.class);
        expected.add(Feet.class);
        expected.add(Inches.class);
        expected.add(Meters.class);
        expected.add(Kilometers.class);
        expected.add(NauticalMiles.class);
        expected.add(StatuteMiles.class);
        listener.availableUnitsChanged(Length.class, expected);
        EasyMock.replay(listener);
        units.removeUnits(Yards.class);
        EasyMock.verify(listener);

        EasyMock.reset(listener);
        expected.add(Yards.class);
        listener.availableUnitsChanged(Length.class, expected);
        EasyMock.replay(listener);
        units.addUnits(Yards.class);
        EasyMock.verify(listener);
    }

    /**
     * Test for {@link LengthUnitsProvider#addUnits(Class)} with an invalid
     * length type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidLengthType1() throws InvalidUnitsException
    {
        new LengthUnitsProvider().addUnits(InvalidLength1.class);
    }

    /**
     * Test for {@link LengthUnitsProvider#addUnits(Class)} with an invalid
     * length type.
     *
     * @throws InvalidUnitsException If the test succeeds.
     */
    @Test(expected = InvalidUnitsException.class)
    public void testAddInvalidLengthType2() throws InvalidUnitsException
    {
        new LengthUnitsProvider().addUnits(InvalidLength2.class);
    }

    /**
     * Test for {@link LengthUnitsProvider#convert(Class, Length)}.
     */
    @Test
    public void testConvert()
    {
        NauticalMiles oneNauticalMile = new NauticalMiles(1.);
        Assert.assertSame(oneNauticalMile, new LengthUnitsProvider().convert(NauticalMiles.class, oneNauticalMile));
        Assert.assertEquals(oneNauticalMile, new LengthUnitsProvider().convert(NauticalMiles.class, new Meters(1852.)));
    }

    /**
     * Test for
     * {@link LengthUnitsProvider#fromMagnitudeAndSelectionLabel(Number, String)}
     * .
     */
    @Test
    public void testFromMagnitudeAndSelectionLabel()
    {
        Assert.assertEquals(new NauticalMiles(1.), new LengthUnitsProvider().fromMagnitudeAndSelectionLabel(Double.valueOf(1.),
                Length.getSelectionLabel(NauticalMiles.class)));
    }

    /**
     * Test for {@link LengthUnitsProvider#fromUnitsAndMagnitude(Class, Number)}
     * .
     */
    @Test
    public void testFromUnitsAndMagnitude()
    {
        Assert.assertEquals(new NauticalMiles(1.),
                new LengthUnitsProvider().fromUnitsAndMagnitude(NauticalMiles.class, Double.valueOf(1.)));
    }

    /**
     * Test for
     * {@link LengthUnitsProvider#getAvailableUnitsSelectionLabels(boolean)}.
     */
    @Test
    public void testGetAvailableUnitsSelectionLabels()
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        String[] expected1 = new String[] { Length.getSelectionLabel(AutoscaleMetric.class),
            Length.getSelectionLabel(AutoscaleImperial.class), Length.getSelectionLabel(AutoscaleNautical.class),
            Length.getSelectionLabel(Feet.class), Length.getSelectionLabel(Inches.class), Length.getSelectionLabel(Meters.class),
            Length.getSelectionLabel(Kilometers.class), Length.getSelectionLabel(NauticalMiles.class),
            Length.getSelectionLabel(StatuteMiles.class), Length.getSelectionLabel(Yards.class), };
        Assert.assertTrue(Arrays.equals(expected1, units.getAvailableUnitsSelectionLabels(true)));
        String[] expected2 = new String[] { Length.getSelectionLabel(Feet.class), Length.getSelectionLabel(Inches.class),
            Length.getSelectionLabel(Meters.class), Length.getSelectionLabel(Kilometers.class),
            Length.getSelectionLabel(NauticalMiles.class), Length.getSelectionLabel(StatuteMiles.class),
            Length.getSelectionLabel(Yards.class), };
        Assert.assertTrue(Arrays.equals(expected2, units.getAvailableUnitsSelectionLabels(false)));
    }

    /**
     * Test for {@link LengthUnitsProvider#getDisplayClass(Length)}.
     */
    @Test
    public void testGetDisplayClass()
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        Assert.assertEquals(Meters.class, units.getDisplayClass(new AutoscaleMetric(1.)));
        Assert.assertEquals(Kilometers.class, units.getDisplayClass(new AutoscaleMetric(50000.)));
    }

    /**
     * Test for {@link LengthUnitsProvider#getFixedScaleUnits(Class, Length)}.
     */
    @Test
    public void testGetFixedScaleUnits()
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        Assert.assertEquals(Meters.class, units.getFixedScaleUnits(AutoscaleMetric.class, Meters.ONE));
        Assert.assertEquals(Kilometers.class, units.getFixedScaleUnits(AutoscaleMetric.class, new Meters(50000.)));
    }

    /**
     * Test for {@link LengthUnitsProvider#getAvailableUnits(boolean)}.
     */
    @Test
    public void testGetLengthTypes()
    {
        Collection<Class<? extends Length>> expected = New.collection();
        expected.add(AutoscaleMetric.class);
        expected.add(AutoscaleImperial.class);
        expected.add(AutoscaleNautical.class);
        expected.add(Feet.class);
        expected.add(Inches.class);
        expected.add(Meters.class);
        expected.add(Kilometers.class);
        expected.add(NauticalMiles.class);
        expected.add(StatuteMiles.class);
        expected.add(Yards.class);
        Assert.assertEquals(expected, new LengthUnitsProvider().getAvailableUnits(true));

        expected.remove(AutoscaleMetric.class);
        expected.remove(AutoscaleImperial.class);
        expected.remove(AutoscaleNautical.class);
        Assert.assertEquals(expected, new LengthUnitsProvider().getAvailableUnits(false));
    }

    /**
     * Test for {@link LengthUnitsProvider#getUnitsWithLongLabel(String)}.
     */
    @Test
    public void testGetLengthWithLongLabel()
    {
        Assert.assertEquals(Feet.class, new LengthUnitsProvider().getUnitsWithLongLabel(Feet.FEET_LONG_LABEL1));
        Assert.assertEquals(Feet.class, new LengthUnitsProvider().getUnitsWithLongLabel(Feet.FEET_LONG_LABEL2));
        Assert.assertEquals(Inches.class, new LengthUnitsProvider().getUnitsWithLongLabel(Inches.INCHES_LONG_LABEL));
        Assert.assertEquals(Meters.class, new LengthUnitsProvider().getUnitsWithLongLabel(Meters.METERS_LONG_LABEL));
        Assert.assertEquals(Kilometers.class, new LengthUnitsProvider().getUnitsWithLongLabel(Kilometers.KILOMETERS_LONG_LABEL));
        Assert.assertEquals(NauticalMiles.class, new LengthUnitsProvider().getUnitsWithLongLabel(NauticalMiles.NM_LONG_LABEL));
        Assert.assertEquals(StatuteMiles.class, new LengthUnitsProvider().getUnitsWithLongLabel(StatuteMiles.MILES_LONG_LABEL));
        Assert.assertEquals(Yards.class, new LengthUnitsProvider().getUnitsWithLongLabel(Yards.YARDS_LONG_LABEL));
    }

    /**
     * Test for {@link LengthUnitsProvider#getUnitsWithSelectionLabel(String)}.
     */
    @Test
    public void testGetLengthWithSelectionLabel()
    {
        Assert.assertEquals(Feet.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(Feet.class)));
        Assert.assertEquals(Inches.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(Inches.class)));
        Assert.assertEquals(Meters.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(Meters.class)));
        Assert.assertEquals(Kilometers.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(Kilometers.class)));
        Assert.assertEquals(NauticalMiles.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(NauticalMiles.class)));
        Assert.assertEquals(StatuteMiles.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(StatuteMiles.class)));
        Assert.assertEquals(Yards.class,
                new LengthUnitsProvider().getUnitsWithSelectionLabel(Length.getSelectionLabel(Yards.class)));
    }

    /**
     * Test for {@link LengthUnitsProvider#getUnitsWithShortLabel(String)}.
     */
    @Test
    public void testGetLengthWithShortLabel()
    {
        Assert.assertEquals(Feet.class, new LengthUnitsProvider().getUnitsWithShortLabel(Feet.FEET_SHORT_LABEL));
        Assert.assertEquals(Inches.class, new LengthUnitsProvider().getUnitsWithShortLabel(Inches.INCHES_SHORT_LABEL));
        Assert.assertEquals(Meters.class, new LengthUnitsProvider().getUnitsWithShortLabel(Meters.METERS_SHORT_LABEL));
        Assert.assertEquals(Kilometers.class,
                new LengthUnitsProvider().getUnitsWithShortLabel(Kilometers.KILOMETERS_SHORT_LABEL));
        Assert.assertEquals(NauticalMiles.class, new LengthUnitsProvider().getUnitsWithShortLabel(NauticalMiles.NM_SHORT_LABEL));
        Assert.assertEquals(StatuteMiles.class, new LengthUnitsProvider().getUnitsWithShortLabel(StatuteMiles.MILES_SHORT_LABEL));
        Assert.assertEquals(Yards.class, new LengthUnitsProvider().getUnitsWithShortLabel(Yards.YARDS_SHORT_LABEL));
    }

    /**
     * Test for {@link LengthUnitsProvider#getPreferredFixedScaleUnits(Length)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testGetPreferredFixedScaleUnits() throws InvalidUnitsException
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        units.setPreferredUnits(Meters.class);
        Assert.assertEquals(Meters.class, units.getPreferredFixedScaleUnits(Meters.ONE));
        units.setPreferredUnits(AutoscaleMetric.class);
        Assert.assertEquals(Meters.class, units.getPreferredFixedScaleUnits(Meters.ONE));
        Assert.assertEquals(Kilometers.class, units.getPreferredFixedScaleUnits(new Meters(50000.)));
    }

    /**
     * Test for {@link LengthUnitsProvider#setPreferredUnits(Class)}.
     *
     * @throws InvalidUnitsException If there is a test failure.
     */
    @Test
    public void testSetPreferredUnits() throws InvalidUnitsException
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        units.setPreferredUnits(Meters.class);
        @SuppressWarnings("unchecked")
        UnitsProvider.UnitsChangeListener<Length> listener = EasyMock.createStrictMock(UnitsProvider.UnitsChangeListener.class);
        EasyMock.replay(listener);
        units.addListener(listener);
        // Listener should not be called.
        units.setPreferredUnits(Meters.class);

        EasyMock.reset(listener);
        listener.preferredUnitsChanged(Feet.class);
        EasyMock.replay(listener);
        units.setPreferredUnits(Feet.class);
        EasyMock.verify(listener);
    }

    /**
     * Test for {@link LengthUnitsProvider#toShortLabelString(Length)} and
     * {@link LengthUnitsProvider#fromShortLabelString(String)}.
     */
    @Test
    public void testToFromShortLabelString()
    {
        LengthUnitsProvider units = new LengthUnitsProvider();
        Meters input = new Meters(827893745.34785384e45);
        String str = units.toShortLabelString(input);
        Length output = units.fromShortLabelString(str);
        Assert.assertEquals(input, output);
    }

    /** An invalid length class. */
    private static class InvalidLength1 extends Length
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param magnitude The magnitude.
         */
        public InvalidLength1(double magnitude)
        {
            super(magnitude);
        }

        @Override
        public String getLongLabel(boolean plural)
        {
            return null;
        }

        @Override
        public String getShortLabel(boolean plural)
        {
            return null;
        }
    }

    /** An invalid length class. */
    private static class InvalidLength2 extends Length
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param length The length.
         */
        public InvalidLength2(Length length)
        {
            super(length.inFeet());
        }

        @Override
        public String getLongLabel(boolean plural)
        {
            return null;
        }

        @Override
        public String getShortLabel(boolean plural)
        {
            return null;
        }
    }
}
