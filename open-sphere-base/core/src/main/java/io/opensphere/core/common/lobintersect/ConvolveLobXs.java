package io.opensphere.core.common.lobintersect;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.opensphere.core.common.convolve.Convolve;
import io.opensphere.core.common.geospatial.model.DataEllipse;

/**
 * Contains methods for creating convolved ellipses from a list of ellipses that
 * have been correlated with each other and contain a cluster ID.
 */
public class ConvolveLobXs
{

    /**
     * Convolves correlated ellipses
     *
     * @param list of LobIntersectPoint's containing ellipses data and cluster
     *            ID
     * @return List of convolved ellipses.
     */
    public static List<ClusterEllipse> convolve(List<LobIntersectPoint> list)
    {

        /* Hash LobIntersections by cluster ID Store the index from the
         * intersections list for the first intersection in each cluster to
         * refer to later to obtain time, range, and rf for the convolved data
         * point. */
        Hashtable<Integer, List<DataEllipse>> table = new Hashtable<>();
        HashMap<Integer, List<Integer>> lobsXIndex = new HashMap<>();

        int index = -1;
        for (LobIntersectPoint pt : list)
        {
            index++;
            double maj = pt.getSma() / 1852;
            double min = pt.getSmi() / 1852;
            double orient = Math.toDegrees(pt.getTheta());
            double lat = Math.toDegrees(pt.getLat());
            double lon = Math.toDegrees(pt.getLon());

            DataEllipse e = new DataEllipse(maj, min, orient, lon, lat);

            int clusterNum = pt.getClusterNumber();
            List<DataEllipse> clusterList = table.get(clusterNum);
            if (clusterList == null)
            {
                clusterList = new ArrayList<>();
                clusterList.add(e);
                table.put(clusterNum, clusterList);
            }
            else
            {
                clusterList.add(e);
            }

            /* Maintain the list of LobIntersects that participated in this
             * cluster */
            List<Integer> lobsList = lobsXIndex.get(clusterNum);
            if (lobsList == null)
            {
                lobsList = new ArrayList<>();
                lobsList.add(index);
                lobsXIndex.put(clusterNum, lobsList);
            }
            else
            {
                lobsList.add(index);
            }
        }

        ArrayList<ClusterEllipse> ellipseList = new ArrayList<>();

        Convolve convolver = new Convolve();
        Enumeration<Integer> keys = table.keys();

        /* Loop over each cluster */
        while (keys.hasMoreElements())
        {
            int key = keys.nextElement();
            List<DataEllipse> clusterList = table.get(key);
            DataEllipse ellipse = convolver.calculate(clusterList);

            /* Map the ellipse orientation to 0-360 for readability. */
            double theta = ellipse.getOrientation();
            if (theta < 0)
            {
                ellipse.setOrientation(theta + 360);
            }

            /* Construct the list of contributing lob feature Ids. */
            List<Integer> lobIndices = lobsXIndex.get(key);
            List<String> lobidList = buildIdList(lobIndices, list);

            /* Build the cluster name from the list of contributing lobs. */
            String clusterName = buildName(lobIndices, list);

            /* Arbitrarily grab time, rf, and range from the 1st intersection in
             * the cluster. */
            int ndx = lobIndices.get(0);
            LobIntersectPoint firstX = list.get(ndx);

            ClusterEllipse ce = new ClusterEllipse(key, ellipse, clusterName, firstX.getTimeStamp(), firstX.getFrequency1(),
                    firstX.getRange(), firstX.getLocalTime(), lobidList);
            ellipseList.add(ce);
        }

        return ellipseList;
    }

    /**
     * Builds a list of lob feature Ids for a given list of lob intersection
     * indices.
     *
     * @param lobIndices - integer list of lob indices
     * @param lobXList - list of lob intersections
     * @return feature Ids
     */
    private static List<String> buildIdList(List<Integer> lobIndices, List<LobIntersectPoint> lobXList)
    {

        Set<String> featureIds = new LinkedHashSet<>();
        /* Loop over each index */
        for (int i : lobIndices)
        {
            LobIntersectPoint x = lobXList.get(i);
            /* Each intersection contains two points, attempt to add the
             * featureId for each into a set so duplicates are removed. */
            String id1 = x.getLobs().get(0).getFeatureId();
            String id2 = x.getLobs().get(1).getFeatureId();

            featureIds.add(id1);
            featureIds.add(id2);
        }

        /* Convert the set to a list to return. */
        List<String> vals = new ArrayList<>(featureIds);
        return vals;
    }

    /**
     * Builds a cluster name from the contributing lob intersection's names.
     *
     * @param lobIndices - index of contributing lobs intersections
     * @param lobXList - list of intersections
     * @return String name of the cluster.
     */
    private static String buildName(List<Integer> lobIndices, List<LobIntersectPoint> lobXList)
    {

        Set<String> names = new LinkedHashSet<>();
        /* Loop over each index */
        for (int i : lobIndices)
        {
            LobIntersectPoint x = lobXList.get(i);
            /* Each intersection contains two points, attempt to add the
             * featureId for each into a set so duplicates are removed. */
            String name1 = x.getLobs().get(0).getPtName();
            String name2 = x.getLobs().get(1).getPtName();

            names.add(name1);
            names.add(name2);
        }

        /* Convert the set to a list to return. */
        StringBuilder clusterName = new StringBuilder();
        int size = names.size();
        int counter = 1;
        for (String name : names)
        {
            clusterName.append(name);
            if (counter < size)
            {
                clusterName.append("/");
            }
            counter++;
        }

        return clusterName.toString();
    }
}