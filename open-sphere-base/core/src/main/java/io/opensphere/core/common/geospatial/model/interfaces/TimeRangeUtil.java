package io.opensphere.core.common.geospatial.model.interfaces;

import java.util.Date;

public final class TimeRangeUtil
{
    /**
     * Tests to see if TimeRange b intersects, is less than, or greater than
     * TimeRange a.
     *
     * @param a - the first to test
     * @param b - the range to see if it intersects (inclusive)with a
     * @return 0 if b intersects a, -1 if b less than a, 1 if b greater than a.
     */
    public static int intersects(TimeRange a, TimeRange b)
    {
        long aStart = a.getStartDate().getTime();
        long aEnd = a.getEndDate() == null ? aStart : a.getEndDate().getTime();
//       if ( aEnd < aStart )
//       {
//           long temp = aStart;
//           aStart = aEnd;
//           aEnd = temp;
//       }
//
        long bStart = b.getStartDate().getTime();
        long bEnd = b.getStartDate() == null ? bStart : b.getEndDate().getTime();
//       if ( bEnd < bStart )
//       {
//           long temp = bStart;
//           bStart = bEnd;
//           aEnd = temp;
//       }

        // Test for intersection.
        if (bEnd < aStart)
        {
            return -1;
        }
        else if (bStart > aEnd)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    public static void main(String[] args)
    {
        Tr a = new Tr(1000, 2000);

        Tr bInterior = new Tr(1200, 1800);
        Tr bRightEdge = new Tr(1500, 2400);
        Tr bLeftEdge = new Tr(500, 1500);
        Tr bOverSpan = new Tr(500, 2500);
        Tr bAfter = new Tr(2001, 3000);
        Tr bBefore = new Tr(500, 999);
        Tr bEquals = new Tr(1000, 2000);

        System.out.println("Interior : " + TimeRangeUtil.intersects(a, bInterior) + " Expect: 0");
        System.out.println("RightEdge: " + TimeRangeUtil.intersects(a, bRightEdge) + " Expect: 0");
        System.out.println("LeftEdge : " + TimeRangeUtil.intersects(a, bLeftEdge) + " Expect: 0");
        System.out.println("OverSpan : " + TimeRangeUtil.intersects(a, bOverSpan) + " Expect: 0");
        System.out.println("After    : " + TimeRangeUtil.intersects(a, bAfter) + " Expect: 1");
        System.out.println("Before   : " + TimeRangeUtil.intersects(a, bBefore) + " Expect: -1");
        System.out.println("Equals   : " + TimeRangeUtil.intersects(a, bEquals) + " Expect: 0");

    }

    public static class Tr implements TimeRange
    {
        Date startDate;

        Date endDate;

        public Tr(int start, int end)
        {
            this(new Date(start), new Date(end));
        }

        public Tr(Date start, Date end)
        {
            startDate = start;
            endDate = end;
        }

        @Override
        public Date getDate()
        {
            return startDate;
        }

        @Override
        public Date getEndDate()
        {
            return endDate;
        }

        @Override
        public Date getStartDate()
        {
            return startDate;
        }

        @Override
        public void setEndDate(Date endDate)
        {
            this.endDate = endDate;
        }
    }
}
