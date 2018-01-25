package io.opensphere.core.math;

/* <p><center>PUBLIC DOMAIN NOTICE</center></p><p> This program was prepared by
 * Los Alamos National Security, LLC at Los Alamos National Laboratory (LANL)
 * under contract No. DE-AC52-06NA25396 with the U.S. Department of Energy
 * (DOE). All rights in the program are reserved by the DOE and Los Alamos
 * National Security, LLC. Permission is granted to the public to copy and use
 * this software without charge, provided that this Notice and any statement of
 * authorship are reproduced on all copies. Neither the U.S. Government nor LANS
 * makes any warranty, express or implied, or assumes any liability or
 * responsibility for the use of this software. */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.junit.Test;

/**
 * <p>
 * Test values from <a href ="http://www.ngdc.noaa.gov/geomag/WMM/soft.shtml">
 * the National GeoPhysical Data Center.</a>. Click on the WMM2015testvalues.pdf
 * link.
 * </p>
 * <p>
 * You have to run this test twice. Once with the WMM.COF file present, and then
 * with it missing. Otherwise the setCoeff method is never tested.
 * </p>
 *
 * @version 1.0 Apr 14, 2006
 * @author John St. Ledger
 * @version 1.1 Jan 28, 2009
 *          <p>
 *          Added 2006 test values.
 *          </p>
 * @version 1.2 Jan 5, 2010
 *          <p>
 *          Updated with the test values for the 2010 WMM.COF coefficients. From
 *          page 18 of <i>The US/UK World Magnetic Model for 2010-2015,
 *          Preliminary online version containing final WMM2010 model
 *          coefficients</i>
 *          </p>
 * @version 1.3 Jan 15, 2015
 *          <p>
 *          Updated with the test values for the 2015 WMM.COF coefficients. From
 *          the test values WMM2015testvalues.pdf from the WMM web site.
 *          </p>
 *          * @version 1.4 May 26, 2015
 *          <p>
 *          Fixed the East-West, North-South bug discovered by Martin Frassl.
 *          </p>
 *
 */
public class TSAGeoMagTest
{
    /**
     * The model on which the test is being performed.
     */
    TSAGeoMag magModel = new TSAGeoMag();

    /**
     * Test method for
     * {@link TSAGeoMag#getDeclination(double, double, double, double)}.
     */
    @Test
    public final void getDeclination()
    {
        assertEquals(-3.85, magModel.getDeclination(80, 0, 2015, 0), 5.0E-03);
        assertEquals(0.57, magModel.getDeclination(0, 120, 2015, 0), 5.0E-03);
        assertEquals(69.81, magModel.getDeclination(-80, 240, 2015, 0), 5.0E-03);
        assertEquals(-4.27, magModel.getDeclination(80, 0, 2015, 100), 5.0E-03);
        assertEquals(0.56, magModel.getDeclination(0, 120, 2015, 100), 5.0E-03);
        assertEquals(69.22, magModel.getDeclination(-80, 240, 2015, 100), 5.0E-03);

        assertEquals(-2.75, magModel.getDeclination(80, 0, 2017.5, 0), 5.0E-03);
        assertEquals(0.32, magModel.getDeclination(0, 120, 2017.5, 0), 5.0E-03);
        assertEquals(69.58, magModel.getDeclination(-80, 240, 2017.5, 0), 5.0E-03);
        assertEquals(-3.17, magModel.getDeclination(80, 0, 2017.5, 100), 5.0E-03);
        assertEquals(0.32, magModel.getDeclination(0, 120, 2017.5, 100), 5.0E-03);
        assertEquals(69.00, magModel.getDeclination(-80, 240, 2017.5, 100), 5.0E-03);

        assertEquals(-2.75, magModel.getDeclination(80, 0), 5.0E-03);
        assertEquals(0.32, magModel.getDeclination(0, 120), 5.0E-03);
        assertEquals(69.58, magModel.getDeclination(-80, 240), 5.0E-03);
    }

    /**
     * Test method for
     * {@link TSAGeoMag#getDipAngle(double, double, double, double)}.
     */
    @Test
    public final void getDipAngle()
    {
        assertEquals(83.04, magModel.getDipAngle(80, 0, 2015, 0), 5E-03);
        assertEquals(-15.89, magModel.getDipAngle(0, 120, 2015, 0), 5.0E-03);
        assertEquals(-72.39, magModel.getDipAngle(-80, 240, 2015, 0), 5.0E-03);
        assertEquals(83.09, magModel.getDipAngle(80, 0, 2015, 100), 5.0E-03);
        assertEquals(-16.01, magModel.getDipAngle(0, 120, 2015, 100), 5.0E-03);
        assertEquals(-72.57, magModel.getDipAngle(-80, 240, 2015, 100), 5.0E-03);

        assertEquals(83.08, magModel.getDipAngle(80, 0, 2017.5, 0), 5.0E-03);
        assertEquals(-15.57, magModel.getDipAngle(0, 120, 2017.5, 0), 5.0E-03);
        assertEquals(-72.28, magModel.getDipAngle(-80, 240, 2017.5, 0), 5.0E-03);
        assertEquals(83.13, magModel.getDipAngle(80, 0, 2017.5, 100), 5.0E-03);
        assertEquals(-15.70, magModel.getDipAngle(0, 120, 2017.5, 100), 5.0E-03);
        assertEquals(-72.45, magModel.getDipAngle(-80, 240, 2017.5, 100), 5.0E-03);

        assertEquals(83.08, magModel.getDipAngle(80, 0), 5.0E-03);
        assertEquals(-15.57, magModel.getDipAngle(0, 120), 5.0E-03);
        assertEquals(-72.28, magModel.getDipAngle(-80, 240), 5.0E-03);
    }

    /**
     * Test method for
     * {@link TSAGeoMag#getHorizontalIntensity(double, double, double, double)}
     * in nT and {@link TSAGeoMag#getHorizontalIntensity(double, double)} in nT.
     */
    @Test
    public final void getHorizontalIntensity()
    {
        assertEquals(6642.1, magModel.getHorizontalIntensity(80, 0, 2015, 0), 5.0E-02);
        assertEquals(39520.2, magModel.getHorizontalIntensity(0, 120, 2015, 0), 5.0E-02);
        assertEquals(16793.5, magModel.getHorizontalIntensity(-80, 240, 2015, 0), 5.0E-02);
        assertEquals(6331.9, magModel.getHorizontalIntensity(80, 0, 2015, 100), 5.0E-02);
        assertEquals(37537.3, magModel.getHorizontalIntensity(0, 120, 2015, 100), 5.0E-02);
        assertEquals(15820.7, magModel.getHorizontalIntensity(-80, 240, 2015, 100), 5.0E-02);

        assertEquals(6607.0, magModel.getHorizontalIntensity(80, 0, 2017.5, 0), 5.0E-02);
        assertEquals(39572.0, magModel.getHorizontalIntensity(0, 120, 2017.5, 0), 5.0E-02);
        assertEquals(16839.1, magModel.getHorizontalIntensity(-80, 240, 2017.5, 0), 5.0E-02);
        assertEquals(6300.1, magModel.getHorizontalIntensity(80, 0, 2017.5, 100), 5.0E-02);
        assertEquals(37586.1, magModel.getHorizontalIntensity(0, 120, 2017.5, 100), 5.0E-02);
        assertEquals(15862.0, magModel.getHorizontalIntensity(-80, 240, 2017.5, 100), 5.0E-02);

        assertEquals(6607.0, magModel.getHorizontalIntensity(80, 0), 5.0E-02);
        assertEquals(39572.0, magModel.getHorizontalIntensity(0, 120), 5.0E-02);
        assertEquals(16839.1, magModel.getHorizontalIntensity(-80, 240), 5.0E-02);
    }

    /**
     * Test method for d3.env.TSAGeoMag.getEastIntensity() in nT.
     */
    @Test
    public final void getNorthIntensity()
    {
        assertEquals(6627.1, magModel.getNorthIntensity(80, 0, 2015, 0), 5.0E-02);
        assertEquals(39518.2, magModel.getNorthIntensity(0, 120, 2015, 0), 5.0E-02);
        assertEquals(5797.3, magModel.getNorthIntensity(-80, 240, 2015, 0), 5.0E-02);
        assertEquals(6314.3, magModel.getNorthIntensity(80, 0, 2015, 100), 5.0E-02);
        assertEquals(37535.6, magModel.getNorthIntensity(0, 120, 2015, 100), 5.0E-02);
        assertEquals(5613.1, magModel.getNorthIntensity(-80, 240, 2015, 100), 5.0E-02);

        assertEquals(6599.4, magModel.getNorthIntensity(80, 0, 2017.5, 0), 5.0E-02);
        assertEquals(39571.4, magModel.getNorthIntensity(0, 120, 2017.5, 0), 5.0E-02);
        assertEquals(5873.8, magModel.getNorthIntensity(-80, 240, 2017.5, 0), 5.0E-02);
        assertEquals(6290.5, magModel.getNorthIntensity(80, 0, 2017.5, 100), 5.0E-02);
        assertEquals(37585.5, magModel.getNorthIntensity(0, 120, 2017.5, 100), 5.0E-02);
        assertEquals(5683.5, magModel.getNorthIntensity(-80, 240, 2017.5, 100), 5.0E-02);

        assertEquals(6599.4, magModel.getNorthIntensity(80, 0), 5.0E-02);
        assertEquals(39571.4, magModel.getNorthIntensity(0, 120), 5.0E-02);
        assertEquals(5873.8, magModel.getNorthIntensity(-80, 240), 5.0E-02);
    }

    /**
     * Test method for d3.env.TSAGeoMag.getNorthIntensity() in nT.
     */
    @Test
    public final void getEastIntensity()
    {
        assertEquals(-445.9, magModel.getEastIntensity(80, 0, 2015, 0), 5.0E-02);
        assertEquals(392.9, magModel.getEastIntensity(0, 120, 2015, 0), 5.0E-02);
        assertEquals(15761.1, magModel.getEastIntensity(-80, 240, 2015, 0), 5.0E-02);
        assertEquals(-471.6, magModel.getEastIntensity(80, 0, 2015, 100), 5.0E-02);
        assertEquals(364.4, magModel.getEastIntensity(0, 120, 2015, 100), 5.0E-02);
        assertEquals(14791.5, magModel.getEastIntensity(-80, 240, 2015, 100), 5.0E-02);

        assertEquals(-317.1, magModel.getEastIntensity(80, 0, 2017.5, 0), 5.0E-02);
        assertEquals(222.5, magModel.getEastIntensity(0, 120, 2017.5, 0), 5.0E-02);
        assertEquals(15781.4, magModel.getEastIntensity(-80, 240, 2017.5, 0), 5.0E-02);
        assertEquals(-348.5, magModel.getEastIntensity(80, 0, 2017.5, 100), 5.0E-02);
        assertEquals(209.5, magModel.getEastIntensity(0, 120, 2017.5, 100), 5.0E-02);
        assertEquals(14808.8, magModel.getEastIntensity(-80, 240, 2017.5, 100), 5.0E-02);

        assertEquals(-317.1, magModel.getEastIntensity(80, 0), 5.0E-02);
        assertEquals(222.5, magModel.getEastIntensity(0, 120), 5.0E-02);
        assertEquals(15781.4, magModel.getEastIntensity(-80, 240), 5.0E-02);
    }

    /**
     * Test method for d3.env.TSAGeoMag.getVerticalIntensity().
     */
    @Test
    public final void getVerticalIntensity()
    {
        assertEquals(54432.3, magModel.getVerticalIntensity(80, 0, 2015, 0), 5.0E-02);
        assertEquals(-11252.4, magModel.getVerticalIntensity(0, 120, 2015, 0), 5.0E-02);
        assertEquals(-52919.1, magModel.getVerticalIntensity(-80, 240, 2015, 0), 5.0E-02);
        assertEquals(52269.8, magModel.getVerticalIntensity(80, 0, 2015, 100), 5.0E-02);
        assertEquals(-10773.4, magModel.getVerticalIntensity(0, 120, 2015, 100), 5.0E-02);
        assertEquals(-50378.6, magModel.getVerticalIntensity(-80, 240, 2015, 100), 5.0E-02);

        assertEquals(54459.2, magModel.getVerticalIntensity(80, 0, 2017.5, 0), 5.0E-02);
        assertEquals(-11030.1, magModel.getVerticalIntensity(0, 120, 2017.5, 0), 5.0E-02);
        assertEquals(-52687.9, magModel.getVerticalIntensity(-80, 240, 2017.5, 0), 5.0E-02);
        assertEquals(52292.7, magModel.getVerticalIntensity(80, 0, 2017.5, 100), 5.0E-02);
        assertEquals(-10564.2, magModel.getVerticalIntensity(0, 120, 2017.5, 100), 5.0E-02);
        assertEquals(-50163.0, magModel.getVerticalIntensity(-80, 240, 2017.5, 100), 5.0E-02);

        assertEquals(54459.2, magModel.getVerticalIntensity(80, 0), 5.0E-02);
        assertEquals(-11030.1, magModel.getVerticalIntensity(0, 120), 5.0E-02);
        assertEquals(-52687.9, magModel.getVerticalIntensity(-80, 240), 5.0E-02);
    }

    /**
     * Test method for d3.env.TSAGeoMag.getIntensity().
     */
    @Test
    public final void getIntensity()
    {
        assertEquals(54836.0, magModel.getIntensity(80, 0, 2015, 0), 5.0E-02);
        assertEquals(41090.9, magModel.getIntensity(0, 120, 2015, 0), 5.0E-02);
        assertEquals(55519.8, magModel.getIntensity(-80, 240, 2015, 0), 5.0E-02);
        assertEquals(52652.0, magModel.getIntensity(80, 0, 2015, 100), 5.0E-02);
        assertEquals(39052.7, magModel.getIntensity(0, 120, 2015, 100), 5.0E-02);
        assertEquals(52804.4, magModel.getIntensity(-80, 240, 2015, 100), 5.0E-02);

        assertEquals(54858.5, magModel.getIntensity(80, 0, 2017.5, 0), 5.0E-02);
        assertEquals(41080.5, magModel.getIntensity(0, 120, 2017.5, 0), 5.0E-02);
        assertEquals(55313.4, magModel.getIntensity(-80, 240, 2017.5, 0), 5.0E-02);
        assertEquals(52670.9, magModel.getIntensity(80, 0, 2017.5, 100), 5.0E-02);
        assertEquals(39042.5, magModel.getIntensity(0, 120, 2017.5, 100), 5.0E-02);
        assertEquals(52611.1, magModel.getIntensity(-80, 240, 2017.5, 100), 5.0E-02);

        assertEquals(54858.5, magModel.getIntensity(80, 0), 5.0E-02);
        assertEquals(41080.5, magModel.getIntensity(0, 120), 5.0E-02);
        assertEquals(55313.4, magModel.getIntensity(-80, 240), 5.0E-02);
        // assertEquals(52672.8, magModel.getIntensity(40, -105, 2014, 0),
        // 0.05);
        // assertEquals(52672.8, magModel.getIntensity(40, -105,
        // magModel.decimalYear(new GregorianCalendar(2014, 0, 0)), 0), 0.05);
        // assertEquals(52672.5, magModel.getIntensity(40, -105,
        // magModel.decimalYear(new GregorianCalendar(2014, 0, 1)), 0), 0.05);
    }

    /**
     * test method for {@link TSAGeoMag#decimalYear(GregorianCalendar)}.
     */
    @Test
    public final void decimalYear()
    {
        TSAGeoMag mag = new TSAGeoMag();

        GregorianCalendar cal = new GregorianCalendar(2010, 0, 0);
        assertEquals(2010.0, mag.decimalYear(cal), 0.0);

        GregorianCalendar cal2 = new GregorianCalendar(2012, 6, 1);
        assertTrue(cal2.isLeapYear(2012));
        assertEquals(2012.5, mag.decimalYear(cal2), 0.0);

        cal2 = new GregorianCalendar(2013, 3, 13);
        assertFalse(cal2.isLeapYear(2013));
        assertEquals(2013.282, mag.decimalYear(cal2), 0.0005);
    }
}
