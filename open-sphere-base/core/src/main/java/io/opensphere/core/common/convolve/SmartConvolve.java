package io.opensphere.core.common.convolve;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;

/**
 * A convolver that attempts to smartly cluster in 3D space, isolate and exclude
 * outliers, and then convolve final ellipses. The target user group is
 * non-signals analysts that wish to calculate precision answers from bulk pulls
 * of national databases.
 *
 * TODO explain variable name convention!!!!!!
 */
public class SmartConvolve
{
    /** WGS84 Ellipsoid Constants in KM. */
    private static final double ERU2KM = 6378.137;

    private static final double INVFLATTENING = 298.257223563;

    private static final double PRU2KM = ERU2KM - ERU2KM / INVFLATTENING;

    private static final double ONE_MINUS_FLATTENING_SQ = (1 - 1 / INVFLATTENING) * (1 - 1 / INVFLATTENING);

    private static final double FIRST_ECCENTRICITY_SQ = (ERU2KM * ERU2KM - PRU2KM * PRU2KM) / (ERU2KM * ERU2KM);

    private static final double SECOND_ECCENTRICITY_SQ = (ERU2KM * ERU2KM - PRU2KM * PRU2KM) / (PRU2KM * PRU2KM);

    /** Constant for calculating 95% error ellipse */
    static private double SCALEFACTOR_95_2D = 2.4477;

    static private double SCALEFACTOR_95_2D_SQ = SCALEFACTOR_95_2D * SCALEFACTOR_95_2D;

    static private double SCALEFACTOR_95_3D = 2.795;

    /** Constants for converting Lat/Lon degrees into miss-distances in km **/
    static private double ARCMIN_PER_DEGREE = 60.0;

    /**
     * An arc minute of latitude is a nautical mile; hence the familiar 1.852
     */
    static private double KM_PER_ARCMIN = 1.852;

    /** Access to the colt linear algebra routines. */
    private final Algebra mat = new Algebra();

    private final double myEllipseNormThreshold;

    private final double myElevationUncertainty;

    private final boolean myUseModifiedEllipses;

    private final double myMinEllipseAxis;

    /**
     * Default Constructor
     *
     * @param ellipseNormThreshold
     */
    public SmartConvolve(double ellipseNormThreshold, double elevationUncertainty)
    {
        this(ellipseNormThreshold, elevationUncertainty, true, 0.01);
    }

    public SmartConvolve(double ellipseNormThreshold, double elevationUncertainty, boolean useModifiedEllipses,
            double minEllipseAxis)
    {
        myEllipseNormThreshold = ellipseNormThreshold;
        myElevationUncertainty = elevationUncertainty;
        myUseModifiedEllipses = useModifiedEllipses;
        myMinEllipseAxis = minEllipseAxis;
    }

    private Position lla_to_ecef(double lat, double lon, double alt)
    {
        Position returnPoint = null;

        // Do math
        double phi = Math.toRadians(lat);
        double lamda = Math.toRadians(lon);

        double coslamda = Math.cos(lamda);
        double sinlamda = Math.sin(lamda);
        double cosphi = Math.cos(phi);
        double sinphi = Math.sin(phi);

        // Assumes altitude is relative to surface of the earth.
        double denom = Math.sqrt(cosphi * cosphi + ONE_MINUS_FLATTENING_SQ * sinphi * sinphi);
        double semiMajOverDenom = ERU2KM / denom;
        double r = (semiMajOverDenom + alt) * cosphi;
        double s = (semiMajOverDenom * ONE_MINUS_FLATTENING_SQ + alt) * sinphi;
        returnPoint = new Position(r * coslamda, r * sinlamda, s);

        return returnPoint;
    }

    private Position ecef_to_lla(double x, double y, double z)
    {
        Position returnPoint = null;

        // Do math
        double phi = Math.sqrt(x * x + y * y);
        double lamda = Math.atan2(ERU2KM * z, PRU2KM * phi);
        double sinlamda = Math.sin(lamda);
        double coslamda = Math.cos(lamda);
        double lon = Math.atan2(y, x);
        double lat = Math.atan2(z + SECOND_ECCENTRICITY_SQ * PRU2KM * sinlamda * sinlamda * sinlamda,
                phi - FIRST_ECCENTRICITY_SQ * ERU2KM * coslamda * coslamda * coslamda);
        double sinLat = Math.sin(lat);
        double cosLat = Math.cos(lat);
        double denom = Math.sqrt(1 - FIRST_ECCENTRICITY_SQ * sinLat * sinLat);
        double n = ERU2KM / denom;
        // Returns altitude relative to the ellipsoid
        double height = phi / cosLat - n;

        returnPoint = new Position(Math.toDegrees(lon), Math.toDegrees(lat), height);

        return returnPoint;
    }

    /**
     * Compute transformation matrices for enu-xyz conversion. ENU is a
     * Cartesian coordinate system whose origin is the center of the Earth. It
     * is a rotated ECEF system. The E stands for East. The N stands for North.
     * The U stands for Up. The E component is the unit vector pointing East
     * relative to the emitter's position on Earth. The N component is the unit
     * vector pointing North relative to the emitter's position on Earth. The U
     * component is the unit vector pointing Up with respect to the surface of
     * the Earth.
     *
     * @param convolvedPoint 1x3 Matrix of the position in ECEF (xyz)
     * @param lat The altitude of the position in km (lla)
     * @param enpl Not sure yet.
     *
     * @return the transformation matrix
     */
    private DoubleMatrix2D computeEnuTransform(DoubleMatrix1D convolvedPoint, double alt)
    {
        DoubleMatrix2D returnMatrix = new DenseDoubleMatrix2D(3, 3);
        // local vertical correction factor
        double lvc;
        // East
        DoubleMatrix1D e = new DenseDoubleMatrix1D(3);
        // North
        DoubleMatrix1D n = new DenseDoubleMatrix1D(3);
        // Up
        DoubleMatrix1D u = new DenseDoubleMatrix1D(3);

        // Compute ENU matrix. First U = radius vector.
        u.assign(convolvedPoint);

        // Compute altitude correction from surface of earth
        lvc = (ERU2KM + alt) / (PRU2KM + alt);
        lvc *= lvc;
        u.setQuick(2, u.get(2) * lvc);

        // Normalize U vector.
        u.assign(Mult.div(Math.sqrt(mat.norm2(u))));

        // Compute E = Z x U. Normalize.
        e.setQuick(0, -u.get(1));
        e.setQuick(1, u.get(0));
        e.setQuick(2, 0.0);
        e.assign(Mult.div(Math.sqrt(mat.norm2(e))));

        // Compute N = U x E.
        // TODO Surely Colt has a better way to do cross product.
        // x_prod(u, e, n);
        n.setQuick(0, u.get(1) * e.get(2) - u.get(2) * e.get(1));
        n.setQuick(1, u.get(2) * e.get(0) - u.get(0) * e.get(2));
        n.setQuick(2, u.get(0) * e.get(1) - u.get(1) * e.get(0));

        // Place e, n, u into enu matrix
        for (int i = 0; i < 3; i++)
        {
            returnMatrix.setQuick(i, 0, e.get(i));
            returnMatrix.setQuick(i, 1, n.get(i));
            returnMatrix.setQuick(i, 2, u.get(i));
        }

        return returnMatrix;
    }

    /**
     * Computes the 95% error ellipse. Ellipse units are
     * Kilometers/Kilometers/DegreesClockwiseFromNorth.
     *
     * @param atwai_enu_1sig
     * @param convolvedPoint
     * @param sCALEFACTOR_95_2D2
     * @return
     */
    private Ellipse compute95ell(DoubleMatrix2D atwai_enu_1sig, Ellipse convolvedPoint, double multiplier)
    {
        Ellipse returnEllipse = convolvedPoint;

        double tr = atwai_enu_1sig.get(0, 0) + atwai_enu_1sig.get(1, 1);
        double p2 = atwai_enu_1sig.get(1, 1) - atwai_enu_1sig.get(0, 0);

        // Compute semiaxes squared
        double emaxs = .5 * (tr + Math.sqrt(p2 * p2 + 4.0 * atwai_enu_1sig.get(0, 1) * atwai_enu_1sig.get(0, 1)));
        double emins = tr - emaxs;

        // Simple zero check
        if (emins < 0.0)
        {
            emins = 0.0;
        }

        // Compute semimajor axis (in kilometers)
        double sma = Math.sqrt(emaxs) * multiplier;
        returnEllipse.setSMA(sma);

        // Compute semiminor axis (in kilometers)
        double smi = Math.sqrt(emins) * multiplier;
        returnEllipse.setSMI(smi);

        // Compute orientation angle (in degress from North)
        double orientation = Math.toDegrees(Math.atan2(2.0 * atwai_enu_1sig.get(0, 1), p2) * .5);
        returnEllipse.setOrientation(orientation);

        // Compute Altitude Uncertainty (in kilometers)
        double altUncer = Math.sqrt(atwai_enu_1sig.get(2, 2)) * multiplier;
        returnEllipse.setAltitudeUncertainty(altUncer);

        EigenvalueDecomposition eig = new EigenvalueDecomposition(atwai_enu_1sig);
        DoubleMatrix2D D = eig.getD();

        // Find the longest Axis
        double max = 0;
        for (int i = 0; i < 3; i++)
        {
            if (D.get(i, i) > max)
            {
                max = D.get(i, i);
            }
        }

        // This is the 95% Max Axis Length.
        double maxAxisLength = Math.sqrt(max) * multiplier;
        returnEllipse.setMaxAxisLength(maxAxisLength);

        return returnEllipse;
    }

    /**
     * Calculate the convolved point from the input list. See Bart Peters for
     * explanation of how this calculation works (Add reference to Paper). For
     * smart clustering, call this and the outlier detection continuously until
     * a valid point is returned with no outliers.
     *
     * The returned position does not contain an ellipse, but the entries in the
     * inputEllipses list and the returnedEllipse are updated with all the info
     * required to calculate the ellipse.
     *
     * NOTE: This function is made public for more control of the intermediate
     * steps. The outlierCheck and calculateEllipse functions are dependent upon
     * calculatePostion and eachother. For usage example, see the "calculate"
     * function.
     *
     * @param inputEllipses List of contributing ellipses, outliers rejected
     *            upon return.
     * @param outputEllipses List of geos that were rejected from this ellipse
     * @return A bit of a hack, but a pair representing the resulting Ellipse
     *         for this calculation represented by the point, and t
     */
    public Ellipse calculatePosition(List<Ellipse> inputEllipses)
    {
        Ellipse returnEllipse = null;

        // if we are not using modified ellipses in convolution, go through the
        // input list and discard any ellipses which would be modified; this is
        // inefficient, but is vastly simpler than trying to handle excluding
        // some ellipses during the actual processing
        List<Ellipse> ellipseList;
        if (myUseModifiedEllipses)
        {
            ellipseList = inputEllipses;
        }
        else
        {
            ellipseList = new ArrayList<>();
            for (Ellipse ellipse : inputEllipses)
            {
                if (ellipse.getSMA() >= myMinEllipseAxis && ellipse.getSMI() >= myMinEllipseAxis)
                {
                    ellipseList.add(ellipse);
                }
            }
        }

        // Capital letters represent Sums over the set, lower case represents
        // per point
        DoubleMatrix2D A = new SparseDoubleMatrix2D(ellipseList.size() * 3, 3);
        DoubleMatrix2D ATW = new SparseDoubleMatrix2D(3, ellipseList.size() * 3);
        DoubleMatrix1D y = new DenseDoubleMatrix1D(ellipseList.size() * 3);

        Iterator<Ellipse> iter = ellipseList.iterator();
        for (int i = 0; i < ellipseList.size() && iter.hasNext(); i++)
        {
            Ellipse input = iter.next();
            Position ecefPoint = lla_to_ecef(input.getLat(), input.getLon(), input.getAlt());

            // A is identity matrix
            A.setQuick(i * 3, 0, 1);
            A.setQuick(i * 3 + 1, 1, 1);
            A.setQuick(i * 3 + 2, 2, 1);

            // Setup ATW Matrix

            // Calculate atwa if needed
            DoubleMatrix2D atwa = input.getAtwa();
            if (atwa == null)
            {
                Double sigma1 = input.getSMI();
                if (sigma1 < myMinEllipseAxis)
                {
                    sigma1 = myMinEllipseAxis;
                }
                Double sigma2 = input.getSMA();
                if (sigma2 < myMinEllipseAxis)
                {
                    sigma2 = myMinEllipseAxis;
                }
                Double theta = Math.toRadians(input.getOrientation());

                // Start with temporary matrix, we'll need to invert this later.
                DoubleMatrix2D atwai_enu = new SparseDoubleMatrix2D(3, 3);

                // First row, column:
                // For now, I'm going to assume that Math.pow to 2 is faster
                // than Math.sin or Math.cos
                double zero_zero = sigma1 * sigma1 * Math.pow(Math.cos(theta), 2)
                        + sigma2 * sigma2 * Math.pow(Math.sin(theta), 2);
                atwai_enu.setQuick(0, 0, zero_zero);

                // First row, second column:
                double zero_one = (sigma2 * sigma2 - sigma1 * sigma1) * Math.cos(theta) * Math.sin(theta);
                atwai_enu.setQuick(0, 1, zero_one);

                // Second row, first column; is the same as 0,1
                atwai_enu.setQuick(1, 0, zero_one);

                // Second row, second column; slightly different from 0,0
                double one_one = sigma1 * sigma1 * Math.pow(Math.sin(theta), 2) + sigma2 * sigma2 * Math.pow(Math.cos(theta), 2);
                atwai_enu.setQuick(1, 1, one_one);

                double elevationTolerance = myElevationUncertainty * SCALEFACTOR_95_2D;
                double elevationvariance = elevationTolerance * elevationTolerance;
                atwai_enu.setQuick(2, 2, elevationvariance);

                // Swap from enu coordinate plane to xyz
                Position pos = lla_to_ecef(input.getLat(), input.getLon(), input.getAlt());
                DoubleMatrix1D posecef = new DenseDoubleMatrix1D(3);
                posecef.setQuick(0, pos.getX());
                posecef.setQuick(1, pos.getY());
                posecef.setQuick(2, pos.getZ());
                DoubleMatrix2D enu = computeEnuTransform(posecef, input.getAlt());
                DoubleMatrix2D enut = mat.transpose(enu);

                DoubleMatrix2D dummy = mat.mult(atwai_enu, enut);
                DoubleMatrix2D atwai = mat.mult(enu, dummy);

                // Inverse atwai and save for future passes
                atwa = mat.inverse(atwai);
                input.setAtwa(atwa);
            }

            ATW.setQuick(0, i * 3, atwa.get(0, 0));
            ATW.setQuick(0, i * 3 + 1, atwa.get(0, 1));
            ATW.setQuick(0, i * 3 + 2, atwa.get(0, 2));
            ATW.setQuick(1, i * 3, atwa.get(1, 0));
            ATW.setQuick(1, i * 3 + 1, atwa.get(1, 1));
            ATW.setQuick(1, i * 3 + 2, atwa.get(1, 2));
            ATW.setQuick(2, i * 3, atwa.get(2, 0));
            ATW.setQuick(2, i * 3 + 1, atwa.get(2, 1));
            ATW.setQuick(2, i * 3 + 2, atwa.get(2, 2));

            // Setup Y
            y.setQuick(i * 3, ecefPoint.getX());
            y.setQuick(i * 3 + 1, ecefPoint.getY());
            y.setQuick(i * 3 + 2, ecefPoint.getZ());

        }

        DoubleMatrix2D ATWA = mat.mult(ATW, A);
        DoubleMatrix1D ATWy = mat.mult(ATW, y);

        DoubleMatrix2D ATWAI = mat.inverse(ATWA);

        // Do geo
        DoubleMatrix1D convolvedPoint = mat.mult(ATWAI, ATWy);
        returnEllipse = new Ellipse(ecef_to_lla(convolvedPoint.get(0), convolvedPoint.get(1), convolvedPoint.get(2)));

        // Save ATWAI for error ellipse calculation
        returnEllipse.setATWAI(ATWAI);

        return returnEllipse;
    }

    /**
     * Check for outliers within the input list as determined from the input
     * position.
     *
     * NOTE: This function is made public for more control of the intermediate
     * steps. The outlierCheck and calculateEllipse functions are dependent upon
     * calculatePostion and eachother. For usage example, see the "calculate"
     * function.
     *
     * @param startPos The position to calculate distance from, in lat/lon/alt
     *            coordinate space. Nominally this is the returned ellipse from
     *            "calculatePosition"
     * @param inputEllipses List of contributing ellipses, outlier rejected upon
     *            return.
     * @return Ellipse The outlier Ellipse, or null if no outlier found.
     */
    public Ellipse outlierCheck(Ellipse startPos, List<Ellipse> inputEllipses)
    {
        Ellipse returnEllipse = null;

        // Back to ecef
        Position pos = lla_to_ecef(startPos.getLat(), startPos.getLon(), startPos.getAlt());
        DoubleMatrix1D startPoint = new DenseDoubleMatrix1D(3);
        startPoint.setQuick(0, pos.getX());
        startPoint.setQuick(1, pos.getY());
        startPoint.setQuick(2, pos.getZ());

        DoubleMatrix1D delta = new DenseDoubleMatrix1D(3);
        DoubleMatrix1D miss = new DenseDoubleMatrix1D(inputEllipses.size());
        Iterator<Ellipse> iter2 = inputEllipses.iterator();
        double maxNorm = 0;
        Ellipse maxNormEllipse = null;
        for (int i = 0; i < inputEllipses.size() && iter2.hasNext(); i++)
        {
            Ellipse inputEllipse = iter2.next();

            // if we are not using modified ellipses in convolution, report any
            // ellipse which would be modified as an outlier
            if (!myUseModifiedEllipses && (inputEllipse.getSMA() < myMinEllipseAxis || inputEllipse.getSMI() < myMinEllipseAxis))
            {
                // make sure the logic outside the loop will remove this ellipse
                maxNorm = myEllipseNormThreshold + 1;
                maxNormEllipse = inputEllipse;

                // short circuit the rest of the loop
                break;
            }

            // Find distance in 3D space
            Position inputPosition = lla_to_ecef(inputEllipse.getLat(), inputEllipse.getLon(), inputEllipse.getAlt());

            delta.setQuick(0, startPoint.get(0) - inputPosition.getX());
            delta.setQuick(1, startPoint.get(1) - inputPosition.getY());
            delta.setQuick(2, startPoint.get(2) - inputPosition.getZ());

            miss.setQuick(i, Math.sqrt(delta.get(0) * delta.get(0) + delta.get(1) * delta.get(1) + delta.get(2) * delta.get(2)));

            DoubleMatrix1D dummy1 = mat.mult(inputEllipse.getAtwa(), delta);

            // Calculate norm
            double norm2 = mat.mult(delta, dummy1);

            // Save norm2 for ellipse calculation
            inputEllipse.setNorm2(norm2);

            double norm = Math.sqrt(norm2);

            if (norm > maxNorm)
            {
                maxNorm = norm;
                maxNormEllipse = inputEllipse;
            }

            // Calculate s (3x3)
            DoubleMatrix2D s = new DenseDoubleMatrix2D(3, 3);
            s.setQuick(0, 0, delta.get(0) * delta.get(0));
            s.setQuick(0, 1, delta.get(0) * delta.get(1));
            s.setQuick(1, 0, delta.get(0) * delta.get(1));
            s.setQuick(0, 2, delta.get(0) * delta.get(2));
            s.setQuick(2, 0, delta.get(0) * delta.get(2));
            s.setQuick(1, 1, delta.get(1) * delta.get(1));
            s.setQuick(1, 2, delta.get(1) * delta.get(2));
            s.setQuick(2, 1, delta.get(1) * delta.get(2));
            s.setQuick(2, 2, delta.get(2) * delta.get(2));

            inputEllipse.setS(s);
        }

        // The units for this is ellipse norm. (1.0 is on the ellipse, 0 is at
        // the center).
        if (maxNorm > myEllipseNormThreshold
                && maxNormEllipse != null /* should always be true */)
        {
            inputEllipses.remove(maxNormEllipse);
            returnEllipse = maxNormEllipse;
        }

        return returnEllipse;
    }

    /**
     * Computes the 95% error ellipse for the final convolved position, from the
     * saved ATWAI calculated in "calculatePostion" and the "s" factors
     * determined in "outlierCheck". This function will not work without having
     * previously called those two functions.
     *
     * NOTE: This function is made public for more control of the intermediate
     * steps. The outlierCheck and calculateEllipse functions are dependent upon
     * calculatePostion and each other. For usage example, see the "calculate"
     * function.
     *
     * @param convolvedPosition The position to calculate distance from, in
     *            lat/lon/alt coordinate space. Nominally this is the returned
     *            ellipse from "calculatePosition"
     * @param inputEllipses List of contributing ellipses, with intermediate
     *            calculations from "outlierCheck"
     * @return Ellipse The final ellipse, with fully populated
     *         sma/smi/orientation.
     */
    public Ellipse calculateErrorEllipse(Ellipse convolvedPosition, List<Ellipse> inputEllipses)
    {
        Ellipse returnEllipse = null;

        // Compute Error ellipse for this answer.

        // Back to ecef
        Position pos = lla_to_ecef(convolvedPosition.getLat(), convolvedPosition.getLon(), convolvedPosition.getAlt());
        DoubleMatrix1D ecefPoint = new DenseDoubleMatrix1D(3);
        ecefPoint.setQuick(0, pos.getX());
        ecefPoint.setQuick(1, pos.getY());
        ecefPoint.setQuick(2, pos.getZ());

        double x2 = 0f;

        DoubleMatrix2D Ssample = new DenseDoubleMatrix2D(3, 3);
        DoubleMatrix2D Ssmi = new DenseDoubleMatrix2D(3, 3);
        for (Ellipse ell : inputEllipses)
        {
            // This is Chi-Square of the 95% error ellipse (NOT 1-sigma).
            // The expected value of sum(x2) here is n/2.4477^2, so it will be
            // scaled later
            x2 += ell.getNorm2();

            // This is the 1-sigma (NOT 95%) sample covariance. When S was
            // constructed in
            // {@link outlierCheck}, no scaling was used.
            Ssample = Ssample.assign(ell.getS(), PlusMult.plusMult(1));

            // Calculate East and North miss-distance between input point and
            // convolved point (in km)
            double aveLat = (ell.getLat() + convolvedPosition.getLat()) / 2.0;
            double eastMiss = (ell.getLon() - convolvedPosition.getLon()) * ARCMIN_PER_DEGREE * KM_PER_ARCMIN
                    * Math.cos(Math.toRadians(aveLat));
            double northMiss = (ell.getLat() - convolvedPosition.getLat()) * ARCMIN_PER_DEGREE * KM_PER_ARCMIN;

            double orient = Math.toRadians(ell.getOrientation());

            // Calculate SMI miss between input point and convolved point
            double smiMiss = eastMiss * Math.cos(orient) - northMiss * Math.sin(orient);

            double deltaEastSmi = Math.cos(orient) * smiMiss;
            double deltaNorthSmi = -Math.sin(orient) * smiMiss;
            double deltaUpSmi = ell.getAlt() - convolvedPosition.getAlt();

            // Calculate Ssmi (3x3)
            // Ssmi is the sample covariance of the SMI miss for the input
            // points
            DoubleMatrix2D ssmi = new DenseDoubleMatrix2D(3, 3);
            ssmi.setQuick(0, 0, deltaEastSmi * deltaEastSmi);
            ssmi.setQuick(0, 1, deltaEastSmi * deltaNorthSmi);
            ssmi.setQuick(1, 0, deltaEastSmi * deltaNorthSmi);
            ssmi.setQuick(0, 2, deltaEastSmi * deltaUpSmi);
            ssmi.setQuick(2, 0, deltaEastSmi * deltaUpSmi);
            ssmi.setQuick(1, 1, deltaNorthSmi * deltaNorthSmi);
            ssmi.setQuick(1, 2, deltaNorthSmi * deltaUpSmi);
            ssmi.setQuick(2, 1, deltaNorthSmi * deltaUpSmi);
            ssmi.setQuick(2, 2, deltaUpSmi * deltaUpSmi);

            Ssmi = Ssmi.assign(ssmi, PlusMult.plusMult(1));
        }

        int n = inputEllipses.size();
        int n2 = n * n;

        // Scale x2 to be 1-sigma
        double x2_1sig = x2 * SCALEFACTOR_95_2D_SQ;

        // ATWAI is 95%
        DoubleMatrix2D ATWAIclone = convolvedPosition.getATWAI().copy();

        double x2_inflation_factor = (x2_1sig + 1) / n;

        // Scale the model covariance to 1-sigma (by dividing by
        // SCALEFACTOR_95_2D_SQ)
        // AND inflate the model covariance by multiplying by the 1-sigma
        // chi-square statistic
        DoubleMatrix2D Sinflatedmodel = ATWAIclone.assign(Mult.mult(x2_inflation_factor / SCALEFACTOR_95_2D_SQ));

        // Sinflatedmodel is in XYZ
        // Ssmi is in ENU
        // Rotate Sinflatedmodel to ENU before adding to Ssmi
        DoubleMatrix2D enu = computeEnuTransform(ecefPoint, convolvedPosition.getAlt());
        DoubleMatrix2D enut = mat.transpose(enu);

        DoubleMatrix2D dummy = mat.mult(Sinflatedmodel, enu);
        DoubleMatrix2D Sinflatedmodel_enu = mat.mult(enut, dummy);

        DoubleMatrix2D Sfinal_enu = Sinflatedmodel_enu.assign(Ssmi, PlusMult.plusDiv(n2));

        returnEllipse = compute95ell(Sfinal_enu, convolvedPosition, SCALEFACTOR_95_2D);

        // Capture other values from convolvedPosition, in case needed later
        returnEllipse.setAtwa(convolvedPosition.getAtwa());

        return returnEllipse;
    }

    public Ellipse enlargeEllipseForAltVariance(Ellipse ell, double[] alts)
    {
        double maxAltDev = 0;
        for (double d : alts)
        {
            double dev = Math.abs(d - ell.getAlt());
            if (dev > maxAltDev)
            {
                maxAltDev = dev;
            }
        }
        double factor = maxAltDev / ell.getAltitudeUncertainty();

        // Cap between 2.5 and 1
        factor = Math.min(factor, 2.5);
        factor = Math.max(factor, 1);

        ell.setSMA(ell.getSMA() * factor);
        ell.setSMI(ell.getSMI() * factor);
        ell.setAltitudeUncertainty(ell.getAltitudeUncertainty() * factor);
        ell.setMaxAxisLength(ell.getMaxAxisLength() * factor);

        return ell;
    }
}
