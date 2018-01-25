package io.opensphere.core.common.geom;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A class that performs polygon intersecting checking. An initial polygon or
 * list of polygon are provided to the class. Fore each of these base polygons a
 * report is created to detail how many and which polygons are intersecting when
 * checked against another individual or list. This report list will contain
 * references to the original polygons and a list of references to those that
 * intersect.
 *
 * This calculator can be reused only if the initial base poly or base poly list
 * remain the same, but new reports will be generated.
 */
public class PolygonIntersectionCalculator
{

    ArrayList<PolygonHD> myBasePolys;

    ArrayList<PolygonIntersectionReport> myIntersectionReports;

    protected PolygonIntersectionCalculator()
    {
        myBasePolys = new ArrayList<>();
        myIntersectionReports = new ArrayList<>();
    }

    /**
     * Constructor where a list of polygons are provided for intersection
     * checking, there will be one report for each of those in the initial list
     *
     * @param basePolys
     */
    public PolygonIntersectionCalculator(List<PolygonHD> basePolys)
    {
        this();
        myBasePolys.addAll(basePolys);
        initialize();
    }

    /**
     * Constructor where base set consists of only one polygon, so report list
     * will only have one report on this polygon.
     *
     * @param basePoly
     */
    public PolygonIntersectionCalculator(PolygonHD basePoly)
    {
        this();
        myBasePolys.add(basePoly);
        initialize();
    }

    /**
     * Initializes the calculator
     */
    protected void initialize()
    {
        myIntersectionReports.clear();
        for (PolygonHD poly : myBasePolys)
        {
            PolygonIntersectionReport report = new PolygonIntersectionReport(poly);
            myIntersectionReports.add(report);
        }
    }

    /**
     * Checks for intersection of the given poly with the base set, results are
     * added to the IntersectReport list
     *
     * @param poly
     */
    public void checkForIntersections(PolygonHD poly)
    {
        for (int i = 0; i < myBasePolys.size(); i++)
        {
            if (poly != myBasePolys.get(i) && intersects(myBasePolys.get(i), poly))
            {
                myIntersectionReports.get(i).addIntersectingPolygon(poly);
            }
        }
    }

    /**
     * Checks the provided list of polygons for intersections with the original
     * set of polygons for this calculator. Adds these intersections to the
     * existing intersection reports for the base polys. In this case even if a
     * poly is compared to itself it is counted as an intersection unless the
     * lists for base and list to check are the same in both order and
     * composition. Note: If the provided list here is the same list given to
     * create the base set a more efficient algorithm is used.
     *
     * Retrieve the results by getting the
     *
     * @param polys - the polys to check for intersection with the base set.
     */
    public void checkForIntersections(List<PolygonHD> polys)
    {
        if (polys == null)
        {
            return;
        }

        // Check to see if the two lists are the same, i.e. we're checkign
        // for intersections within the same set.
        boolean isSameList = true;
        if (polys.size() == myBasePolys.size())
        {
            for (int i = 0; i < myBasePolys.size(); i++)
            {
                if (polys.get(i) != myBasePolys.get(i))
                {
                    isSameList = false;
                    break;
                }
            }
        }

        // If we're comparing against ourselves use a different algorithm
        // to minimize compares to only those that are necessary
        if (isSameList)
        {
            if (myBasePolys.size() > 1)
            {
                PolygonHD poly1 = null;
                PolygonHD poly2 = null;
                for (int i = 0; i < myBasePolys.size() - 1; i++)
                {
                    poly1 = myBasePolys.get(i);
                    for (int j = i + 1; j < myBasePolys.size(); j++)
                    {
                        poly2 = myBasePolys.get(j);

                        if (poly1 == poly2)
                        {
                            continue;
                        }

                        if (intersects(poly1, poly2))
                        {
                            myIntersectionReports.get(i).addIntersectingPolygon(poly2);
                            myIntersectionReports.get(j).addIntersectingPolygon(poly1);
                        }
                    }
                }
            }
        }
        else // Different lists compare everything all the time
        {
            PolygonHD polyBase = null;
            PolygonHD polyExt = null;
            for (int i = 0; i < myBasePolys.size(); i++)
            {
                polyBase = myBasePolys.get(i);
                for (int j = 0; j < polys.size(); j++)
                {
                    polyExt = polys.get(j);

                    if (polyBase == polyExt)
                    {
                        continue;
                    }

                    if (intersects(polyBase, polyExt))
                    {
                        myIntersectionReports.get(i).addIntersectingPolygon(polyExt);
                    }
                }
            }
        }
    }

    /**
     * Tests if a polygon polyA, bounded by boundsA, intersects with polygon
     * polyB bounded by boundsB The bounds are checked first to see if they
     * intersect before performing the more expensive Area based intersection
     * check.
     *
     * @param boundsA - the bounds of polyA
     * @param polyA - the first polygon
     * @param boundsB - the bounds of polyB
     * @param polyB - the second polygon
     * @return true if they intersect, false if not.
     */
    protected static boolean intersects(PolygonHD polyA, PolygonHD polyB)
    {
        if (polyA.getBounds2D().intersects(polyB.getBounds2D()))
        {
            Area areaA = new Area(polyA);
            Area areaB = new Area(polyB);
            areaA.intersect(areaB);
            if (!areaA.isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the list of {@link PolygonIntersectionReport} one for each of the
     * original polygons provided to the calculator.
     *
     * @return the list of {@link PolygonIntersectionReport}
     */
    public List<PolygonIntersectionReport> getReports()
    {
        return myIntersectionReports;
    }

    /**
     * Resets the calculator to preform a new set of intersection checks with
     * the original polygon list.
     */
    public void reset()
    {
        initialize();
    }

    public static void main(String[] args)
    {

        ArrayList<PolygonHD> pList1 = new ArrayList<>();
        HashMap<PolygonHD, String> ptos = new HashMap<>();
        PolygonHD p1 = new PolygonHD();
        p1.addPoint(1, 1);
        p1.addPoint(4, 1);
        p1.addPoint(2, 3);
        p1.addPoint(1, 1);
        ptos.put(p1, "P1");
        pList1.add(p1);

        PolygonHD p2 = new PolygonHD();
        p2.addPoint(1, 5);
        p2.addPoint(3, 5);
        p2.addPoint(3, 7);
        p2.addPoint(1, 7);
        p2.addPoint(1, 5);
        ptos.put(p2, "P2");
        pList1.add(p2);

        PolygonHD p3 = new PolygonHD();
        p3.addPoint(2, 4);
        p3.addPoint(7, 3);
        p3.addPoint(2, 6);
        p3.addPoint(2, 4);
        ptos.put(p3, "P3");
        pList1.add(p3);

        PolygonHD p4 = new PolygonHD();
        p4.addPoint(4, 6);
        p4.addPoint(7, 6);
        p4.addPoint(7, 4);
        p4.addPoint(4, 6);
        ptos.put(p4, "P4");
        pList1.add(p4);

        PolygonHD p5 = new PolygonHD();
        p5.addPoint(1, 0);
        p5.addPoint(5, 7);
        p5.addPoint(4, 2);
        p5.addPoint(1, 0);
        ptos.put(p5, "P5");
        pList1.add(p5);

        PolygonHD p6 = new PolygonHD();
        p6.addPoint(8, 4);
        p6.addPoint(9, 4);
        p6.addPoint(9, 6);
        p6.addPoint(8, 6);
        p6.addPoint(8, 4);
        ptos.put(p6, "P6");
        pList1.add(p6);

        PolygonHD p7 = new PolygonHD();
        p7.addPoint(8, 3.999);
        p7.addPoint(9.001, 3.999);
        p7.addPoint(9.001, 6);
        p7.addPoint(10, 3);
        p7.addPoint(8, 3.999);
        ptos.put(p7, "P7");
        pList1.add(p7);

        ArrayList<PolygonHD> plist2 = new ArrayList<>(pList1);
        Collections.reverse(plist2);

        PolygonIntersectionCalculator pcalc = new PolygonIntersectionCalculator(pList1);
        pcalc.checkForIntersections(plist2);

        List<PolygonIntersectionReport> reports = pcalc.getReports();
        for (int i = 0; i < reports.size(); i++)
        {
            PolygonIntersectionReport report = reports.get(i);
            System.out.println("Report[" + i + "][" + ptos.get(report.getPolygon()) + "] Intersections["
                    + report.getIntersectingPolygons().size() + "]");
            ArrayList<PolygonHD> polys = report.getIntersectingPolygons();
            for (int j = 0; j < polys.size(); j++)
            {
                System.out.println("   With Poly: " + ptos.get(polys.get(j)));
            }
        }

    }

}
