package io.opensphere.imagery.transform;

import java.util.List;

import Jama.Matrix;
import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.core.model.LatLonAlt;

/**
 * Find polynomial transforms up to 8th order. 1,2,3 produce nice images.
 * Starting at 4th order you can get alot of jitter in the transform.
 *
 * You MUST sort the GCPs if you want the same transform next time.
 */
public class ImageryTransformNthOrder extends ImageryTransform
{
    /** the algorithm to use to determine the transform. */
    private static int ourMethod = 1;

    // 3rd order transformation
    // X' = a0 + a1x + a2y + a3x^2 + a4y^2 + a5xy
    // Y' = b0 + b1x + b2y + b3x^2 + b4y^2 + b5xy
    // 12 unknowns need 6 GCPs
    // true shape not maintained, parallel lines no longer remain parallel after
    // transform (which is good)

    /**
     * Gets the method.
     *
     * @return the method
     */
    public static int getMethod()
    {
        return ourMethod;
    }

    /**
     * Returns the number of coefficients for a warp order size.
     *
     * @param order the order
     * @return the number of coefficients
     */
    public static int getNumCoefficientsForOrder(int order)
    {
        int result = -1;
        switch (order)
        {
            case 1:
                result = 3;
                break;
            case 2:
                result = 6;
                break;
            case 3:
                result = 10;
                break;
            case 4:
                result = 15;
                break;
            case 5:
                result = 21;
                break;
            case 6:
                result = 28;
                break;
            case 7:
                result = 36;
                break;
            case 8:
                result = 45;
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * Sets the method.
     *
     * @param method the new method
     */
    public static void setMethod(int method)
    {
        ImageryTransformNthOrder.ourMethod = method;
    }

    /**
     * Instantiates a new imagery transform nth order.
     */
    public ImageryTransformNthOrder()
    {
        // default
        setGeoToPixelTransformCoeff(new TransformCoefficients(1));
        setPixelToGeoTransformCoeff(new TransformCoefficients(1));
    }

    /**
     * Instantiates a new imagery transform nth order.
     *
     * @param inorder the in order
     */
    public ImageryTransformNthOrder(int inorder)
    {
        setGeoToPixelTransformCoeff(new TransformCoefficients(1));
        setPixelToGeoTransformCoeff(new TransformCoefficients(1));

        if (inorder >= 1 && inorder <= 8)
        {
            setOrder(inorder);
        }

        switch (getOrder())
        {
            case 1:
                setOrderSize(3);
                break;
            case 2:
                setOrderSize(6);
                break;
            case 3:
                setOrderSize(10);
                break;
            case 4:
                setOrderSize(15);
                break;
            case 5:
                setOrderSize(21);
                break;
            case 6:
                setOrderSize(28);
                break;
            case 7:
                setOrderSize(36);
                break;
            case 8:
                setOrderSize(45);
                break;
            default:
                break;
        }
    }

    /**
     * Apply coeffecients to value.
     *
     * @param a the a
     * @param x the x
     * @param y the y
     * @return the double
     */
    public double applyCoeffecientsToValue(double[] a, double x, double y)
    {
        double ret = a[0] + a[1] * x + a[2] * y;

        // for saving multiplies
        double xx = 0.0;
        double yy = 0.0;
        double xxx = 0.0;
        double yyy = 0.0;
        double xxxx = 0.0;
        double yyyy = 0.0;

        if (getOrder() >= 2)
        {
            xx = x * x;
            yy = y * y;
            ret += a[3] * xx + a[4] * yy + a[5] * x * y;
        }
        if (getOrder() >= 3)
        {
            xxx = xx * x;
            yyy = yy * y;
            if (getOrderSize() > 6)
            {
                ret += a[6] * xxx;
            }
            if (getOrderSize() > 7)
            {
                ret += a[7] * yyy;
            }
            if (getOrderSize() > 8)
            {
                ret += a[8] * xx * y;
            }
            if (getOrderSize() > 9)
            {
                ret += a[9] * x * yy;
            }
        }
        if (getOrder() >= 4)
        {
            xxxx = xx * xx;
            yyyy = yy * yy;

            // ret +=
            // a[10]*xxxx +
            // a[11]*yyyy +
            // a[12]*xxx*y +
            // a[13]*xx*yy +
            // a[14]*x*yyy;
            if (getOrderSize() > 10)
            {
                ret += a[10] * xxxx;
            }
            if (getOrderSize() > 11)
            {
                ret += a[11] * yyyy;
            }
            if (getOrderSize() > 12)
            {
                ret += a[12] * xxx * y;
            }
            if (getOrderSize() > 13)
            {
                ret += a[13] * xx * yy;
            }
            if (getOrderSize() > 14)
            {
                ret += a[14] * x * yyy;
            }
        }
        if (getOrder() >= 5)
        {
            if (getOrderSize() > 15)
            {
                ret += a[15] * xxx * xx;
            }
            if (getOrderSize() > 16)
            {
                ret += a[16] * yyy * yy;
            }
            if (getOrderSize() > 17)
            {
                ret += a[17] * xxxx * y;
            }
            if (getOrderSize() > 18)
            {
                ret += a[18] * xxx * yy;
            }
            if (getOrderSize() > 19)
            {
                ret += a[19] * xx * yyy;
            }
            if (getOrderSize() > 20)
            {
                ret += a[20] * x * yyyy;
            }
        }

        if (getOrder() >= 6)
        {
            if (getOrderSize() > 21)
            {
                ret += a[21] * xxx * xxx;
            }
            if (getOrderSize() > 22)
            {
                ret += a[22] * yyy * yyy;
            }
            if (getOrderSize() > 23)
            {
                ret += a[23] * xxx * xx * y;
            }
            if (getOrderSize() > 24)
            {
                ret += a[24] * xxx * x * yy;
            }
            if (getOrderSize() > 25)
            {
                ret += a[25] * xxx * yyy;
            }
            if (getOrderSize() > 26)
            {
                ret += a[26] * xx * yyyy;
            }
            if (getOrderSize() > 27)
            {
                ret += a[27] * x * yy * yyy;
            }
        }
        if (getOrder() >= 7)
        {
            if (getOrderSize() > 28)
            {
                ret += a[28] * xxxx * xxx;
            }
            if (getOrderSize() > 29)
            {
                ret += a[29] * yyyy * yyy;
            }
            if (getOrderSize() > 30)
            {
                ret += a[30] * xxxx * xx * y;
            }
            if (getOrderSize() > 31)
            {
                ret += a[31] * xxxx * x * yy;
            }
            if (getOrderSize() > 32)
            {
                ret += a[32] * xxxx * yyy;
            }
            if (getOrderSize() > 33)
            {
                ret += a[33] * xxx * yyyy;
            }
            if (getOrderSize() > 34)
            {
                ret += a[34] * xx * yyyy * y;
            }
            if (getOrderSize() > 35)
            {
                ret += a[35] * x * yy * yyyy;
            }
        }
        if (getOrder() >= 8)
        {
            if (getOrderSize() > 36)
            {
                ret += a[36] * xxxx * xxxx;
            }
            if (getOrderSize() > 37)
            {
                ret += a[37] * yyyy * yyyy;
            }
            if (getOrderSize() > 38)
            {
                ret += a[38] * xxxx * xxx * y;
            }
            if (getOrderSize() > 39)
            {
                ret += a[39] * xxxx * xx * yy;
            }
            if (getOrderSize() > 40)
            {
                ret += a[40] * xxxx * x * yyy;
            }
            if (getOrderSize() > 41)
            {
                ret += a[41] * xxxx * yyyy;
            }
            if (getOrderSize() > 42)
            {
                ret += a[42] * xxx * yyyy * y;
            }
            if (getOrderSize() > 43)
            {
                ret += a[43] * xx * yyyy * yy;
            }
            if (getOrderSize() > 44)
            {
                ret += a[44] * x * yyyy * yyy;
            }
        }
        return ret;
    }

    @Override
    public ImageryTransformNthOrder clone()
    {
        ImageryTransformNthOrder aITNO = (ImageryTransformNthOrder)super.clone();
        aITNO.setGCPs(getGCPs());
        aITNO.setGeoToPixelTransformCoeff(getGeoToPixelTransformCoeff().clone());
        aITNO.setPixelToGeoTransformCoeff(getPixelToGeoTransformCoeff().clone());
        return aITNO;
    }

    @Override
    public synchronized void findTransform(List<GroundControlPoint> listOfGCPs)
    {
        setGCPs(GroundControlPoint.cloneList(listOfGCPs));

        sortGCPs(getGCPs());
        toMD5Hash();

        makePixelToGeoTransform(listOfGCPs);
        makeGeoToPixelTransform(listOfGCPs);
        detectAmountOfError(listOfGCPs);
    }

    /**
     * Get the first order transform, packed the same way that GDAL would
     * provide it. Only valid for first order transforms.
     *
     * @return the aDF transform
     */
    @Override
    public double[] getADFTransform()
    {
        if (getOrder() > 1)
        {
            throw new UnsupportedOperationException("Cannot getADFTransform for transforms greater than order 1.");
        }

        double[] gt = new double[6];
        gt[0] = getPixelToGeoTransformCoeff().getXCoefficients()[0];
        gt[1] = getPixelToGeoTransformCoeff().getXCoefficients()[1];
        gt[2] = getPixelToGeoTransformCoeff().getXCoefficients()[2];
        gt[3] = getPixelToGeoTransformCoeff().getYCoefficients()[0];
        gt[4] = getPixelToGeoTransformCoeff().getYCoefficients()[1];
        gt[5] = getPixelToGeoTransformCoeff().getYCoefficients()[2];
        return gt;
    }

    @Override
    public LatLonAlt getLatLonBasedOnTransform(double xPixel, double yPixel)
    {
        return LatLonAlt.createFromDegrees(getYGeoBasedOnTransform(xPixel, yPixel), getXGeoBasedOnTransform(xPixel, yPixel));
    }

    @Override
    public double getXGeoBasedOnTransform(double xpix, double ypix)
    {
        return applyCoeffecientsToValue(getPixelToGeoTransformCoeff().getXCoefficients(), xpix, ypix);
    }

    @Override
    public double getXPixelBasedOnTransform(double xgeo, double ygeo)
    {
        double ret = applyCoeffecientsToValue(getGeoToPixelTransformCoeff().getXCoefficients(), xgeo, ygeo);
        return ret;
    }

    @Override
    public double getYGeoBasedOnTransform(double xpix, double ypix)
    {
        return applyCoeffecientsToValue(getPixelToGeoTransformCoeff().getYCoefficients(), xpix, ypix);
    }

//    /**
//     * Output this file to an octopus script.
//     *
//     * @param stream The stream to which to print.
//     * @param a - Coefficients for printing.
//     */
//    public void toOctopusScript(PrintStream stream, double[] a)
//    {
//        stream.println("a = " + a[0] + ";");
//        stream.println("b = " + a[1] + ";");
//        stream.println("c = " + a[2] + ";");
//
//        if (getOrderSize() > 3)
//        {
//            stream.println("d = " + a[3] + ";");
//        }
//        if (getOrderSize() > 4)
//        {
//            stream.println("e = " + a[4] + ";");
//        }
//        if (getOrderSize() > 5)
//        {
//            stream.println("f = " + a[5] + ";");
//        }
//        if (getOrderSize() > 6)
//        {
//            stream.println("g = " + a[6] + ";");
//        }
//        if (getOrderSize() > 7)
//        {
//            stream.println("h = " + a[7] + ";");
//        }
//        if (getOrderSize() > 8)
//        {
//            stream.println("i = " + a[8] + ";");
//        }
//        if (getOrderSize() > 9)
//        {
//            stream.println("j = " + a[9] + ";");
//        }
//        if (getOrderSize() > 10)
//        {
//            stream.println("k = " + a[10] + ";");
//        }
//        if (getOrderSize() > 11)
//        {
//            stream.println("l = " + a[11] + ";");
//        }
//        if (getOrderSize() > 12)
//        {
//            stream.println("m = " + a[12] + ";");
//        }
//        if (getOrderSize() > 13)
//        {
//            stream.println("n = " + a[13] + ";");
//        }
//        if (getOrderSize() > 14)
//        {
//            stream.println("o = " + a[14] + ";");
//        }
//        if (getOrderSize() > 15)
//        {
//            stream.println("p = " + a[15] + ";");
//        }
//        if (getOrderSize() > 16)
//        {
//            stream.println("q = " + a[16] + ";");
//        }
//        if (getOrderSize() > 17)
//        {
//            stream.println("r = " + a[17] + ";");
//        }
//        if (getOrderSize() > 18)
//        {
//            stream.println("s = " + a[18] + ";");
//        }
//        if (getOrderSize() > 19)
//        {
//            stream.println("t = " + a[19] + ";");
//        }
//        if (getOrderSize() > 20)
//        {
//            stream.println("u = " + a[20] + ";");
//        }
//        if (getOrderSize() > 21)
//        {
//            stream.println("v = " + a[21] + ";");
//        }
//        if (getOrderSize() > 22)
//        {
//            stream.println("w = " + a[22] + ";");
//        }
//        if (getOrderSize() > 23)
//        {
//            stream.println("x = " + a[23] + ";");
//        }
//        if (getOrderSize() > 24)
//        {
//            stream.println("y = " + a[24] + ";");
//        }
//        if (getOrderSize() > 25)
//        {
//            stream.println("z = " + a[25] + ";");
//        }
//        if (getOrderSize() > 26)
//        {
//            stream.println("aa = " + a[26] + ";");
//        }
//        if (getOrderSize() > 27)
//        {
//            stream.println("ab = " + a[27] + ";");
//        }
//        if (getOrderSize() > 28)
//        {
//            stream.println("ac = " + a[28] + ";");
//        }
//        if (getOrderSize() > 29)
//        {
//            stream.println("ad = " + a[29] + ";");
//        }
//        if (getOrderSize() > 30)
//        {
//            stream.println("ae = " + a[30] + ";");
//        }
//        if (getOrderSize() > 31)
//        {
//            stream.println("af = " + a[31] + ";");
//        }
//        if (getOrderSize() > 32)
//        {
//            stream.println("ag = " + a[32] + ";");
//        }
//        if (getOrderSize() > 33)
//        {
//            stream.println("ah = " + a[33] + ";");
//        }
//        if (getOrderSize() > 34)
//        {
//            stream.println("ai = " + a[34] + ";");
//        }
//        if (getOrderSize() > 35)
//        {
//            stream.println("aj = " + a[35] + ";");
//        }
//        if (getOrderSize() > 36)
//        {
//            stream.println("ak = " + a[36] + ";");
//        }
//        if (getOrderSize() > 37)
//        {
//            stream.println("al = " + a[37] + ";");
//        }
//        if (getOrderSize() > 38)
//        {
//            stream.println("am = " + a[38] + ";");
//        }
//        if (getOrderSize() > 39)
//        {
//            stream.println("an = " + a[39] + ";");
//        }
//        if (getOrderSize() > 40)
//        {
//            stream.println("ao = " + a[40] + ";");
//        }
//        if (getOrderSize() > 41)
//        {
//            stream.println("ap = " + a[41] + ";");
//        }
//        if (getOrderSize() > 42)
//        {
//            stream.println("aq = " + a[42] + ";");
//        }
//        if (getOrderSize() > 43)
//        {
//            stream.println("ar = " + a[43] + ";");
//        }
//        if (getOrderSize() > 44)
//        {
//            stream.println("as = " + a[44] + ";");
//        }
//
//        stream.println("[xi,yi] = meshgrid(tx,ty);");
//
//        stream.print("tz = a + b .* xi + c .* yi");
//
//        if (getOrder() >= 2)
//        {
//            if (getOrderSize() > 3)
//            {
//                stream.print(" + d .* xi .* xi");
//            }
//            if (getOrderSize() > 4)
//            {
//                stream.print(" + e .* yi .* yi");
//            }
//            if (getOrderSize() > 5)
//            {
//                stream.print(" + f .* xi .* yi");
//            }
//        }
//        if (getOrder() >= 3)
//        {
//
//            if (getOrderSize() > 6)
//            {
//                stream.print(" + g .* xi .* xi .* xi");
//            }
//            if (getOrderSize() > 7)
//            {
//                stream.print(" + h .* yi .* yi .* yi");
//            }
//            if (getOrderSize() > 8)
//            {
//                stream.print(" + i .* xi .* xi .* yi");
//            }
//            if (getOrderSize() > 9)
//            {
//                stream.print(" + j .* xi .* yi .* yi");
//            }
//
//        }
//        if (getOrder() >= 4)
//        {
//
//            // stream.print(" +
//            // a[10]*xxxx +
//            // a[11]*yyyy +
//            // a[12]*xxx*y +
//            // a[13]*xx*yy +
//            // a[14]*x*yyy");
//            if (getOrderSize() > 10)
//            {
//                stream.print(" + k .* xi .* xi .* xi .* xi");
//            }
//            if (getOrderSize() > 11)
//            {
//                stream.print(" + l .* yi .* yi .* yi .* yi");
//            }
//            if (getOrderSize() > 12)
//            {
//                stream.print(" + m .* xi .* xi .* xi .* yi");
//            }
//            if (getOrderSize() > 13)
//            {
//                stream.print(" + n .* xi .* xi .* yi .* yi");
//            }
//            if (getOrderSize() > 14)
//            {
//                stream.print(" + o .* xi .* yi .* yi .* yi");
//            }
//        }
//        // if (myOrder >= 5)
//        // {
//        // if (getOrderSize() > 15)
//        // stream.print(" + p * x^3 * x^2");
//        // if (getOrderSize() > 16)
//        // stream.print(" + q * y^3 * y*yi");
//        // if (getOrderSize() > 17)
//        // stream.print(" + r * x^4 * y");
//        // if (getOrderSize() > 18)
//        // stream.print(" + s * x^3 * y*yi");
//        // if (getOrderSize() > 19)
//        // stream.print(" + t * x^2 * y^3");
//        // if (getOrderSize() > 20)
//        // stream.print(" + u * x * y^4");
//        // }
//        //
//        // if (myOrder >= 6)
//        // {
//        // if (getOrderSize() > 21)
//        // stream.print(" + v * x^6");
//        // if (getOrderSize() > 22)
//        // stream.print(" + w * y^6");
//        // if (getOrderSize() > 23)
//        // stream.print(" + x * x^5 * y");
//        // if (getOrderSize() > 24)
//        // stream.print(" + y * x^4 * y^2");
//        // if (getOrderSize() > 25)
//        // stream.print(" + z * x^3 * y^3");
//        // if (getOrderSize() > 26)
//        // stream.print(" + aa * x^2 * y^4");
//        // if (getOrderSize() > 27)
//        // stream.print(" + ab * x * y^5");
//        // }
//        //
//        // if (myOrder >= 7)
//        // {
//        // if (getOrderSize() > 28)
//        // stream.print(" + ac * x^7");
//        // if (getOrderSize() > 29)
//        // stream.print(" + ad * y^7");
//        // if (getOrderSize() > 30)
//        // stream.print(" + ae * x^6 * y");
//        // if (getOrderSize() > 31)
//        // stream.print(" + af * x^5 * y^2");
//        // if (getOrderSize() > 32)
//        // stream.print(" + ag * x^4 * y^3");
//        // if (getOrderSize() > 33)
//        // stream.print(" + ah * x^3 * y^4");
//        // if (getOrderSize() > 34)
//        // stream.print(" + ai * x^2 * y^5");
//        // if (getOrderSize() > 35)
//        // stream.print(" + aj * x * y^6");
//        // }
//        //
//        // if (myOrder >= 8)
//        // {
//        // if (getOrderSize() > 36)
//        // stream.print(" + ak * x^8");
//        // if (getOrderSize() > 37)
//        // stream.print(" + al * y^8");
//        // if (getOrderSize() > 38)
//        // stream.print(" + am * x^7 * y");
//        // if (getOrderSize() > 39)
//        // stream.print(" + an * x^6 * y^2");
//        // if (getOrderSize() > 40)
//        // stream.print(" + ao * x^5 * y^3");
//        // if (getOrderSize() > 41)
//        // stream.print(" + ap * x^4 * y^4");
//        // if (getOrderSize() > 42)
//        // stream.print(" + aq * x^3 * y^5");
//        // if (getOrderSize() > 43)
//        // stream.print(" + ar * x^2 * y^6");
//        // if (getOrderSize() > 44)
//        // stream.print(" + as * x * y^7");
//        // }
//
//    }

//    /**
//     * Prints the octopus script.
//     *
//     * @param stream The stream to which to print.
//     * @param printSector the print sector
//     */
//    public void printOctopusScript(PrintStream stream, GeographicBoundingBox printSector)
//    {
//        stream.println("# Geo To Pixel X Coefficients");
//        // stream.println("tx = linspace(-96.8438 , -96.7767 ,45)';");
//        stream.println("tx = linspace(" + printSector.getMinLonD() + " , " + printSector.getMaxLonD() + " ,45)';");
//        // stream.println("ty = linspace(32.7471 ,32.8807 ,45)';");
//        stream.println("ty = linspace(" + printSector.getMinLatD() + " ," + printSector.getMaxLatD() + " ,45)';");
//        toOctopusScript(null, getGeoToPixelTransformCoeff().getXCoefficients());
//
//        stream.println(";");
//        stream.println("mesh(tx,ty,tz);");
//
//        stream.println("# Geo To Pixel Y Coefficients");
//        // stream.println("tx = linspace(-96.8438 , -96.7767 ,45)';");
//        stream.println("tx = linspace(" + printSector.getMinLonD() + " , " + printSector.getMaxLonD() + " ,45)';");
//        // stream.println("ty = linspace(32.7471 ,32.8807 ,45)';");
//        stream.println("ty = linspace(" + printSector.getMinLatD() + " ," + printSector.getMaxLatD() + " ,45)';");
//        toOctopusScript(null, getGeoToPixelTransformCoeff().getYCoefficients());
//
//        stream.println(";");
//        stream.println("mesh(tx,ty,tz);");
//
//        stream.println("# Pixel to Geo X Coefficients");
//        stream.println("tx = linspace(0," + 700 + ",41)';");
//        stream.println("ty = linspace(0," + 600 + ",41)';");
//
//        toOctopusScript(null, getPixelToGeoTransformCoeff().getXCoefficients());
//
//        stream.println(";");
//        stream.println("mesh(tx,ty,tz);");
//
//        stream.println("hold on");
//
//        stream.print("ta = [");
//        for (GroundControlPoint gcp : getGCPs())
//        {
//            stream.print(gcp.getPixel() + " ");
//        }
//        stream.println("];");
//
//        stream.print("tb = [");
//        for (GroundControlPoint gcp : getGCPs())
//        {
//            stream.print(gcp.getLine() + " ");
//        }
//        stream.println("];");
//
//        stream.print("tc = [");
//        for (GroundControlPoint gcp : getGCPs())
//        {
//            stream.print(gcp.getLon() + " ");
//        }
//        stream.println("];");
//        stream.println("scatter3(ta,tb,tc);");
//
//        stream.println("# Pixel to Geo Y Coefficients");
//        stream.println("tx = linspace(0," + 700 + ",41)';");
//        stream.println("ty = linspace(0," + 600 + ",41)';");
//        toOctopusScript(null, getPixelToGeoTransformCoeff().getYCoefficients());
//
//        stream.println(";");
//        stream.println("mesh(tx,ty,tz);");
//
//    }

    @Override
    public double getYPixelBasedOnTransform(double xgeo, double ygeo)
    {
        return applyCoeffecientsToValue(getGeoToPixelTransformCoeff().getYCoefficients(), xgeo, ygeo);
    }

    /**
     * Make geo to pixel transform.
     *
     * @param listOfGCPs the list of gc ps
     */
    public void makeGeoToPixelTransform(List<GroundControlPoint> listOfGCPs)
    {
        int max = listOfGCPs.size();

        if (max < getOrderSize())
        {
            throw new UnsupportedOperationException("Not enough GCPs to support order requestd");
        }

        double[][] array = new double[max][getOrderSize()];
        for (int i = 0; i < max; i++)
        {
            for (int j = 0; j < getOrderSize(); j++)
            {
                double x = listOfGCPs.get(i).getLon();
                double y = listOfGCPs.get(i).getLat();

                array[i][j] = 0.0;

                if (j == 0)
                {
                    array[i][j] = 1.0;
                }
                else if (j == 1)
                {
                    array[i][j] = x;
                }
                else if (j == 2)
                {
                    array[i][j] = y;
                }
                else if (j == 3)
                {
                    array[i][j] = x * x;
                }
                else if (j == 4)
                {
                    array[i][j] = y * y;
                }
                else if (j == 5)
                {
                    // end 2nd
                    array[i][j] = x * y;
                }
                else if (j == 6)
                {
                    array[i][j] = x * x * x;
                }
                else if (j == 7)
                {
                    array[i][j] = y * y * y;
                }
                else if (j == 8)
                {
                    array[i][j] = x * x * y;
                }
                else if (j == 9)
                {
                    // end 3rd
                    array[i][j] = x * y * y;
                }
                else if (j == 10)
                {
                    array[i][j] = x * x * x * x;
                }
                else if (j == 11)
                {
                    array[i][j] = y * y * y * y;
                }
                else if (j == 12)
                {
                    array[i][j] = x * x * x * y;
                }
                else if (j == 13)
                {
                    array[i][j] = x * x * y * y;
                }
                else if (j == 14)
                {
                    // end 4th
                    array[i][j] = x * y * y * y;
                }
                else if (j == 15)
                {
                    array[i][j] = x * x * x * x * x;
                }
                else if (j == 16)
                {
                    array[i][j] = y * y * y * y * y;
                }
                else if (j == 17)
                {
                    array[i][j] = x * x * x * x * y;
                }
                else if (j == 18)
                {
                    array[i][j] = x * x * x * y * y;
                }
                else if (j == 19)
                {
                    array[i][j] = x * x * y * y * y;
                }
                else if (j == 20)
                {
                    array[i][j] = x * y * y * y * y;
                }
                else if (j == 21)
                {
                    array[i][j] = x * x * x * x * x * x;
                }
                else if (j == 22)
                {
                    array[i][j] = y * y * y * y * y * y;
                }
                else if (j == 23)
                {
                    array[i][j] = x * x * x * x * x * y;
                }
                else if (j == 24)
                {
                    array[i][j] = x * x * x * x * y * y;
                }
                else if (j == 25)
                {
                    array[i][j] = x * x * x * y * y * y;
                }
                else if (j == 26)
                {
                    array[i][j] = x * x * y * y * y * y;
                }
                else if (j == 27)
                {
                    array[i][j] = x * y * y * y * y * y;
                }
                else if (j == 28)
                {
                    array[i][j] = x * x * x * x * x * x * x;
                }
                else if (j == 29)
                {
                    array[i][j] = y * y * y * y * y * y * y;
                }
                else if (j == 30)
                {
                    array[i][j] = x * x * x * x * x * x * y;
                }
                else if (j == 31)
                {
                    array[i][j] = x * x * x * x * x * y * y;
                }
                else if (j == 32)
                {
                    array[i][j] = x * x * x * x * y * y * y;
                }
                else if (j == 33)
                {
                    array[i][j] = x * x * x * y * y * y * y;
                }
                else if (j == 34)
                {
                    array[i][j] = x * x * y * y * y * y * y;
                }
                else if (j == 35)
                {
                    array[i][j] = x * y * y * y * y * y * y;
                }
                else if (j == 36)
                {
                    array[i][j] = x * x * x * x * x * x * x * x;
                }
                else if (j == 37)
                {
                    array[i][j] = y * y * y * y * y * y * y * y;
                }
                else if (j == 38)
                {
                    array[i][j] = x * x * x * x * x * x * x * y;
                }
                else if (j == 39)
                {
                    array[i][j] = x * x * x * x * x * x * y * y;
                }
                else if (j == 40)
                {
                    array[i][j] = x * x * x * x * x * y * y * y;
                }
                else if (j == 41)
                {
                    array[i][j] = x * x * x * x * y * y * y * y;
                }
                else if (j == 42)
                {
                    array[i][j] = x * x * x * y * y * y * y * y;
                }
                else if (j == 43)
                {
                    array[i][j] = x * x * y * y * y * y * y * y;
                }
                else if (j == 44)
                {
                    array[i][j] = x * y * y * y * y * y * y * y;
                }
            }
        }

        double[][] latAnswers = new double[max][1];
        for (int i = 0; i < max; i++)
        {
            latAnswers[i][0] = listOfGCPs.get(i).getLine();
        }

        Matrix pixPos = new Matrix(array);
        Matrix lats = new Matrix(latAnswers);

        Matrix latsSolve;

        latsSolve = internalSolve(pixPos, lats);

        double[][] lonAnswers = new double[max][1];
        for (int i = 0; i < max; i++)
        {
            lonAnswers[i][0] = listOfGCPs.get(i).getPixel();
        }

        Matrix lons = new Matrix(lonAnswers);

        Matrix lonsSolve;

        lonsSolve = internalSolve(pixPos, lons);

        double[][] lo = lonsSolve.getArray();
        double[][] la = latsSolve.getArray();

        double[] xcoe = new double[getOrderSize()];
        for (int i = 0; i < getOrderSize(); i++)
        {
            xcoe[i] = lo[i][0];
        }

        double[] ycoe = new double[getOrderSize()];
        for (int i = 0; i < getOrderSize(); i++)
        {
            ycoe[i] = la[i][0];
        }

        getGeoToPixelTransformCoeff().setXCoefficients(xcoe);
        getGeoToPixelTransformCoeff().setYCoefficients(ycoe);
    }

    /**
     * Make pixel to geo transform.
     *
     * @param listOfGCPs the list of gc ps
     */
    public void makePixelToGeoTransform(List<GroundControlPoint> listOfGCPs)
    {
        int max = listOfGCPs.size();

        if (max < getOrderSize())
        {
            throw new UnsupportedOperationException("Not enough GCPs to support order requestd");
        }

        double[][] array = new double[max][getOrderSize()];
        for (int iIndex = 0; iIndex < max; iIndex++)
        {
            for (int jIndexj = 0; jIndexj < getOrderSize(); jIndexj++)
            {
                double x = listOfGCPs.get(iIndex).getPixel();
                double y = listOfGCPs.get(iIndex).getLine();

                array[iIndex][jIndexj] = 0.0;

                if (jIndexj == 0)
                {
                    array[iIndex][jIndexj] = 1.0;
                }
                else if (jIndexj == 1)
                {
                    array[iIndex][jIndexj] = x;
                }
                else if (jIndexj == 2)
                {
                    array[iIndex][jIndexj] = y;
                }
                else if (jIndexj == 3)
                {
                    array[iIndex][jIndexj] = x * x;
                }
                else if (jIndexj == 4)
                {
                    array[iIndex][jIndexj] = y * y;
                }
                else if (jIndexj == 5)
                {
                    // end 2nd
                    array[iIndex][jIndexj] = x * y;
                }
                else if (jIndexj == 6)
                {
                    array[iIndex][jIndexj] = x * x * x;
                }
                else if (jIndexj == 7)
                {
                    array[iIndex][jIndexj] = y * y * y;
                }
                else if (jIndexj == 8)
                {
                    array[iIndex][jIndexj] = x * x * y;
                }
                else if (jIndexj == 9)
                {
                    // end 3rd
                    array[iIndex][jIndexj] = x * y * y;
                }
                else if (jIndexj == 10)
                {
                    array[iIndex][jIndexj] = x * x * x * x;
                }
                else if (jIndexj == 11)
                {
                    array[iIndex][jIndexj] = y * y * y * y;
                }
                else if (jIndexj == 12)
                {
                    array[iIndex][jIndexj] = x * x * x * y;
                }
                else if (jIndexj == 13)
                {
                    array[iIndex][jIndexj] = x * x * y * y;
                }
                else if (jIndexj == 14)
                {
                    // end 4th
                    array[iIndex][jIndexj] = x * y * y * y;
                }
                else if (jIndexj == 15)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x;
                }
                else if (jIndexj == 16)
                {
                    array[iIndex][jIndexj] = y * y * y * y * y;
                }
                else if (jIndexj == 17)
                {
                    array[iIndex][jIndexj] = x * x * x * x * y;
                }
                else if (jIndexj == 18)
                {
                    array[iIndex][jIndexj] = x * x * x * y * y;
                }
                else if (jIndexj == 19)
                {
                    array[iIndex][jIndexj] = x * x * y * y * y;
                }
                else if (jIndexj == 20)
                {
                    array[iIndex][jIndexj] = x * y * y * y * y;
                }
                else if (jIndexj == 21)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x;
                }
                else if (jIndexj == 22)
                {
                    array[iIndex][jIndexj] = y * y * y * y * y * y;
                }
                else if (jIndexj == 23)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * y;
                }
                else if (jIndexj == 24)
                {
                    array[iIndex][jIndexj] = x * x * x * x * y * y;
                }
                else if (jIndexj == 25)
                {
                    array[iIndex][jIndexj] = x * x * x * y * y * y;
                }
                else if (jIndexj == 26)
                {
                    array[iIndex][jIndexj] = x * x * y * y * y * y;
                }
                else if (jIndexj == 27)
                {
                    array[iIndex][jIndexj] = x * y * y * y * y * y;
                }
                else if (jIndexj == 28)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x * x;
                }
                else if (jIndexj == 29)
                {
                    array[iIndex][jIndexj] = y * y * y * y * y * y * y;
                }
                else if (jIndexj == 30)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x * y;
                }
                else if (jIndexj == 31)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * y * y;
                }
                else if (jIndexj == 32)
                {
                    array[iIndex][jIndexj] = x * x * x * x * y * y * y;
                }
                else if (jIndexj == 33)
                {
                    array[iIndex][jIndexj] = x * x * x * y * y * y * y;
                }
                else if (jIndexj == 34)
                {
                    array[iIndex][jIndexj] = x * x * y * y * y * y * y;
                }
                else if (jIndexj == 35)
                {
                    array[iIndex][jIndexj] = x * y * y * y * y * y * y;
                }
                else if (jIndexj == 36)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x * x * x;
                }
                else if (jIndexj == 37)
                {
                    array[iIndex][jIndexj] = y * y * y * y * y * y * y * y;
                }
                else if (jIndexj == 38)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x * x * y;
                }
                else if (jIndexj == 39)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * x * y * y;
                }
                else if (jIndexj == 40)
                {
                    array[iIndex][jIndexj] = x * x * x * x * x * y * y * y;
                }
                else if (jIndexj == 41)
                {
                    array[iIndex][jIndexj] = x * x * x * x * y * y * y * y;
                }
                else if (jIndexj == 42)
                {
                    array[iIndex][jIndexj] = x * x * x * y * y * y * y * y;
                }
                else if (jIndexj == 43)
                {
                    array[iIndex][jIndexj] = x * x * y * y * y * y * y * y;
                }
                else if (jIndexj == 44)
                {
                    array[iIndex][jIndexj] = x * y * y * y * y * y * y * y;
                }
            }
        }

        double[][] latAnswers = new double[max][1];
        for (int i = 0; i < max; i++)
        {
            latAnswers[i][0] = listOfGCPs.get(i).getLat();
        }

        Matrix pixPos = new Matrix(array);
        Matrix lats = new Matrix(latAnswers);

        Matrix latsSolve;

        latsSolve = internalSolve(pixPos, lats);

        double[][] lonAnswers = new double[max][1];
        for (int i = 0; i < max; i++)
        {
            lonAnswers[i][0] = listOfGCPs.get(i).getLon();
        }

        Matrix lons = new Matrix(lonAnswers);

        // T -1 T
        // alpha = (H H) H b

        Matrix lonsSolve;

        lonsSolve = internalSolve(pixPos, lons);

        double[][] loArray = lonsSolve.getArray();
        double[][] laArray = latsSolve.getArray();

        double[] xcoe = new double[getOrderSize()];
        for (int i = 0; i < getOrderSize(); i++)
        {
            xcoe[i] = loArray[i][0];
        }

        double[] ycoe = new double[getOrderSize()];
        for (int i = 0; i < getOrderSize(); i++)
        {
            ycoe[i] = laArray[i][0];
        }

        getPixelToGeoTransformCoeff().setXCoefficients(xcoe);
        getPixelToGeoTransformCoeff().setYCoefficients(ycoe);
    }

    /**
     * Internal solve.
     *
     * @param system the system
     * @param answers the answers
     * @return the matrix
     */
    private Matrix internalSolve(Matrix system, Matrix answers)
    {
        if (ourMethod == 1)
        {
            return system.solve(answers);
        }

        if (ourMethod == 2)
        {
            // functional 1 and 2 but misses 3rd and 4th order
            Matrix pixPosTrans = system.transpose();
            Matrix transposeMult = pixPosTrans.times(system).inverse();
            Matrix c = pixPosTrans.times(answers);
            Matrix d = transposeMult.times(c);
            return d;
        }

        if (ourMethod == 3)
        {
            // METHOD B same as METHOD A
            Matrix a = system.transpose();
            Matrix b = a.times(answers);
            Matrix c = a.times(system);
            Matrix d = c.inverse().times(b);
            return d;
        }

        if (ourMethod == 4)
        {
            // METHOD C similiar to B and A
            Matrix a = system.transpose();
            Matrix b = a.times(answers);
            Matrix c = a.times(system);
            Matrix d = c.inverse().times(b);
            return d;
        }

        if (ourMethod == 5)
        {
            // METHOD D better than above, lots of mirroring
            return system.inverse().times(answers);
        }

        return null;
    }
}
