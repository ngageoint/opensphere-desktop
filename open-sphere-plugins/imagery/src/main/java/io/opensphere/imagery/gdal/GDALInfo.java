package io.opensphere.imagery.gdal;

/******************************************************************************
 * $Id: gdalinfo.java 16360 2009-02-18 20:45:09Z rouault $
 *
 * Name: gdalinfo.java Project: GDAL SWIG Interface Purpose: Java port of
 * gdalinfo application Author: Benjamin Collins, The MITRE Corporation
 *
 * ****************************************************************************
 * Copyright (c) 2009, Even Rouault Copyright (c) 1998, Frank Warmerdam
 * Copyright (c) 2006, The MITRE Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ****************************************************************************/

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.gdal.gdal.Band;
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.GCP;
import org.gdal.gdal.RasterAttributeTable;
import org.gdal.gdal.TermProgressCallback;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.imagery.transform.ImageryTransform;
import io.opensphere.imagery.transform.ImageryTransformFactory;

/**
 * The Class GDALInfo.
 */
@SuppressWarnings({ "PMD.ReplaceVectorWithList", "PMD.UseArrayListInsteadOfVector", "PMD.GodClass", "rawtypes" })
// GDAL uses Vectors
public final class GDALInfo
{
    /**
     * GDALInfoReportCorner.
     *
     * @param hDataset the h dataset
     * @param cornerName the corner_name
     * @param x the x
     * @param y the y
     * @param stringOut the string out
     * @return the point2 d. double
     */
    public static Point2D.Double gdalInfoReportCorner(Dataset hDataset, String cornerName, double x, double y,
            StringBuilder stringOut)
    {
        double dfGeoX = 0.0;
        double dfGeoY = 0.0;
        String pszProjection;
        double[] adfGeoTransform = new double[6];

        stringOut.append(cornerName).append(' ');

        /* -------------------------------------------------------------------- */
        /* Transform the point into georeferenced coordinates. */
        /* -------------------------------------------------------------------- */
        hDataset.GetGeoTransform(adfGeoTransform);
        {
            pszProjection = hDataset.GetProjectionRef();

            dfGeoX = adfGeoTransform[0] + adfGeoTransform[1] * x + adfGeoTransform[2] * y;
            dfGeoY = adfGeoTransform[3] + adfGeoTransform[4] * x + adfGeoTransform[5] * y;
        }

        if (adfGeoTransform[0] == 0 && adfGeoTransform[1] == 0 && adfGeoTransform[2] == 0 && adfGeoTransform[3] == 0
                && adfGeoTransform[4] == 0 && adfGeoTransform[5] == 0)
        {
            stringOut.append('(').append(x).append(',').append(y).append(")\n");
            return null;
        }

        CoordinateTransformation hTransform = null;

        /* -------------------------------------------------------------------- */
        /* Report the georeferenced coordinates. */
        /* -------------------------------------------------------------------- */
        Point2D.Double aPoint = new Point2D.Double(dfGeoX, dfGeoY);
        stringOut.append('(').append(dfGeoX).append(',').append(dfGeoY).append(") ");

        /* -------------------------------------------------------------------- */
        /* Setup transformation to lat/long. */
        /* -------------------------------------------------------------------- */
        if (pszProjection != null && pszProjection.length() > 0)
        {
            SpatialReference srhProj = new SpatialReference(pszProjection);
            SpatialReference hLatLong = srhProj.CloneGeogCS();
            if (hLatLong != null)
            {
                // CPLPushErrorHandler( gdalconstConstants.CPLQuietErrorHandler
                // );
                hTransform = new CoordinateTransformation(srhProj, hLatLong);
                // CPLPopErrorHandler();
                hLatLong.delete();
            }

            srhProj.delete();
        }

        /* -------------------------------------------------------------------- */
        /* Transform to latlong and report. */
        /* -------------------------------------------------------------------- */
        if (hTransform != null)
        {
            double[] transPoint = new double[3];
            hTransform.TransformPoint(transPoint, dfGeoX, dfGeoY, 0);
            stringOut.append('(').append(gdal.DecToDMS(transPoint[0], "Long", 2));
            stringOut.append(',').append(gdal.DecToDMS(transPoint[1], "Lat", 2)).append(')');
        }

        if (hTransform != null)
        {
            hTransform.delete();
        }

        stringOut.append('\n');

        return aPoint;
    }

    //
    // public static String getInfoPrint(Dataset ds)
    // {
    // return null;
    // }

    /**
     * Gets the geo coordinate from pixel.
     *
     * @param hDataset the h dataset
     * @param x the x
     * @param y the y
     * @return the geo coordinate from pixel
     */
    public static Point2D.Double getGeoCoordinateFromPixel(Dataset hDataset, double x, double y)
    {
        double dfGeoX = 0;
        double dfGeoY = 0;
        String pszProjection;
        double[] adfGeoTransform = { 0, 0, 0, 0, 0, 0 };

        /* -------------------------------------------------------------------- */
        /* Transform the point into georeferenced coordinates. */
        /* -------------------------------------------------------------------- */
        pszProjection = hDataset.GetProjectionRef();
        SpatialReference hSRS = new SpatialReference(pszProjection);
        String proj = hSRS.GetAttrValue("GEOGCS");
        if (StringUtils.isNotEmpty(proj))
        {
            hDataset.GetGeoTransform(adfGeoTransform);
            {
                pszProjection = hDataset.GetProjectionRef();

                dfGeoX = adfGeoTransform[0] + adfGeoTransform[1] * x + adfGeoTransform[2] * y;
                dfGeoY = adfGeoTransform[3] + adfGeoTransform[4] * x + adfGeoTransform[5] * y;
            }
        }
        else
        {
            Vector<?> groundControlPoints = new Vector<>();
            hDataset.GetGCPs(groundControlPoints);

            Enumeration<?> e = groundControlPoints.elements();
            List<GroundControlPoint> ar = new ArrayList<>();

            while (e.hasMoreElements())
            {
                GCP gcp = (GCP)e.nextElement();

                GroundControlPoint gcpa = new GroundControlPoint();
                gcpa.setLat(gcp.getGCPY());
                gcpa.setLon(gcp.getGCPX());
                gcpa.setPixel(gcp.getGCPPixel());
                gcpa.setLine(gcp.getGCPLine());

                ar.add(gcpa);
            }

            if (ar.size() >= 3)
            {
                ImageryTransform transform = ImageryTransformFactory.getImageryTransformBestFit(ar);
                return new Point2D.Double(transform.getXGeoBasedOnTransform(x, y), transform.getYGeoBasedOnTransform(x, y));
            }
        }

        CoordinateTransformation hTransform = null;

        /* -------------------------------------------------------------------- */
        /* Report the georeferenced coordinates. */
        /* -------------------------------------------------------------------- */
        Point2D.Double point = new Point2D.Double(dfGeoX, dfGeoY);

        /* -------------------------------------------------------------------- */
        /* Setup transformation to lat/long. */
        /* -------------------------------------------------------------------- */
        if (pszProjection != null && pszProjection.length() > 0)
        {
            SpatialReference hProj = new SpatialReference(pszProjection);
            SpatialReference hLatLong = hProj.CloneGeogCS();
            // CPLPushErrorHandler( gdalconstConstants.CPLQuietErrorHandler
            // );
            hTransform = new CoordinateTransformation(hProj, hLatLong);
            // CPLPopErrorHandler();
            hLatLong.delete();
            hProj.delete();
        }

        /* -------------------------------------------------------------------- */
        /* Transform to latlong and report. */
        /* -------------------------------------------------------------------- */
        if (hTransform != null)
        {
            double[] transPoint = new double[3];
            hTransform.TransformPoint(transPoint, dfGeoX, dfGeoY, 0);
            point = new Point2D.Double(transPoint[0], transPoint[1]);
        }

        if (hTransform != null)
        {
            hTransform.delete();
        }

        return point;
    }

    /**
     * Gets the geo coordinate from pixel as lat lon.
     *
     * @param hDataset the h dataset
     * @param x the x
     * @param y the y
     * @return the geo coordinate from pixel as lat lon
     */
    public static LatLonAlt getGeoCoordinateFromPixelAsLatLon(Dataset hDataset, double x, double y)
    {
        Point2D.Double val = getGeoCoordinateFromPixel(hDataset, x, y);
        return LatLonAlt.createFromDegrees(val.y, val.x);
    }

    /** Show compute min/max. */
    private static final boolean ourComputeMinMax = false;

    /** Show GCPs. */
    private static final boolean ourShowGCPs = true;

    /** Show metadata. */
    private static final boolean ourShowMetadata = true;

    /** Show stats. */
    private static final boolean ourStats = false;

    /** Show approx stats. */
    private static final boolean ourApproxStats = true;

    /** Show color table. */
    private static final boolean ourShowColorTable = true;

    /** Show compute checksum. */
    private static final boolean ourComputeChecksum = false;

    /** Report histos. */
    private static final boolean ourReportHistograms = false;

    /** Show RAT. */
    private static final boolean ourShowRAT = true;

    /**
     * Gets the info print.
     *
     * @param ds the ds
     * @return the info print
     */
    public static String getInfoPrint(Dataset ds)
    {
        StringBuilder stringOut = new StringBuilder(256);
        Dataset hDataset = ds;
        double[] adfGeoTransform = new double[6];
        Vector papszMetadata;

        /* -------------------------------------------------------------------- */
        /* Report general info. */
        /* -------------------------------------------------------------------- */
        reportGeneralInfo(stringOut, hDataset);

        /* -------------------------------------------------------------------- */
        /* Report projection. */
        /* -------------------------------------------------------------------- */
        reportProjection(stringOut, hDataset);

        /* -------------------------------------------------------------------- */
        /* Report Geotransform. */
        /* -------------------------------------------------------------------- */
        reportGeotransform(stringOut, hDataset, adfGeoTransform);

        /* -------------------------------------------------------------------- */
        /* Report GCPs. */
        /* -------------------------------------------------------------------- */
        reportGCPs(stringOut, hDataset);

        /* -------------------------------------------------------------------- */
        /* Report metadata. */
        /* -------------------------------------------------------------------- */
        reportMetadata(stringOut, hDataset);

        /* -------------------------------------------------------------------- */
        /* Report subdatasets. */
        /* -------------------------------------------------------------------- */
        reportSubdatasets(stringOut, hDataset);

        /* -------------------------------------------------------------------- */
        /* Report geolocation. */
        /* -------------------------------------------------------------------- */
        papszMetadata = hDataset.GetMetadata_List("GEOLOCATION");
        if (!papszMetadata.isEmpty())
        {
            stringOut.append("Geolocation:\n");
            Enumeration keys = papszMetadata.elements();
            while (keys.hasMoreElements())
            {
                stringOut.append("  ").append((String)keys.nextElement()).append('\n');
            }
        }

        /* -------------------------------------------------------------------- */
        /* Report RPCs */
        /* -------------------------------------------------------------------- */
        papszMetadata = hDataset.GetMetadata_List("RPC");
        if (!papszMetadata.isEmpty())
        {
            stringOut.append("RPC Metadata:\n");
            Enumeration keys = papszMetadata.elements();
            while (keys.hasMoreElements())
            {
                stringOut.append("  ").append((String)keys.nextElement()).append('\n');
            }
        }

        /* -------------------------------------------------------------------- */
        /* Report corners. */
        /* -------------------------------------------------------------------- */
        stringOut.append("Corner Coordinates:\n");
        gdalInfoReportCorner(hDataset, "Upper Left ", 0.0, 0.0, stringOut);
        gdalInfoReportCorner(hDataset, "Lower Left ", 0.0, hDataset.getRasterYSize(), stringOut);
        gdalInfoReportCorner(hDataset, "Upper Right", hDataset.getRasterXSize(), 0.0, stringOut);
        gdalInfoReportCorner(hDataset, "Lower Right", hDataset.getRasterXSize(), hDataset.getRasterYSize(), stringOut);
        gdalInfoReportCorner(hDataset, "Center     ", hDataset.getRasterXSize() / 2.0, hDataset.getRasterYSize() / 2.0,
                stringOut);

        /* ==================================================================== */
        /* Loop over bands. */
        /* ==================================================================== */
        reportBands(stringOut, hDataset);

        return stringOut.toString();
    }

    /**
     * Gets the info print.
     *
     * @param aFile the a file
     * @return the info print
     */
    public static String getInfoPrint(File aFile)
    {
        return "";
    }

    /**
     * Append an object's string representation to the string builder if the
     * object isn't null.
     *
     * @param stringOut The string builder.
     * @param label The label for the object.
     * @param obj The object.
     */
    private static void appendValue(StringBuilder stringOut, String label, Object obj)
    {
        if (obj != null)
        {
            stringOut.append(label).append(obj).append(' ');
        }
    }

    /**
     * Report on the flags for the band.
     *
     * @param stringOut the output for the report.
     * @param hBand the horizontal band.
     */
    private static void reportBandFlags(StringBuilder stringOut, Band hBand)
    {
        int nMaskFlags = hBand.GetMaskFlags();
        if ((nMaskFlags & (gdalconstConstants.GMF_NODATA | gdalconstConstants.GMF_ALL_VALID)) == 0)
        {
            Band hMaskBand = hBand.GetMaskBand();

            stringOut.append("  Mask Flags: ");
            if ((nMaskFlags & gdalconstConstants.GMF_PER_DATASET) != 0)
            {
                stringOut.append("PER_DATASET ");
            }
            if ((nMaskFlags & gdalconstConstants.GMF_ALPHA) != 0)
            {
                stringOut.append("ALPHA ");
            }
            if ((nMaskFlags & gdalconstConstants.GMF_NODATA) != 0)
            {
                stringOut.append("NODATA ");
            }
            if ((nMaskFlags & gdalconstConstants.GMF_ALL_VALID) != 0)
            {
                stringOut.append("ALL_VALID ");
            }
            stringOut.append('\n');

            if (hMaskBand != null && hMaskBand.GetOverviewCount() > 0)
            {
                int iOverview;

                stringOut.append("  Overviews of mask band: ");
                for (iOverview = 0; iOverview < hMaskBand.GetOverviewCount(); iOverview++)
                {
                    Band hOverview;
                    hOverview = hMaskBand.GetOverview(iOverview);
                    stringOut.append(hOverview.getXSize()).append('x').append(hOverview.getYSize());
                }
                stringOut.append('\n');
            }
        }
    }

    /**
     * Report on the overviews for the band.
     *
     * @param stringOut the output for the report.
     * @param bComputeChecksum show when true.
     * @param hBand the horizontal band.
     */
    private static void reportBandOverviews(StringBuilder stringOut, boolean bComputeChecksum, Band hBand)
    {
        if (hBand.GetOverviewCount() > 0)
        {
            int iOverview;

            stringOut.append("  Overviews: ");
            for (iOverview = 0; iOverview < hBand.GetOverviewCount(); iOverview++)
            {
                Band hOverview;

                if (iOverview != 0)
                {
                    stringOut.append(", ");
                }

                hOverview = hBand.GetOverview(iOverview);
                stringOut.append(hOverview.getXSize()).append('x').append(hOverview.getYSize());
            }
            stringOut.append('\n');

            if (bComputeChecksum)
            {
                stringOut.append("  Overviews checksum: ");
                for (iOverview = 0; iOverview < hBand.GetOverviewCount(); iOverview++)
                {
                    if (iOverview != 0)
                    {
                        stringOut.append(", ");
                    }
                }
                stringOut.append('\n');
            }
        }
    }

    /**
     * Report on the papsz for the band.
     *
     * @param stringOut the output for the report.
     * @param hBand the horizontal band.
     */
    private static void reportBandPapsz(StringBuilder stringOut, Band hBand)
    {
        int i;
        Vector papszCategories = hBand.GetRasterCategoryNames();
        if (!papszCategories.isEmpty())
        {
            stringOut.append("  Categories:\n");
            Enumeration eCategories = papszCategories.elements();
            i = 0;
            while (eCategories.hasMoreElements())
            {
                stringOut.append("    ").append(i).append(": ").append((String)eCategories.nextElement()).append('\n');
                i++;
            }
        }
    }

    /**
     * RAT was an excellent band.
     *
     * @param stringOut the output for the report.
     * @param bShowRAT show when true.
     * @param hBand the horizontal band.
     */
    private static void reportBandRAT(StringBuilder stringOut, boolean bShowRAT, Band hBand)
    {
        RasterAttributeTable rat = hBand.GetDefaultRAT();
        if (bShowRAT && rat != null)
        {
            stringOut.append("<GDALRasterAttributeTable ");
            double[] pdfRow0Min = new double[1];
            double[] pdfBinSize = new double[1];
            if (rat.GetLinearBinning(pdfRow0Min, pdfBinSize))
            {
                stringOut.append("Row0Min=\"").append(pdfRow0Min[0]).append("\" BinSize=\"").append(pdfBinSize[0]).append("\">");
            }
            stringOut.append('\n');
            int colCount = rat.GetColumnCount();
            for (int col = 0; col < colCount; col++)
            {
                stringOut.append("  <FieldDefn index=\"").append(col).append("\">\n" + "    <Name>").append(rat.GetNameOfCol(col))
                        .append("</Name>\n" + "    <Type>").append(rat.GetTypeOfCol(col)).append("</Type>\n" + "    <Usage>")
                        .append(rat.GetUsageOfCol(col)).append("</Usage>\n" + "  </FieldDefn>\n");
            }
            int rowCount = rat.GetRowCount();
            for (int row = 0; row < rowCount; row++)
            {
                stringOut.append("  <Row index=\"").append(row).append("\">\n");
                for (int col = 0; col < colCount; col++)
                {
                    stringOut.append("    <F>").append(rat.GetValueAsString(row, col)).append("</F>\n");
                }
                stringOut.append("  </Row>\n");
            }
            stringOut.append("</GDALRasterAttributeTable>\n");
        }
    }

    /**
     * Report data on the bands.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportBands(StringBuilder stringOut, Dataset hDataset)
    {
        Band hBand;
        int iBand;
        for (iBand = 0; iBand < hDataset.getRasterCount(); iBand++)
        {
            Double[] pass1 = new Double[1];
            Double[] pass2 = new Double[1];

            hBand = hDataset.GetRasterBand(iBand + 1);

            /* if( bSample ) { float[] afSample = new float[10000]; int nCount;
             *
             * nCount = hBand.GetRandomRasterSample( 10000, afSample );
             * System.out.println( "Got " + nCount + " samples." ); } */

            int[] blockXSize = new int[1];
            int[] blockYSize = new int[1];
            hBand.GetBlockSize(blockXSize, blockYSize);
            stringOut.append("Band ").append(iBand + 1).append(" Block=").append(blockXSize[0]).append('x').append(blockYSize[0])
                    .append(" Type=").append(gdal.GetDataTypeName(hBand.getDataType())).append(", ColorInterp=")
                    .append(gdal.GetColorInterpretationName(hBand.GetRasterColorInterpretation())).append('\n');

            String hBandDesc = hBand.GetDescription();
            if (hBandDesc != null && hBandDesc.length() > 0)
            {
                stringOut.append("  Description = ").append(hBandDesc).append('\n');
            }

            hBand.GetMinimum(pass1);
            hBand.GetMaximum(pass2);
            if (pass1[0] != null || pass2[0] != null || ourComputeMinMax)
            {
                stringOut.append("  ");
                appendValue(stringOut, "Min=", pass1[0]);
                appendValue(stringOut, "Max=", pass2[0]);

                if (ourComputeMinMax)
                {
                    double[] adfCMinMax = new double[2];
                    hBand.ComputeRasterMinMax(adfCMinMax, 0);
                    stringOut.append("  Computed Min/Max=").append(adfCMinMax[0]).append(',').append(adfCMinMax[1]);
                }

                stringOut.append('\n');
            }

            reportBandStatistics(stringOut, ourStats, ourApproxStats, ourReportHistograms, hBand);

            if (ourComputeChecksum)
            {
                stringOut.append("  Checksum=").append(hBand.Checksum()).append('\n');
            }

            hBand.GetNoDataValue(pass1);
            appendValue(stringOut, "  NoData Value=", pass1[0]);

            reportBandOverviews(stringOut, ourComputeChecksum, hBand);

            if (hBand.HasArbitraryOverviews())
            {
                stringOut.append("  Overviews: arbitrary\n");
            }

            reportBandFlags(stringOut, hBand);

            if (hBand.GetUnitType() != null && hBand.GetUnitType().length() > 0)
            {
                stringOut.append("  Unit Type: ").append(hBand.GetUnitType()).append('\n');
            }

            reportBandPapsz(stringOut, hBand);

            hBand.GetOffset(pass1);
            if (pass1[0] != null && pass1[0].doubleValue() != 0)
            {
                stringOut.append("  Offset: ").append(pass1[0]);
            }
            hBand.GetScale(pass1);
            if (pass1[0] != null && pass1[0].doubleValue() != 1)
            {
                stringOut.append(",   Scale:").append(pass1[0]).append('\n');
            }

            reportMoreBandPapszStuff(stringOut, ourShowMetadata, ourShowColorTable, hBand);

            reportBandRAT(stringOut, ourShowRAT, hBand);
        }
    }

    /**
     * Report on the statistics for the band.
     *
     * @param stringOut the output for the report.
     * @param bStats show when true.
     * @param bApproxStats show when true.
     * @param bReportHistograms show when true.
     * @param hBand the horizontal band.
     */
    private static void reportBandStatistics(StringBuilder stringOut, boolean bStats, boolean bApproxStats,
            boolean bReportHistograms, Band hBand)
    {
        double[] dfMin = new double[1];
        double[] dfMax = new double[1];
        double[] dfMean = new double[1];
        double[] dfStdDev = new double[1];
        if (hBand.GetStatistics(bApproxStats, bStats, dfMin, dfMax, dfMean, dfStdDev) == gdalconstConstants.CE_None)
        {
            stringOut.append("  Minimum=").append(dfMin[0]).append(", Maximum=").append(dfMax[0]).append(", Mean=")
                    .append(dfMean[0]).append(", StdDev=").append(dfStdDev[0]).append('\n');
        }

        if (bReportHistograms)
        {
            int[][] panHistogram = new int[1][];
            int eErr = hBand.GetDefaultHistogram(dfMin, dfMax, panHistogram, true, new TermProgressCallback());
            if (eErr == gdalconstConstants.CE_None)
            {
                int nBucketCount = panHistogram[0].length;
                stringOut.append("  ").append(nBucketCount).append(" buckets from ").append(dfMin[0]).append(" to ")
                        .append(dfMax[0]).append(":\n\n");
            }
        }
    }

    /**
     * Report the GPCs.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportGCPs(StringBuilder stringOut, Dataset hDataset)
    {
        if (ourShowGCPs && hDataset.GetGCPCount() > 0)
        {
            stringOut.append("GCP Projection = ").append(hDataset.GetGCPProjection()).append('\n');

            int count = 0;
            Vector groundControlPoints = new Vector();
            hDataset.GetGCPs(groundControlPoints);

            Enumeration e = groundControlPoints.elements();
            while (e.hasMoreElements())
            {
                GCP gcp = (GCP)e.nextElement();
                stringOut.append("GCP[").append(count++).append("]: Id=").append(gcp.getId()).append(", Info=")
                        .append(gcp.getInfo()).append("\n    (").append(gcp.getGCPPixel()).append(',').append(gcp.getGCPLine())
                        .append(") (").append(gcp.getGCPX()).append(',').append(gcp.getGCPY()).append(',').append(gcp.getGCPZ())
                        .append(")\n");
            }
        }
    }

    /**
     * Report the general info.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportGeneralInfo(StringBuilder stringOut, Dataset hDataset)
    {
        Driver hDriver;
        Vector papszFileList;
        hDriver = hDataset.GetDriver();
        stringOut.append("Driver: ").append(hDriver.getShortName()).append('/').append(hDriver.getLongName()).append('\n');

        papszFileList = hDataset.GetFileList();
        if (!papszFileList.isEmpty())
        {
            stringOut.append("Files: none associated\n");
        }
        else
        {
            Enumeration e = papszFileList.elements();
            while (e.hasMoreElements())
            {
                stringOut.append("       ").append((String)e.nextElement()).append('\n');
            }
        }

        stringOut.append("Size is ").append(hDataset.getRasterXSize()).append(", ").append(hDataset.getRasterYSize())
                .append('\n');
    }

    /**
     * Report the geotransform.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     * @param adfGeoTransform the geo transform
     */
    private static void reportGeotransform(StringBuilder stringOut, Dataset hDataset, double[] adfGeoTransform)
    {
        hDataset.GetGeoTransform(adfGeoTransform);
        {
            if (adfGeoTransform[2] == 0.0 && adfGeoTransform[4] == 0.0)
            {
                stringOut.append("Origin = (").append(adfGeoTransform[0]).append(',').append(adfGeoTransform[3])
                        .append(")\n" + "Pixel Size = (").append(adfGeoTransform[1]).append(',').append(adfGeoTransform[5])
                        .append(")\n");
            }
            else
            {
                stringOut.append("GeoTransform =\n" + "  ").append(adfGeoTransform[0]).append(", ").append(adfGeoTransform[1])
                        .append(", ").append(adfGeoTransform[2]).append("\n  ").append(adfGeoTransform[3]).append(", ")
                        .append(adfGeoTransform[4]).append(", ").append(adfGeoTransform[5]).append('\n');
            }
        }
    }

    /**
     * Report the metadata items.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportMetadata(StringBuilder stringOut, Dataset hDataset)
    {
        Vector papszMetadata;
        papszMetadata = hDataset.GetMetadata_List("");
        if (ourShowMetadata && !papszMetadata.isEmpty())
        {
            Enumeration keys = papszMetadata.elements();
            stringOut.append("Metadata:\n");
            while (keys.hasMoreElements())
            {
                stringOut.append("  ").append((String)keys.nextElement()).append('\n');
            }
        }

        /* -------------------------------------------------------------------- */
        /* Report "IMAGE_STRUCTURE" metadata. */
        /* -------------------------------------------------------------------- */
        papszMetadata = hDataset.GetMetadata_List("IMAGE_STRUCTURE");
        if (ourShowMetadata && !papszMetadata.isEmpty())
        {
            Enumeration keys = papszMetadata.elements();
            stringOut.append("Image Structure Metadata:\n");
            while (keys.hasMoreElements())
            {
                stringOut.append("  ").append((String)keys.nextElement()).append('\n');
            }
        }
    }

    /**
     * Report on the papsz for the band.
     *
     * @param stringOut the output for the report.
     * @param bShowMetadata show when true.
     * @param bShowColorTable show when true.
     * @param hBand the horizontal band.
     */
    private static void reportMoreBandPapszStuff(StringBuilder stringOut, boolean bShowMetadata, boolean bShowColorTable,
            Band hBand)
    {
        Vector papszMetadata;
        ColorTable hTable;
        papszMetadata = hBand.GetMetadata_List("");
        if (bShowMetadata && !papszMetadata.isEmpty())
        {
            Enumeration keys = papszMetadata.elements();
            stringOut.append("  Metadata:\n");
            while (keys.hasMoreElements())
            {
                stringOut.append("    ").append((String)keys.nextElement()).append('\n');
            }
        }
        if (hBand.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex
                && (hTable = hBand.GetRasterColorTable()) != null)
        {
            int count;

            stringOut.append("  Color Table (").append(gdal.GetPaletteInterpretationName(hTable.GetPaletteInterpretation()))
                    .append(" with ").append(hTable.GetCount()).append(" entries)\n");

            if (bShowColorTable)
            {
                for (count = 0; count < hTable.GetCount(); count++)
                {
                    stringOut.append(' ').append(count).append(": ").append(hTable.GetColorEntry(count)).append('\n');
                }
            }
        }
    }

    /**
     * Report the projection.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportProjection(StringBuilder stringOut, Dataset hDataset)
    {
        if (hDataset.GetProjectionRef() != null)
        {
            SpatialReference hSRS;
            String pszProjection;

            pszProjection = hDataset.GetProjectionRef();

            hSRS = new SpatialReference(pszProjection);
            if (pszProjection.length() != 0)
            {
                String[] pszPrettyWkt = new String[1];

                hSRS.ExportToPrettyWkt(pszPrettyWkt, 0);
                stringOut.append("Coordinate System is:\n");
                stringOut.append(pszPrettyWkt[0]).append('\n');
                // gdal.CPLFree( pszPrettyWkt );
            }
            else
            {
                stringOut.append("Coordinate System is `").append(hDataset.GetProjectionRef()).append("'\n");
            }

            hSRS.delete();
        }
    }

    /**
     * Report the sub-datasets.
     *
     * @param stringOut The output for the report.
     * @param hDataset The data set.
     */
    private static void reportSubdatasets(StringBuilder stringOut, Dataset hDataset)
    {
        Vector papszMetadata;
        papszMetadata = hDataset.GetMetadata_List("SUBDATASETS");
        if (!papszMetadata.isEmpty())
        {
            stringOut.append("Subdatasets:\n");
            Enumeration keys = papszMetadata.elements();
            while (keys.hasMoreElements())
            {
                stringOut.append("  ").append((String)keys.nextElement()).append('\n');
            }
        }
    }

    /**
     * Do not allow instantiation.
     */
    private GDALInfo()
    {
    }
}
