package io.opensphere.core.common.lobintersect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains a correlation algorithm for grouping related LOB intersections.
 */
public class LobIntersectCluster
{

    /** expressed in nautical miles. */
    private double radiusAllowed;

    private double maxDeltaT;

    private double maxDeltaRF;

    private int min_inCluster;

    /**
     * Public constructor
     *
     * @param radiusAllowed
     * @param maxDeltaT
     * @param maxDeltaRF
     * @param min_inCluster
     */
    public LobIntersectCluster(double radiusAllowed, double maxDeltaT, double maxDeltaRF, int min_inCluster)
    {

        this.radiusAllowed = radiusAllowed;
        this.maxDeltaT = maxDeltaT;
        this.maxDeltaRF = maxDeltaRF;
        this.min_inCluster = min_inCluster;
    }

    /**
     * Thou shalt use the public constructor only.
     */
    @SuppressWarnings("unused")
    private LobIntersectCluster()
    {
    }

    /**
     * Creates an N length array populated with zeros.
     *
     * @param size
     * @return
     */
    private List<Integer> zeroList(int size)
    {
        ArrayList<Integer> t = new ArrayList<>();
        for (int i = 0; i < size; i++)
        {
            t.add(0);
        }

        return t;
    }

    /**
     * Computes a density array of the number of points near each point using
     * lat, lon, time, and frequency.
     *
     * @param pts
     * @param latAllowed - tolerance
     * @param lonAllowed - tolerance
     * @return int[] density array
     */
    private List<Integer> densitize(List<LobIntersectPoint> pts, double latAllowed, double lonAllowed)
    {
        List<Integer> density = zeroList(pts.size());

        int d = 0;
        for (LobIntersectPoint pt1 : pts)
        {
            for (LobIntersectPoint pt2 : pts)
            {
                if (!pt1.equals(pt2) && Math.abs(pt1.getLat() - pt2.getLat()) < latAllowed
                        && Math.abs(pt1.getLon() - pt2.getLon()) < lonAllowed
                        && Math.abs(pt1.getFrequency1() - pt2.getFrequency1()) < maxDeltaRF
                        && Math.abs(pt1.getTimeStamp().getTime() - pt2.getTimeStamp().getTime()) < maxDeltaT)
                {
                    int count = density.get(d).intValue() + 1;
                    density.set(d, count);
                }
            }
            d++;
        }
        return density;
    }

    /**
     * Creates an index array based on the compare array passed in.
     *
     * @param compare
     * @return sorted array of indices.
     */
    public List<Integer> sortIndex(List<Integer> compare)
    {

        ArrayList<Integer> index = new ArrayList<>();
        ArrayList<Boolean> used = new ArrayList<>();
        for (int i = 0; i < compare.size(); i++)
        {
            used.add(false);
        }

        for (int j = 0; j < compare.size(); j++)
        {
            int min = Integer.MAX_VALUE;
            int posMin = 0;

            for (int i = 0; i < compare.size(); i++)
            {
                if (compare.get(i) < min && !used.get(i))
                {
                    min = compare.get(i);
                    posMin = i;
                }
            }

            index.add(posMin);
            used.set(posMin, true);
        }
        return index;
    }

    /**
     * Populates the cluster ID field of the passed in list of LOB
     * intersections.
     *
     * @param x - Pre-generated list of LOB intersects.
     * @param meanLat - Pre-calculated average of all the intersection
     *            latitudes.
     */
    public void cluster(List<LobIntersectPoint> x, double meanLat)
    {

        int n = x.size();
        double percentInCommon = 0.25;
        double latAllowed = radiusAllowed / 60.0;
        double lonAllowed = latAllowed / Math.cos(meanLat);

        latAllowed = Math.toRadians(latAllowed);
        lonAllowed = Math.toRadians(lonAllowed);

        List<Integer> density = densitize(x, latAllowed, lonAllowed);

        List<Integer> index = sortIndex(density);

        Collections.reverse(index);

        List<Integer> cluster_M = zeroList(n);

        List<Integer> ndx_b = new ArrayList<>();
        List<Integer> ndx_c = new ArrayList<>();

        int n_cl = 0;
        int n_pot_cl = 0;
        for (int m = 0; m < n; m++)
        {

            int pt2Process = index.get(m);
            List<Integer> pot_cluster_M = zeroList(n);

            boolean pot_cl = false;
            boolean combine = false;
            double centerPtLat = 0;
            double centerPtLon = 0;
            double meanSoiRf = 0;
            double meanSoiTime = 0;

            if (cluster_M.get(pt2Process) == 0)
            {
                /* get all the points within search box of pt2process. */
                LobIntersectPoint ptp = x.get(pt2Process);

                int xcntr = 0;

                for (LobIntersectPoint curPt : x)
                {

                    if (Math.abs(ptp.getLat() - curPt.getLat()) < latAllowed
                            && Math.abs(ptp.getLon() - curPt.getLon()) < lonAllowed
                            && Math.abs(ptp.getTimeStamp().getTime() - curPt.getTimeStamp().getTime()) < maxDeltaT
                            && Math.abs(ptp.getFrequency1() - curPt.getFrequency2()) < maxDeltaRF)
                    {

                        centerPtLat += curPt.getLat();
                        centerPtLon += curPt.getLon();
                        meanSoiRf += curPt.getFrequency1();
                        meanSoiTime += curPt.getTimeStamp().getTime();
                        xcntr++;
                    }
                }

                /* Determine lat/lon centroid */
                centerPtLat /= xcntr;
                centerPtLon /= xcntr;
                meanSoiRf /= xcntr;
                meanSoiTime /= xcntr;
                ndx_b.clear();
                xcntr = 0;

                for (int i = 0; i < n; ++i)
                {

                    LobIntersectPoint curPt = x.get(i);

                    if (Math.abs(centerPtLat - curPt.getLat()) < latAllowed && Math.abs(centerPtLon - curPt.getLon()) < lonAllowed
                            && Math.abs(meanSoiTime - curPt.getTimeStamp().getTime()) < maxDeltaT
                            && Math.abs(meanSoiRf - curPt.getFrequency1()) < maxDeltaRF)
                    {

                        ndx_b.add(i);
                        xcntr++;
                        // xcntr=0;
                    }
                }

                int num_poss = xcntr;
                if (num_poss >= min_inCluster)
                {
                    pot_cl = true;

                    for (int i = 0; i < num_poss; ++i)
                    {
                        int ndx = ndx_b.get(i);
                        pot_cluster_M.set(ndx, 1);
                    }

                    /* loop through each point of the cluster and see if we can
                     * add any more points to the cluster. */
                    for (int i = 0; i < num_poss; ++i)
                    {
                        LobIntersectPoint li = x.get(ndx_b.get(i));

                        if (ndx_b.get(i) != pt2Process)
                        {
                            if (density.get(ndx_b.get(i)) >= min_inCluster)
                            {
                                ndx_c.clear();
                                for (int j = 0; j < n; ++j)
                                {

                                    LobIntersectPoint lj = x.get(j);

                                    if (Math.abs(li.getLat() - lj.getLat()) < latAllowed
                                            && Math.abs(li.getLon() - lj.getLon()) < lonAllowed
                                            && Math.abs(li.getTimeStamp().getTime() - lj.getTimeStamp().getTime()) < maxDeltaT
                                            && Math.abs(li.getFrequency1() - lj.getFrequency1()) < maxDeltaRF)
                                    {
                                        ndx_c.add(j);
                                    }
                                }
                                for (int j = 0; j < ndx_c.size(); ++j)
                                {
                                    pot_cluster_M.set(ndx_c.get(j), 1);
                                }
                            }
                        }
                    }
                }
            }

            /* check sortIndex on all references to pot and cluster m */
            int add_to_cl = 0;
            if (pot_cl)
            {
                ++n_pot_cl;
                if (n_cl == 0)
                {
                    ++n_cl;
                    for (int u = 0; u < pot_cluster_M.size(); ++u)
                    {
                        if (pot_cluster_M.get(u) == 1)
                        {
                            cluster_M.set(u, n_cl);
                        }
                    }
                }
                else
                {
                    /* see if this potential cluster can be combined with an
                     * existing cluster. */
                    combine = false;
                    float ratio = 0.0f;
                    for (int j = 1; j <= n_cl; ++j)
                    {
                        int cnt = 0;
                        for (int i = 0; i < n; ++i)
                        {
                            if (pot_cluster_M.get(i) == 1 && cluster_M.get(i) == j)
                            {
                                ++cnt;
                            }
                        }

                        int pcmcnt = 0;
                        for (int u = 0; u < pot_cluster_M.size(); ++u)
                        {
                            if (pot_cluster_M.get(u) == 1)
                            {
                                ++pcmcnt;
                            }
                        }

                        if (pcmcnt != 0)
                        {
                            ratio = (float)cnt / (float)pcmcnt;
                        }
                        else
                        {
                            System.out.println("Algorithm fail - pcmcnt = 0");
                            return;
                        }
                        if (ratio >= percentInCommon)
                        {
                            /* combine the cluster and potential cluster. */
                            combine = true;
                            add_to_cl = j;
                            break;
                        }
                    }

                    if (!combine)
                    {
                        ++n_cl;
                        for (int i = 0; i < n; ++i)
                        {
                            if (pot_cluster_M.get(i) == 1 && cluster_M.get(i) == 0)
                            {
                                cluster_M.set(i, n_cl);
                            }
                        }
                    }
                    else
                    {
                        /* combine is true */
                        for (int u = 0; u < pot_cluster_M.size(); ++u)
                        {
                            if (pot_cluster_M.get(u) == 1)
                            {
                                cluster_M.set(u, add_to_cl);
                            }
                        }
                    }
                }
            }
        }

        for (int u = 0; u < cluster_M.size(); u++)
        {
            int clusterNumber = cluster_M.get(u);
            x.get(u).setClusterNumber(clusterNumber);
        }
    }
}
