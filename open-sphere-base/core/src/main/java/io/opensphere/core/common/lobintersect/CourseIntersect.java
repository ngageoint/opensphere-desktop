package io.opensphere.core.common.lobintersect;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.common.coordinate.math.strategy.Vincenty;

/**
 * Course Intersect - calculates the intersection point of two lines of bearing
 * on an oblate Earth.
 */
public class CourseIntersect
{

    public Vincenty vincenty = new Vincenty();

    public static double METERS_PER_NM = 1852D;

    private double signAzDiff(double az1, double az2)
    {
        double val = az1 - az2 + Math.PI;
        double val1 = val % (Math.PI * 2);
        double val2 = val1 - Math.PI;
        return val2;
    }

    private double findLinearRoot(List<Double> x, List<Double> err)
    {
        double val = 0;
        if (x.get(0) == x.get(1))
        {
            val = Double.NaN;
        }
        else if (err.get(0) == err.get(1))
        {
            if (Math.abs(err.get(0) - err.get(1)) < 1e-15)
            {
                val = x.get(0);
            }
            else
            {
                val = Double.NaN;
            }
        }
        else
        {
            // 11/11/11 to match jlees most recent
//             double m = (err.get(0)-err.get(1))/(x.get(0)-x.get(1));
//             double b = err.get(0) - m *x.get(0);
//             val = -b/m;
            val = -err.get(0) * (x.get(1) - x.get(0)) / (err.get(1) - err.get(0)) + x.get(0);
        }
        return val;
    }

    private void correctAz(double[] val1, double[] val2)
    {
        if (val1[0] < 0.0)
        {
            val1[0] += Math.PI * 2;
        }

        val2[0] += Math.PI;
        if (val2[0] < 0.0)
        {
            val2[0] += Math.PI * 2;
        }
    }

    /**
     *
     * @param pt1 - Lob1
     * @param pt2 - Lob2
     * @param intersect- Intersection of Lobs 1 & 2
     * @param az13 - azimuth from lob1 to intersection
     * @param az23 - azimuth from lob2 to intersection
     * @param retVals - array[0] = latitude of intersection array[1] = longitude
     *            of intersection array[2] = azimuth from intersection to lob1,
     *            array[3] = distance from intersection to lob1, array[4] =
     *            azimuth from intersection to lob2, array[5] = distance from
     *            intersection to lob2,
     * @return - true if calculation was successful.
     */
    public boolean calculate(LobPoint point1, LobPoint point2, double tol, List<Double> retVals)
    {

        double az31 = 0;
        double az32 = 0;
        double dist13 = 0;
        double dist23 = 0;

        double pt1Lat = point1.lat;
        double pt2Lat = point2.lat;

        double pt1Lon = point1.getLon();
        double pt2Lon = point2.getLon();
        double az13 = point1.getBearing();
        double az23 = point2.getBearing();
        double dAz13 = az13;
        double dAz23 = az23;

        double[] vals = vincenty.calculateDistanceBetweenPointsRadians(pt1Lat, pt1Lon, pt2Lat, pt2Lon);
        double dist = vals[0];

        double[] alpha1 = { vals[1] };
        double[] alpha2 = { vals[2] };
        correctAz(alpha1, alpha2);

        double angle1 = Math.abs(signAzDiff(dAz13, alpha1[0]));
        double angle2 = Math.abs(signAzDiff(alpha2[0], dAz23));

        if (angle1 < 0.0 && angle2 < 0.0)
        {
            angle1 = -angle1;
            angle2 = -angle2;
        }

        if (Math.abs(Math.sin(angle1)) < Double.MIN_VALUE && Math.abs(Math.sin(angle1)) < Double.MIN_VALUE)
        {
            return false;
        }

        double sphereRadius = Math.PI * 2 * Vincenty.a; // Circumference at
                                                        // equator
        double cosA = Math.cos(angle1);
        double sinA = Math.sin(angle1);
        double cosB = Math.cos(angle2);
        double sinB = Math.sin(angle2);

        double c = Math.acos(-cosA * cosB + sinA * sinB * Math.cos(dist / sphereRadius));
        double cosC = Math.cos(c);
        double sinC = Math.sin(c);
        double bs = sphereRadius * Math.acos((cosB + cosA * cosC) / (sinA * sinC));

        if (Double.isNaN(bs))
        {
            return false;
        }
        // Replace Vincenty with LobIntersect for initial point estimate
        LatLon estimate = LobIntersection.getLobIntersection(point1, point2);
        if (estimate == null)
        {
            return false;
        }
        double[] llIntersect = { estimate.getLat(), estimate.getLon() };

        // vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, bs,
        // dAz13);
        // double[] llIntersect = {vals[0], vals[1]};

        vals = vincenty.calculateDistanceBetweenPointsRadians(pt1Lat, pt1Lon, llIntersect[0], llIntersect[1]);
        dist13 = vals[0];

        double[] llInv = { llIntersect[0], llIntersect[1] };
        llInv[0] = -llInv[0];
        llInv[1] = llInv[1] - Math.PI;
        vals = vincenty.calculateDistanceBetweenPointsRadians(pt1Lat, pt1Lon, llInv[0], llInv[1]);
        double resultDist = vals[0];
        alpha1[0] = vals[1];
        alpha2[0] = vals[2];
        correctAz(alpha1, alpha2);

        if (dist13 > resultDist)
        {
            llIntersect = llInv;
            dist13 = resultDist;
            az31 = alpha2[0];
            dAz13 = dAz13 + Math.PI;
            dAz23 = dAz23 + Math.PI;
        }

        vals = vincenty.calculateDistanceBetweenPointsRadians(pt2Lat, pt2Lon, llIntersect[0], llIntersect[1]);
        dist23 = vals[0];

        if (dist13 < METERS_PER_NM)
        {
            vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, METERS_PER_NM, dAz13 + Math.PI);
            pt1Lat = vals[0];
            pt1Lon = vals[1];
            vals = vincenty.calculateDistanceBetweenPointsRadians(pt1Lat, pt1Lon, llIntersect[0], llIntersect[1]);
            alpha1[0] = vals[1];
            alpha2[0] = vals[2];
            correctAz(alpha1, alpha2);
            dAz13 = alpha1[0];
        }

        if (dist23 < METERS_PER_NM)
        {
            vals = vincenty.calculateDestinationRadians(pt2Lat, pt2Lon, METERS_PER_NM, dAz23 + Math.PI);
            pt2Lat = vals[0];
            pt2Lon = vals[1];
            vals = vincenty.calculateDistanceBetweenPointsRadians(pt2Lat, pt2Lon, llIntersect[0], llIntersect[1]);
            alpha1[0] = vals[1];
            alpha2[0] = vals[2];
            correctAz(alpha1, alpha2);
            dAz23 = alpha1[0];
        }

        boolean bSwapped = false;
        if (dist23 < dist13)
        {
            double tmp = pt1Lat;
            pt1Lat = pt2Lat;
            pt2Lat = tmp;
            tmp = pt1Lon;
            pt1Lon = pt2Lon;
            pt2Lon = tmp;
            tmp = dAz13;
            dAz13 = dAz23;
            dAz23 = tmp;
            dist13 = dist23;
            bSwapped = true;
        }

        List<Double> distArray = new ArrayList<>(2);
        List<Double> errArray = new ArrayList<>(2);
        distArray.add(dist13);

        vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, dist13, dAz13);
        llIntersect[0] = vals[0];
        llIntersect[1] = vals[1];

        vals = vincenty.calculateDistanceBetweenPointsRadians(pt2Lat, pt2Lon, llIntersect[0], llIntersect[1]);
        alpha1[0] = vals[1];
        alpha2[0] = vals[2];
        correctAz(alpha1, alpha2);
        double aacrs23 = alpha1[0];

        errArray.add(signAzDiff(aacrs23, dAz23));
        distArray.add(1.01 * dist13);

        vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, distArray.get(1), dAz13);
        llIntersect[0] = vals[0];
        llIntersect[1] = vals[1];

        vals = vincenty.calculateDistanceBetweenPointsRadians(pt2Lat, pt2Lon, llIntersect[0], llIntersect[1]);
        alpha1[0] = vals[1];
        alpha2[0] = vals[2];
        correctAz(alpha1, alpha2);

        aacrs23 = alpha1[0];
        errArray.add(signAzDiff(aacrs23, dAz23));

        int k = 0;
        double dErr = 0;
        int nMaxCount = 50;
        while (k == 0 || dErr > tol && k <= nMaxCount)
        {
            dist13 = findLinearRoot(distArray, errArray);
            vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, dist13, dAz13);
            double[] ll = { vals[0], vals[1] };
            vals = vincenty.calculateDistanceBetweenPointsRadians(pt2Lat, pt2Lon, ll[0], ll[1]);
            alpha1[0] = vals[1];
            alpha2[0] = vals[2];
            correctAz(alpha1, alpha2);
            aacrs23 = alpha1[0];

            vals = vincenty.calculateDistanceBetweenPointsRadians(ll[0], ll[1], llIntersect[0], llIntersect[1]);
            dErr = vals[0];
            distArray.set(0, distArray.get(1));
            distArray.set(1, dist13);
            errArray.set(0, errArray.get(1));
            errArray.set(1, signAzDiff(aacrs23, dAz23));
            k++;
            llIntersect = ll;
        }

        if (k > nMaxCount && dErr > 1e-8)
        {
            return false;
        }

        if (bSwapped)
        {
            double tmp = pt1Lat;
            pt1Lat = pt2Lat;
            pt2Lat = tmp;
            tmp = pt1Lon;
            pt1Lon = pt2Lon;
            pt2Lon = tmp;
            tmp = dAz13;
            dAz13 = dAz23;
            dAz23 = tmp;
            dist13 = dist23;
        }

        vals = vincenty.calculateDistanceBetweenPointsRadians(llIntersect[0], llIntersect[1], pt1Lat, pt1Lon);
        dist13 = vals[0];
        alpha1[0] = vals[1];
        alpha2[0] = vals[2];
        correctAz(alpha1, alpha2);
        az31 = alpha1[0];

        vals = vincenty.calculateDistanceBetweenPointsRadians(llIntersect[0], llIntersect[1], pt2Lat, pt2Lon);
        dist23 = vals[0];
        alpha1[0] = vals[1];
        alpha2[0] = vals[2];
        correctAz(alpha1, alpha2);
        az32 = alpha1[0];

        /* Verify that that the results plugged back into direct vincenty result
         * in the calculated intersection. This will eliminate false
         * intersections resulting from extending lobs around the world. */
        vals = vincenty.calculateDestinationRadians(pt1Lat, pt1Lon, dist13, dAz13);
        boolean pt1Valid = Math.abs(llIntersect[0] - vals[0]) < tol && Math.abs(llIntersect[1] - vals[1]) < tol;

        vals = vincenty.calculateDestinationRadians(pt2Lat, pt2Lon, dist23, dAz23);
        boolean pt2Valid = Math.abs(llIntersect[0] - vals[0]) < tol && Math.abs(llIntersect[1] - vals[1]) < tol;

        if (pt1Valid && pt2Valid)
        {
            retVals.add(llIntersect[0]);
            retVals.add(llIntersect[1]);
            retVals.add(az31);
            retVals.add(dist13);
            retVals.add(az32);
            retVals.add(dist23);
        }

        return !retVals.isEmpty();
    }
}