package io.opensphere.core.common.lobintersect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import io.opensphere.core.common.configuration.Configurator;
import io.opensphere.core.common.coordinate.math.strategy.Vincenty;
import io.opensphere.core.common.geospatial.model.interfaces.IDataPoint;
import io.opensphere.core.common.time.DateUtils;

/**
 * Computes intersections between lobs, and orchestrates the correlation and
 * convolving of the results.
 */
public class LobIntersectCalculator
{
    protected Log LOGGER = LogFactory.getLog(LobIntersectCalculator.class);

    private final Vincenty vincenty = new Vincenty();

    private String LobCsvFile;

    // initially read in as degs, then converted to rads
    private double minLat;

    // ditto
    private double maxLat;

    // ditto
    private double minLon;

    // ditto
    private double maxLon;

    private final String BAD_VAL = "_BAD_VAL_";

    private final Vincenty earth = new Vincenty();

    private final double radsPerHr = Math.PI * 2.0 / 24;

    private final Calendar localCal = Calendar.getInstance();

    private List<LobIntersectPoint> intersections = new ArrayList<>();

    // Configuration keys Keys
    private final String SITENAME_KEY = "lob.intersect.site.name.column";

    private final String FREQ_KEY = "lob.intersect.frequency.column";

    private final String BEARING_KEY = "lob.intersect.bearing.column";

    private final String FOF_KEY = "lob.intersect.figure.of.fit.column";

    private final String SOI_CONF_KEY = "lob.intersect.soi.confirmation.column";

    private final String REQUIRE_ONE_CONF_KEY = "lob.intersect.require.one.confidence.flag";

    private final String USE_CONF_ONLY_KEY = "lob.intersect.use.only.confidence.flag";

    private final String USE_FOF_KEY = "lob.intersect.use.fof.flag";

    private final String MAX_RANGE_KEY = "lob.intersect.max.range.km";

    private final String RADIUS_ALLOWED_KEY = "lob.intersect.radius.allowed";

    private final String MIN_IN_CLUSTER_KEY = "lob.intersect.min.in.cluster";

    private final String MAX_DELTA_TIME_KEY = "lob.intersect.max.delta.time.sec";

    private final String MAX_DELTA_RF_KEY = "lob.intersect.max.delta.rf.hz";

    private final String DROPBOX_KEY = "lob.intersect.dropbox.location";

    private final String CONTRIB_OUTPUT_FILENAME_KEY = "lob.intersect.contributing.output.filename";

    final double twoSigmaScaleFactor = 2.4477;

    // Values read from Configuration
    private String[] siteColKeys;

    private String[] freqColKeys;

    private String[] bearingColKeys;

    private String[] fofColKeys;

    private String[] soiConfColKeys;

    private boolean useConfirmedPtsOnly = false;

    private boolean requireOneConfirmedPt = false;

    private boolean useFigureOfFitFlag = false;

    private double maxRange = 0.0;

    private double radiusAllowed;

    private int minInCluster;

    private double maxDeltaTime;

    private double maxDeltaRF;

    private UnscentedTransform ut = new UnscentedTransform();

    // 50km from collector
    private double minRange = 50000;

    private CourseIntersect courseIntersect;

    // private int invalidateXs = 0;
    // private int outvalidateXs = 0;
    // private int arcDistPass = 0;
    // private int unscentedPass= 0;
    // private int passRangeTOD= 0;
    private boolean useCrsIntersect = true;

    private boolean useJacobi = false;

    private String contributing_output_file = "";

    private String dropbox_location = "";

    public LobIntersectCalculator()
    {
        clearIntersections();
        courseIntersect = new CourseIntersect();
    }

    public void init()
    {
        // Extract values for configurable data
        CombinedConfiguration config = Configurator.getConfig();
        siteColKeys = config.getStringArray(SITENAME_KEY);
        freqColKeys = config.getStringArray(FREQ_KEY);
        bearingColKeys = config.getStringArray(BEARING_KEY);
        fofColKeys = config.getStringArray(FOF_KEY);
        soiConfColKeys = config.getStringArray(SOI_CONF_KEY);

        requireOneConfirmedPt = config.getBoolean(REQUIRE_ONE_CONF_KEY);
        useConfirmedPtsOnly = config.getBoolean(USE_CONF_ONLY_KEY);

        useFigureOfFitFlag = config.getBoolean(USE_FOF_KEY);
        // convert to meters
        maxRange = config.getDouble(MAX_RANGE_KEY) * 1000;
        radiusAllowed = config.getDouble(RADIUS_ALLOWED_KEY);
        minInCluster = config.getInt(MIN_IN_CLUSTER_KEY);
        // ms
        maxDeltaTime = config.getDouble(MAX_DELTA_TIME_KEY) * 1000;
        maxDeltaRF = config.getDouble(MAX_DELTA_RF_KEY);
        contributing_output_file = config.getString(CONTRIB_OUTPUT_FILENAME_KEY);
        dropbox_location = config.getString(DROPBOX_KEY);
    }

    /**
     * Clears out previously computed intersections.
     */
    public void clearIntersections()
    {
        intersections.clear();
    }

    /**
     * Reads program configuration data from the specified file.
     *
     * @param fileName
     */
    @SuppressWarnings("unused")
    private void readConfigValues(String fileName)
    {
        System.out.println("Using config file: " + fileName);
        try (DataInputStream in = new DataInputStream(new FileInputStream(fileName)))
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            LobCsvFile = br.readLine();
            // Skip a line
            br.readLine();

            String val = br.readLine();
            if (val != null)
            {
                minLat = Double.parseDouble(val);
                minLat = Math.toRadians(minLat);
            }

            val = br.readLine();
            if (val != null)
            {
                maxLat = Double.parseDouble(val);
                maxLat = Math.toRadians(maxLat);
            }

            val = br.readLine();
            if (val != null)
            {
                minLon = Double.parseDouble(val);
                minLon = Math.toRadians(minLon);
            }

            val = br.readLine();
            if (val != null)
            {
                maxLon = Double.parseDouble(val);
                maxLon = Math.toRadians(maxLon);
            }

            val = br.readLine();
            if (val != null)
                // Convert to meters
                maxRange = Double.parseDouble(val) * 1000;

            val = br.readLine();
            if (val != null)
                radiusAllowed = Double.parseDouble(val);

            val = br.readLine();
            if (val != null)
                minInCluster = Integer.parseInt(val);

            val = br.readLine();
            if (val != null)
                // convert to ms
                maxDeltaTime = Double.parseDouble(val) * 1000;

            val = br.readLine();
            if (val != null)
                maxDeltaRF = Double.parseDouble(val);

            val = br.readLine();
            if (val != null)
            {
                double dblVal = Double.parseDouble(val);
                // not used
                double maxDeltaBearing = Math.toRadians(dblVal);
            }

            val = br.readLine();
            if (val != null)
            {
                double dblVal = Double.parseDouble(val);
                // not used
                double maxDeltaElevation = Math.toRadians(dblVal);
            }

            val = br.readLine();
            if (val != null && val.equals("pare"))
                useConfirmedPtsOnly = true;

            val = br.readLine();
            if (val.equals("pare"))
                useFigureOfFitFlag = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Replaces nulls and empty strings with a flag to indicate an invalid
     * value.
     *
     * @param row - Row of data.
     * @return - Array of formatted elements.
     */
    private ArrayList<String> formatInput(String row)
    {
        ArrayList<String> formatted = new ArrayList<>();
        String[] elements = row.split(",");
        for (int i = 0; i < elements.length; i++)
        {
            if (elements[i] == null || elements[i].equals("") || elements[i].equals("---") || elements[i].equals("-99"))
            {
                formatted.add(BAD_VAL);
            }
            else
            {
                formatted.add(elements[i]);
            }
        }
        return formatted;
    }

    /**
     * Reads in Lobs from a csv files.
     *
     * @return
     */
    private List<LobPoint> readLobData()
    {
        System.out.println("Using LOB CSV file: " + LobCsvFile);
        int recordsRead = 0;
        Date minDate = null;
        Date maxDate = null;
        List<LobPoint> lobs = new ArrayList<>();
        int badVal = 0;
        int latLonoor = 0;
        try (DataInputStream in = new DataInputStream(new FileInputStream(LobCsvFile)))
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // Read the header line and locate the columns we care about.
            String data = br.readLine();
            String[] pieces = data.split(",");
            int timeCol = -1;
            int siteCol = -1;
            int freqCol = -1;
            int latCol = -1;
            int lonCol = -1;
            int bearingCol = -1;
            int fofCol = -1;
            int elevCol = -1;
            int soiConfCol = -1;

            for (int i = 0; i < pieces.length; i++)
            {
                if (pieces[i].equals("Time"))
                    timeCol = i;
                if (pieces[i].equals("Site"))
                    siteCol = i;
                if (pieces[i].equals("True Center Freq (MHz)") || pieces[i].equals("Freq (MHz)"))
                {
                    freqCol = i;
                }
                if (pieces[i].equals("Station Latitude (Deg)"))
                    latCol = i;
                if (pieces[i].equals("Station Longitude (Deg)"))
                    lonCol = i;
                if (pieces[i].equals("Bearing (Deg)"))
                    bearingCol = i;
                if (pieces[i].equals("Figure of Fit"))
                    fofCol = i;
                if (pieces[i].equals("Elevation (Deg)"))
                    elevCol = i;
                if (pieces[i].equals("Soi Confirmation Level"))
                    soiConfCol = i;
            }

            // Verify all required columns are defined.
            if (timeCol == -1 || siteCol == -1 || freqCol == -1 || lonCol == -1 || latCol == -1 || bearingCol == -1
                    || fofCol == -1 || elevCol == -1 || soiConfCol == -1)
            {
                System.out.println("Invalid data format, column header missing.");
                return null;
            }

            TimeZone.setDefault(DateUtils.GMT_TIME_ZONE);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HHmmss.SSS'Z'");
            sdf.setTimeZone(DateUtils.GMT_TIME_ZONE);

            // Read the data and store the columns of interest.
            data = br.readLine();
            while (data != null)
            {
                try
                {
                    List<String> elements = formatInput(data);
                    // Skip this line if it is another header row.
                    if (elements.get(siteCol).equals("Site"))
                    {
                        data = br.readLine();
                        continue;
                    }

                    recordsRead++;

                    String dateString = elements.get(timeCol);
                    Date dt = sdf.parse(dateString);

                    String site = elements.get(siteCol);
                    String soiConf = elements.get(soiConfCol);
                    String freqString = elements.get(freqCol);
                    String latString = elements.get(latCol);
                    String lonString = elements.get(lonCol);
                    String bearingString = elements.get(bearingCol);
                    String fofString = elements.get(fofCol);
                    // String elevationString = elements.get(elevCol);

                    if (useConfirmedPtsOnly)
                    {
                        if (!soiConf.equals("definite"))
                        {
                            data = br.readLine();
                            // skip this record
                            continue;
                        }
                    }
                    else if (useFigureOfFitFlag)
                    {
                        if (fofString.equals(BAD_VAL))
                        {
                            data = br.readLine();
                            // skip this record
                            continue;
                        }
                    }

                    if (!latString.equals(BAD_VAL) && !lonString.equals(BAD_VAL) && !bearingString.equals(BAD_VAL)
                            && !freqString.equals(BAD_VAL))
                    {
                        double freq = Double.valueOf(freqString) * 1e6;
                        double lat = Math.toRadians(Double.valueOf(latString));
                        double lon = Math.toRadians(Double.valueOf(lonString));
                        double bearing = Math.toRadians(Double.valueOf(bearingString));

                        if (lat > maxLat || lat < minLat || lon > maxLon || lon < minLon)
                        {
                            data = br.readLine();
                            latLonoor++;
                            continue;
                        }

                        // Determine range
                        double range = maxRange;
                        LobPoint pt = new LobPoint(dt, site, freq, lat, lon, 0.0, bearing, range, "Feature_ID_" + recordsRead);
                        pt.setHasConf(soiConf.equals("definite"));
                        lobs.add(pt);

                        if (minDate == null || dt.before(minDate))
                            minDate = dt;
                        if (maxDate == null || dt.after(maxDate))
                            maxDate = dt;

                    }
                    else
                    {
                        badVal++;
                    }
                }
                catch (ParseException e)
                {
                    // skip this record
                }

                data = br.readLine();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        LOGGER.info("LOB records read = " + recordsRead);
        LOGGER.info("LOB records used = " + lobs.size());
        LOGGER.info("LOB records w/BAD_VAL = " + badVal);
        LOGGER.info("LOB records w/LL out of range = " + latLonoor);

        LOGGER.info("Date range is " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(minDate)
                + " through " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(maxDate));

        return lobs;
    }

    /**
     * Determines if a pair of points can be paired for intersection calculation
     * based on time/frequency coincidence and tolerances.
     *
     * @param pt1 - lob 1
     * @param pt2 - lob 2
     * @return boolean true if lobs should be crossed.
     */
    private boolean validPair(LobPoint pt1, LobPoint pt2)
    {
        // Verify the timeStamps are coincident.
        if (Math.abs(pt1.getTimeMs() - pt2.getTimeMs()) >= maxDeltaTime)
            return false;

        // Verify the frequencies are within tolerance
        if (Math.abs(pt1.getFreq() - pt2.getFreq()) >= maxDeltaRF)
            return false;

        // Verify the two points are not at the same location.
        if (Double.doubleToLongBits(pt1.getLat()) == Double.doubleToLongBits(pt2.getLat())
                && Double.doubleToLongBits(pt1.getLon()) == Double.doubleToLongBits(pt2.getLon()))
        {
            return false;
        }

        return true;
    }

    /**
     * Extracts coefficient values from the covariance matrix and solves for
     * roots using the quadratic equation.
     *
     * @param A - covariance matrix
     * @return list of 2 doubles
     */
    private List<Double> getEigenValues(DoubleMatrix2D A)
    {
        List<Double> eigen = new ArrayList<>(2);

        double a = 1;
        double b = -(A.getQuick(0, 0) + A.getQuick(1, 1));
        double c = A.getQuick(0, 0) * A.getQuick(1, 1) - A.getQuick(1, 0) * A.getQuick(0, 1);

        double bb4ac = b * b - 4 * a * c;

        if (bb4ac > 0)
        {
            double sign = b > 0 ? 1 : -1;
            double q = -0.5 * (b + sign * Math.sqrt(bb4ac));
            eigen.add(q / a);
            eigen.add(c / q);
        }

        return eigen;
    }

    /**
     * Uses frequency and local time of day to determine what the max allowable
     * range is.
     *
     * @param freq
     * @param longitude
     * @param local
     * @return
     */
    private double getRange(double freq, double longitude, Date local)
    {
        localCal.setTime(local);
        int hr = localCal.get(Calendar.HOUR_OF_DAY);

        int amStart = 7;
        int amEnd = 17;

        int irf = (int)(freq / 1000000);
        double range = -99;

        if (irf >= 3 && irf <= 7)
        {
            // band 1
            if (hr >= amStart && hr <= amEnd)
                // day time intercept (km)
                range = 450e3;
            else
                // km
                range = 3200e3;
        }
        else if (irf > 7 && irf <= 9)
        {
            // band 2
            if (hr >= amStart && hr <= amEnd)
                // day time intercept (km)
                range = 800e3;
            else
                // km
                range = 3200e3;
        }
        else if (irf >= 10 && irf <= 13)
        {
            // band 3 tbd upper end
            if (hr >= amStart && hr <= amEnd)
                // day time intercept (km)
                range = 3000e3;
            else
                // km
                range = 3200e3;
        }
        else if (irf >= 14 && irf <= 17)
        {
            // band 4 upper end TBR. congested (km)
            range = 3200e3;
        }
        else if (irf >= 18 && irf <= 21)
        {
            if (hr >= amStart && hr <= amEnd)
                range = 3200e3;
            else
                range = 0;
        }
        else if (irf >= 22 && irf <= 25)
        {
            if (hr >= amStart && hr <= amEnd)
                range = 3200e3;
            else
                range = 0;
        }

        return range;
    }

    /**
     * Performs a validation on the passed in pair of lobs,
     *
     * @param pt1
     * @param pt2
     * @return
     */
    private String validateIntersection(LobPoint pt1, LobPoint pt2, double intersectionLat, double intersectionLon, double xPt1Az,
            double xPt1Dist, double xPt2Az, double xPt2Dist)
    {
        // invalidateXs++;
        double arcDist1 = xPt1Dist;
        double arcDist2 = xPt2Dist;

        // all meters
        if (!(arcDist1 < pt1.getRange() && arcDist1 > minRange && arcDist2 < pt2.getRange() && arcDist2 > minRange))
            return "R";

        // arcDistPass++;
        // Use the unscented transform algorithm
        DoubleMatrix2D Pyy = ut.calculate(pt1, pt2);

        if (Pyy == null)
            // Unscented Transform fail
            return "U1";

        // Compute Eigen values
        List<Double> eigen = null;
        DoubleMatrix2D eVecs = null;
        if (useJacobi)
        {
            eVecs = new DenseDoubleMatrix2D(2, 2);
            double[][] A = new double[][] { { Pyy.getQuick(0, 0), Pyy.getQuick(0, 1) },
                { Pyy.getQuick(1, 0), Pyy.getQuick(1, 1) } };
            eigen = Jacobi.getEigen(A, 2, eVecs);
        }
        else
        {
            eigen = getEigenValues(Pyy);
        }

        // Compute ellipse orientation
        if (!(eigen.size() == 2 && numberValid(eigen.get(0)) && numberValid(eigen.get(1))))
            return "E";

        double theta = 0;
        if (useJacobi)
        {
            double t2;
            if (eigen.get(0) > eigen.get(1))
                t2 = Math.PI * 0.5 - Math.atan2(eVecs.getQuick(1, 0), eVecs.getQuick(0, 0));
            else
                t2 = Math.PI * 0.5 - Math.atan2(eVecs.getQuick(1, 1), eVecs.getQuick(0, 1));

            if (t2 < 0)
            {
                t2 += Math.PI * 2;
                t2 = t2 % Math.PI * 0.5;
            }
            theta = t2;
        }
        else
        {
            theta = 0.5 * Math.atan2(2 * Pyy.getQuick(0, 1), Pyy.getQuick(1, 1) - Pyy.getQuick(0, 0));
            if (theta < 0)
                theta += Math.PI * 2;
        }

        double lat1 = intersectionLat;
        double lon1 = intersectionLon;
        double lat2 = lat1 + twoSigmaScaleFactor * Math.sqrt(eigen.get(0));
        double lon2 = lon1;

        double[] azDist = earth.calculateDistanceBetweenPointsRadians(lat1, lon1, lat2, lon2);

        arcDist1 = azDist[0];
        lat2 = lat1;
        lon2 = lon1 + twoSigmaScaleFactor * Math.sqrt(eigen.get(1));

        azDist = earth.calculateDistanceBetweenPointsRadians(lat1, lon1, lat2, lon2);
        arcDist2 = azDist[0];

        if (!(numberValid(arcDist1) && arcDist1 != 0 && numberValid(arcDist2) && arcDist2 != 0))
            return "U2";

        // unscentedPass++;
        arcDist1 *= 0.5;
        arcDist2 *= 0.5;
        // Check eigen diagonal to determine if arcDistances should be swapped.
        if (eigen.get(1) > eigen.get(0))
        {
            double temp = arcDist1;
            arcDist1 = arcDist2;
            arcDist2 = temp;
        }

        // Determine local time from timestamp and longitude
        int deltaHrs = (int)(intersectionLon / radsPerHr);
        // convert to ms
        long localMs = pt1.getTimeMs() + (long)deltaHrs * 60 * 60 * 1000;
        Date localTime = new Date(localMs);

        double rng = getRange(pt1.getFreq(), intersectionLon, localTime);

        // Verify a valid range was computed.
        if (rng <= 0)
            return "TOD";

        // passRangeTOD++;
        double sma = arcDist1;
        double smi = arcDist2;
        // Create an intersection record, add to stored intersections
        LobIntersectPoint xp = new LobIntersectPoint(pt1, pt2, intersectionLat, intersectionLon, localTime, rng, theta, sma, smi,
                Pyy);
        intersections.add(xp);
        // duplicate...ignore
        return "G";
    }

    /**
     * Determines if the passed in value is reasonable.
     *
     * @param val
     * @return
     */
    private boolean numberValid(double val)
    {
        return !(Double.isInfinite(val) || Double.isNaN(val));
    }

    /**
     * Performs a pairing of all non-unique lobs, calculates their intersection,
     * and determines if this is a valid intersection.
     *
     * @param lobs - list of Lobs
     * @return list of intersections.
     */
    private List<LobIntersectPoint> findIntersections(List<LobPoint> lobs)
    {
        int pt1Num = 0;
//        int combinations = 0;
//        int nullIntersection = 0;
//        int crsSuccess = 0;
//        int validPairs = 0;

        for (int i = 0; i < lobs.size(); i++)
        {
            LobPoint pt1 = lobs.get(i);
            for (int j = pt1Num + 1; j < lobs.size(); j++)
            {
                LobPoint pt2 = lobs.get(j);
                // Verify the two points are different sites.
                // Verify that the two points are within 1 second, and 1000 mghz
                // of each other.
                if (validPair(pt1, pt2))
                {
                    List<Double> retVals = new ArrayList<>(6);
                    Boolean success = false;

                    if (useCrsIntersect)
                    {
                        success = courseIntersect.calculate(pt1, pt2, 1e-6, retVals);
                    }
                    else
                    {
                        LatLon intersection = LobIntersection.getLobIntersection(pt1, pt2);
                        if (intersection != null)
                        {
                            retVals.add(intersection.getLat());
                            retVals.add(intersection.getLon());

                            double[] vals = vincenty.calculateDistanceBetweenPointsRadians(intersection.getLat(),
                                    intersection.getLon(), pt1.getLat(), pt1.getLon());
                            // azimuth
                            retVals.add(vals[1]);
                            // distance
                            retVals.add(vals[0]);

                            vals = vincenty.calculateDistanceBetweenPointsRadians(intersection.getLat(), intersection.getLon(),
                                    pt2.getLat(), pt2.getLon());
                            // azimuth
                            retVals.add(vals[1]);
                            // distance
                            retVals.add(vals[0]);
                            success = true;
                        }
                    }
                    if (success)
                        validateIntersection(pt1, pt2, retVals.get(0), retVals.get(1), retVals.get(2), retVals.get(3),
                                retVals.get(4), retVals.get(5));
                    // otherwise invalid intersection..drop it
                }
            }
            pt1Num++;
        }
        return intersections;
    }

    private Object getProp(String[] keys, List<String> propKeys, List<?> props)
    {
        for (int i = 0; i < keys.length; i++)
        {
            int ndx = propKeys.indexOf(keys[i]);
            if (ndx > -1)
                return props.get(ndx);
        }
        return null;
    }

    /**
     * Creates instances of LobPoint's from input structure.
     *
     * @param points - job input data
     * @return List of LobPoints
     */
    private List<LobPoint> getLobs(List<IDataPoint> points)
    {
        List<LobPoint> lobs = new ArrayList<>();
        // int badRecords = 0;
        int notDef = 0;
        for (IDataPoint point : points)
        {
            try
            {
                List<String> keys = point.getPropertyKeys();
                List<?> properties = point.getProperties();
                String soiConf = (String)getProp(soiConfColKeys, keys, properties);
                String fof = (String)getProp(fofColKeys, keys, properties);

                if (useConfirmedPtsOnly)
                {
                    if (!soiConf.equals("definite"))
                    {
                        notDef++;
                        // skip this record
                        continue;
                    }
                }
                else if (useFigureOfFitFlag)
                {
                    if (fof == null)
                        // skip this record
                        continue;
                }
                String id = point.getFeatureId();
                double lat = point.getLat();
                double lon = point.getLon();
                double freq = Double.parseDouble((String)getProp(freqColKeys, keys, properties));
                double bearing = Double.parseDouble((String)getProp(bearingColKeys, keys, properties));
                String siteName = (String)getProp(siteColKeys, keys, properties);
                Date time = point.getDate();

                // Note: frequency converted from MHz to Hz
                LobPoint pt = new LobPoint(time, siteName, freq * 1000000, Math.toRadians(lat), Math.toRadians(lon), 0.0,
                        Math.toRadians(bearing), maxRange, id);
                pt.setHasConf(soiConf.equals("definite"));
                lobs.add(pt);
            }
            catch (Throwable e)
            {
                // No action, just ignore this point due to invalid data
                // in a required field.
                // badRecords++;
            }
        }
        LOGGER.info(points.size() + " LOB Points received.");
        LOGGER.info(lobs.size() + " LOB Points used.");
        LOGGER.info(notDef + " LOB Points notDef.");

        return lobs;
    }

    /**
     * Common processing entry point regardless of the data source.
     *
     * @param lobs
     * @return List of convolved ellipse representing intersection points.
     */
    private List<ClusterEllipse> processPoints(List<LobPoint> lobs)
    {
        List<ClusterEllipse> results = null;
        List<LobIntersectPoint> inters = findIntersections(lobs);
        LOGGER.info("Intersections calculated: " + inters.size());
        List<LobIntersectPoint> clusteredInters = null;
        if (inters.size() > 0)
        {
            double latSum = 0;
            for (LobIntersectPoint pt : inters)
                latSum += pt.getLat();

            LobIntersectCluster cluster = new LobIntersectCluster(radiusAllowed, maxDeltaTime, maxDeltaRF, minInCluster);

            double latAvg = latSum / inters.size();
            cluster.cluster(inters, latAvg);
            clusteredInters = filterOnMinSites(inters);
            LOGGER.info("Intersections clustered: " + clusteredInters.size());

            // If we are not using all confirmed lobs, then see if the user
            // requested that there
            // be at least one confirmed lob in each cluster.
            if (!useConfirmedPtsOnly && requireOneConfirmedPt)
            {
                clusteredInters = filterOnConfFlag(clusteredInters);
                LOGGER.info("Intersections in a Clusters with at least 1 confident lob: " + clusteredInters.size());
            }

            if (clusteredInters == null || clusteredInters.isEmpty())
            {
                LOGGER.info("No Intersections found");
            }
            else
            {
                // Write contributing intersections to the dropbox
                String filename = dropbox_location + contributing_output_file + "_" + System.currentTimeMillis() + ".csv";
                File outFile = new File(filename);
                Writer output;
                try
                {
                    output = new BufferedWriter(new FileWriter(outFile));
                    output.write(LobIntersectPoint.getHeader() + "\n");
                    for (LobIntersectPoint point : clusteredInters)
                        output.write(point.toCSV());
                    output.flush();
                    output.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                // Convolve ellipses within a cluster down to 1 ellipse
                results = ConvolveLobXs.convolve(clusteredInters);
                LOGGER.info("Calculated " + results.size() + " convolved ellipses.");
            }
        }
        else
        {
            LOGGER.info("No Intersections found");
        }

        return results;
    }

    /**
     * Generates a list of convolved clustered intersections using data received
     * from a WPS process.
     *
     * @param points List of Lobs
     * @return list of ellipses.
     */
    public List<ClusterEllipse> getCorelatedIntersections(List<IDataPoint> points)
    {
        List<ClusterEllipse> results = null;
        // Read parameters from the Configurator
        init();
        List<LobPoint> lobs = getLobs(points);
        results = processPoints(lobs);
        return results;
    }

    /**
     * Accepts a list of intersections that have had a cluster ID assigned.
     * Navigates through each intersection in the cluster and verifies that at
     * least one contributing LOB to the cluster has a SOI confirmation of
     * "DEFINITE".
     *
     * @param inters clustered intersections ordered by cluster ID.
     * @return List of clusters having at least one confirmed LOB contributing.
     */
    private List<LobIntersectPoint> filterOnConfFlag(List<LobIntersectPoint> inters)
    {
        int clusterId = -1;
        List<LobIntersectPoint> filtered = new ArrayList<>();
        List<LobIntersectPoint> aCluster = new ArrayList<>();
        for (LobIntersectPoint pt : inters)
        {
            if (aCluster.isEmpty())
            {
                // First cluster
                clusterId = inters.get(0).getClusterNumber();
                aCluster.add(pt);
            }
            else if (pt.getClusterNumber() == clusterId)
            {
                // Same cluster
                aCluster.add(pt);
            }
            else
            {
                // New cluster; Determine if previous cluster has a confident
                // lob.
                boolean hasConf = false;
                for (LobIntersectPoint clusteredPt : aCluster)
                    for (LobPoint lobPt : clusteredPt.getLobs())
                        hasConf = hasConf || lobPt.hasConf();

                // If at least 1 contributing lob to this cluster of
                // intersections
                // has a confidence set to true, add the intersections to the
                // filtered results list.
                if (hasConf)
                    for (LobIntersectPoint clusteredPt : aCluster)
                        filtered.add(clusteredPt);

                // Reset lists for the next cluster
                aCluster.clear();
                clusterId = pt.getClusterNumber();
                aCluster.add(pt);
            }
        }

        return filtered;
    }

    /**
     *
     * @param inters
     */
    public List<LobIntersectPoint> filterOnMinSites(List<LobIntersectPoint> inters)
    {
        List<LobIntersectPoint> intersections = new ArrayList<>();

        // Find the largest cluster Id.
        int max = 0;
        for (LobIntersectPoint pt : inters)
            if (pt.getClusterNumber() > max)
                max = pt.getClusterNumber();

        if (max <= 0)
            return intersections;

        for (int clusterId = 1; clusterId <= max; clusterId++)
        {
            // establish site vector that should have at least 3 unique sites
            Set<String> siteNames = new TreeSet<>();
            for (LobIntersectPoint x : inters)
            {
                if (x.getClusterNumber() == clusterId)
                {
                    String[] pieces = x.getName().split("/");
                    // add first site name
                    siteNames.add(pieces[0]);
                    // add second site name
                    siteNames.add(pieces[1]);
                }
            }

            if (siteNames.size() >= minInCluster)
                for (LobIntersectPoint x : inters)
                    if (x.getClusterNumber() == clusterId)
                        intersections.add(x);
        }

        return intersections;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        LobIntersectCalculator lobXCalculator = new LobIntersectCalculator();
        lobXCalculator.readConfigValues(args[0]);
        List<LobPoint> lobs = lobXCalculator.readLobData();

        List<ClusterEllipse> results = lobXCalculator.processPoints(lobs);

        if (results == null || results.isEmpty())
        {
            System.out.println("No Intersections found");
        }
        else
        {
            File outFile3 = new File("/tmp/convolved.csv");
            Writer output3;
            try
            {
                output3 = new BufferedWriter(new FileWriter(outFile3));
                output3.write(results.get(0).getHeader() + "\n");
                for (ClusterEllipse point : results)
                    output3.write(point.toCSV());
                output3.flush();
                output3.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
