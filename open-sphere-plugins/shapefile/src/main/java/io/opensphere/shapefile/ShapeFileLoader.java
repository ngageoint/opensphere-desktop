package io.opensphere.shapefile;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osrConstants;

import io.opensphere.core.Toolbox;
import io.opensphere.core.common.shapefile.shapes.MultiPointRecord;
import io.opensphere.core.common.shapefile.shapes.PointRecord;
import io.opensphere.core.common.shapefile.shapes.PolyLineRecord;
import io.opensphere.core.common.shapefile.shapes.PolygonRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord;
import io.opensphere.core.common.shapefile.shapes.ShapeRecord.ShapeType;
import io.opensphere.core.common.shapefile.utils.ShapefileRecord;
import io.opensphere.core.common.shapefile.v2.ESRIShapefile;
import io.opensphere.core.common.shapefile.v2.projection.ProjectionPortion;
import io.opensphere.core.dialog.alertviewer.event.UserMessageEvent;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.gdal.GDALGenericUtilities;
import io.opensphere.core.util.model.GeographicUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.AbstractMapGeometrySupport;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.impl.AbstractMapPathGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapLineOfBearingGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;
import io.opensphere.mantle.util.ProgressDialog;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class ShapeFileLoader.
 */
@SuppressWarnings("PMD.GodClass")
public class ShapeFileLoader
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileLoader.class);

    /** A Counter that helps generate ID's for the geometries. */
    private static AtomicLong ourIDCounter = new AtomicLong(5000000);

    /** The my source. */
    private ShapeFileSource mySource;

    /**
     * Load file.
     *
     * @param tb the tb
     * @param source the source
     * @return the load result set
     * @throws FileNotFoundException the file not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    public LoadResultSet loadFile(Toolbox tb, ShapeFileSource source)
        throws FileNotFoundException, IOException, InterruptedException
    {
        if (!GDALGenericUtilities.loadGDAL())
        {
            return null;
        }

        if (source.getDataTypeInfo() == null)
        {
            // TODO this not a good message to show to the user because they
            // don't know what the type info is and if they did this still
            // wouldn't give any information that they can use. But, at least we
            // are shouting it at them, which is always good.
            UserMessageEvent.error(tb.getEventManager(),
                    "Failed to load ShapeFile:" + source.getPath() + " Because Type Info Was Null!");
            return null;
        }

        mySource = source;

        String shapeFile = source.getShapeFileAbsolutePath();
        LoadResultSet loadResult = new LoadResultSet();
        loadResult.setDataTypeInfo((ShapeFileDataTypeInfo)source.getDataTypeInfo());

        ShapeFileReadUtilities.openPermissions(shapeFile);
        ShapeFileReadUtilities.openPermissions(shapeFile.replace(".shp", ".dbf"));

        ProgressDialog pd = new ProgressDialog(tb.getUIRegistry().getMainFrameProvider(), "Loading Shapefile", false, true, true,
                "Loading Shapefile:\n\n" + source.getName(), 100);

        pd.setResizable(false);
        pd.setNote("Pre-processing Files...");
        pd.setVisible(true, 2000);

        ShapeFileLoaderTaskActivity activity = new ShapeFileLoaderTaskActivity();
        tb.getUIRegistry().getMenuBarRegistry().addTaskActivity(activity);
        activity.setProgress(0, 0);
        activity.setActive(true);
        try
        {
            ESRIShapefile esf = ShapeFileReadUtilities.readFile(shapeFile,
                    tb.getServerProviderRegistry().getProvider(HttpServer.class));
            if (esf != null)
            {
                doLoad(esf, source, loadResult, pd, activity);
            }
        }
        finally
        {
            final ProgressDialog fPd = pd;
            EventQueueUtilities.invokeLater(() -> fPd.done());
            activity.setComplete(true);
        }

        if (loadResult.getDataElements().size() == 0)
        {
            UserMessageEvent.error(tb.getEventManager(), "No data was loaded from the shape file: " + mySource.getPath()
                    + "\nCheck your import settings to make sure the source is properly configured.");
        }
        loadResult.determineTimeAndGeometryBounds();
        return loadResult;
    }

    /**
     * Extract locations.
     *
     * @param points the points
     * @param startIndex the start index
     * @param endIndex the end index
     * @return the list
     */
    protected List<LatLonAlt> extractLocations(Point2D.Double[] points, int startIndex, int endIndex)
    {
        List<LatLonAlt> pointList = New.list(endIndex - startIndex);
        for (int i = startIndex; i <= endIndex; i++)
        {
            pointList.add(LatLonAlt.createFromDegrees(points[i].y, points[i].x));
        }
        return pointList;
    }

    /**
     * Checks a point list for a polygon, if all the values are very close to
     * zero then this returns true, otherwise returns false.
     *
     * Also returns bad if there is only one point in the point list
     *
     * @param pointList the point list
     * @return true if bad, false if ok
     */
    protected boolean isBadPolylineOrPolygon(Point2D.Double[] pointList)
    {
        boolean isGood = false;

        if (pointList != null && pointList.length > 1)
        {
            boolean allBad = true;
            for (Point2D.Double pt : pointList)
            {
                if (!MathUtil.isZero(pt.x) || !MathUtil.isZero(pt.y))
                {
                    allBad = false;
                    break;
                }
            }
            isGood = !allBad;
        }

        return !isGood;
    }

    /**
     * Load metadata from record.
     *
     * @param dti the dti
     * @param results the results
     * @param fieldNames the field names
     * @param filterColumns the filter columns
     * @param row the row
     * @param shapeType the shape type
     * @param mgs the mgs
     * @return the meta data provider
     */
    protected MetaDataProvider loadMetadataFromRecord(ShapeFileDataTypeInfo dti, LoadResultSet results, List<String> fieldNames,
            Collection<String> filterColumns, Object[] row, ShapeType shapeType, MapGeometrySupport mgs)
    {
        MetaDataInfo mdi = results.getDataTypeInfo().getMetaDataInfo();
        MetaDataProvider provider = new MDILinkedMetaDataProvider(mdi);

        switch (shapeType)
        {
            case POINT:
            case POINTM:
            case POINTZ:
                provider.setValue("LAT", Double.valueOf(((MapLocationGeometrySupport)mgs).getLocation().getLatD()));
                provider.setValue("LON", Double.valueOf(((MapLocationGeometrySupport)mgs).getLocation().getLonD()));
                break;
            default:
                break;
        }

        // String aNote = null;
        // aNote.intern();
        // TODO For shape files is it OK to hard code "LAT" and "LON"?
        // If yes, it should be well commented.

        if (row != null)
        {
            try
            {
                String colName = null;
                String value = null;

                for (int colIdx = 0; colIdx < fieldNames.size(); colIdx++)
                {
                    colName = fieldNames.get(colIdx);

                    if (!filterColumns.contains(colName) && colIdx < row.length)
                    {
                        value = row[colIdx] == null ? null : row[colIdx].toString().trim();
                        if (mgs instanceof MapLocationGeometrySupport && mySource.getLobColumn() == colIdx
                                && colName.equals(mdi.getKeyForSpecialType(LineOfBearingKey.DEFAULT)))
                        {
                            float lobVal = 0.0f;
                            if (!StringUtils.isBlank(value))
                            {
                                try
                                {
                                    lobVal = Float.parseFloat(value);
                                    ((DefaultMapLineOfBearingGeometrySupport)mgs).setOrientation(lobVal);
                                }
                                catch (NumberFormatException e)
                                {
                                    LOGGER.error("Failed to parse value : " + value, e);
                                }
                            }
                        }

                        provider.setValue(colName, value);
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                LOGGER.error(e);
            }
        }
        return provider;
    }

    /**
     * Process point.
     *
     * @param location the location
     * @param fieldNames the field names
     * @param filterColumns the filter columns
     * @param row the row
     * @return the map geometry support
     */
    protected MapGeometrySupport processPoint(Point2D.Double location, List<String> fieldNames, Collection<String> filterColumns,
            Object[] row)
    {
        MapLocationGeometrySupport mlgs = null;
        if (mySource.getLobColumn() != -1)
        {
            mlgs = new DefaultMapLineOfBearingGeometrySupport();
        }
        else
        {
            mlgs = new DefaultMapPointGeometrySupport();
        }

        mlgs.setLocation(LatLonAlt.createFromDegrees(location.getY(), location.getX()));
        return mlgs;
    }

    /**
     * Process polyline or polygon data.
     *
     * @param polygon the polygon
     * @param points the points
     * @param parts the parts
     * @return the map geometry support
     */
    protected MapGeometrySupport processPolylineOrPolygonData(boolean polygon, Point2D.Double[] points, int[] parts)
    {
        Utilities.checkNull(points, "points");
        AbstractMapGeometrySupport mgs = null;

        // At this point, a polygon which has multiple exterior
        // rings or has inner rings has been misread. The
        // parts list will contain only one part.
        if (parts == null || parts.length == 1)
        {
            try
            {
                mgs = buildPolylineOrPolygon(polygon, extractLocations(points, 0, points.length - 1));
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Exception while building polyline: " + e, e);
            }
        }
        else
        {
            for (int partIdx = 0; partIdx < parts.length; partIdx++)
            {
                int startIndex = parts[partIdx];
                int endIndex = points.length - 1;
                if (partIdx + 1 < parts.length)
                {
                    endIndex = parts[partIdx + 1] - 1;
                }

                // Don't process parts with only one point.
                if (startIndex != endIndex)
                {
                    AbstractMapGeometrySupport mgsPart = buildPolylineOrPolygon(polygon,
                            extractLocations(points, startIndex, endIndex));
                    if (mgs == null)
                    {
                        mgs = mgsPart;
                    }
                    else
                    {
                        mgs.addChild(mgsPart);
                    }
                }
            }
        }

        return mgs;
    }

    /**
     * Apply polyline and polygon properties.
     *
     * @param polygon the polygon
     * @param mgs the mgs
     */
    private void applyPolylineAndPolygonProperties(boolean polygon, AbstractMapPathGeometrySupport mgs)
    {
        mgs.setColor(mySource.getShapeColor(), this);
        mgs.setFollowTerrain(mySource.linesFollowTerrain(), this);

        if (polygon)
        {
            if (mySource.isFilledPoly())
            {
                ((DefaultMapPolygonGeometrySupport)mgs).setFilled(true);
            }

            if (mySource.isLinePoly())
            {
                ((DefaultMapPolygonGeometrySupport)mgs).setLineDrawn(true);
            }
        }
    }

    /**
     * Builds the polyline.
     *
     * @param polygon the polygon
     * @param arr the arr
     * @return the map geometry support
     */
    private AbstractMapGeometrySupport buildPolylineOrPolygon(boolean polygon, List<LatLonAlt> arr)
    {
        AbstractMapPathGeometrySupport mgs = null;
        if (polygon)
        {
            Map<List<LatLonAlt>, Collection<List<LatLonAlt>>> parts = GeographicUtilities.decomposePositionsToPolygons(arr);
            for (Entry<List<LatLonAlt>, Collection<List<LatLonAlt>>> part : parts.entrySet())
            {
                if (mgs == null)
                {
                    mgs = new DefaultMapPolygonGeometrySupport(part.getKey(), part.getValue());
                    applyPolylineAndPolygonProperties(polygon, mgs);
                }
                else
                {
                    AbstractMapPathGeometrySupport subMGS = new DefaultMapPolygonGeometrySupport(part.getKey(), part.getValue());
                    applyPolylineAndPolygonProperties(polygon, subMGS);
                    mgs.addChild(subMGS);
                }
            }
        }
        else
        {
            mgs = new DefaultMapPolylineGeometrySupport(arr);
            applyPolylineAndPolygonProperties(polygon, mgs);
        }

        return mgs;
    }

    /**
     * Worker method for loading the file.
     *
     * @param esf The shape file.
     * @param source The source.
     * @param loadResult Te load result set.
     * @param pd The progress dialog.
     * @param activity The take activity.
     * @throws InterruptedException If cancelled.
     */
    private void doLoad(ESRIShapefile esf, ShapeFileSource source, LoadResultSet loadResult, ProgressDialog pd,
            ShapeFileLoaderTaskActivity activity) throws InterruptedException
    {
        esf.getDbf().setFormat(ESRIShapefile.MetadataFormat.ACTUAL);
        if (esf.size() <= 0)
        {
            return;
        }

        ShapeFileDataTypeInfo typeInfo = (ShapeFileDataTypeInfo)source.getDataTypeInfo();
        ShapeType shapeType = ShapeType.getInstance(esf.getShapeType());
        Collection<String> filterColumns = source.getColumnFilter();

        MetadataTimeExtractor timeExtractor = new MetadataTimeExtractor(source);

        pd.setNote("Reading Shape Records...");
        pd.setIndeterminate(false);
        pd.setMaxStep(esf.size());
        pd.setMinStep(0);
        activity.setProgress(0, esf.size());

        TimeSpan timeFilter = source.usesTimeFilter() ? TimeSpan.get(source.getMinDate(), source.getMaxDate())
                : TimeSpan.TIMELESS;
        CoordinateTransformation csTransformer = getCoordinateTransformation(esf);

        for (ShapefileRecord rec : esf)
        {
            if (pd.isCancelled())
            {
                throw new InterruptedException("Shape File Load Cancelled By User");
            }

            MapGeometrySupport mgs = null;
            TimeSpan ts = TimeSpan.TIMELESS;

            pd.incrementStep();
            activity.setProgress(pd.getStep(), esf.size());
            ShapeRecord origShapeRec = rec.shape;
            if (origShapeRec != null)
            {
                Date recDate = timeExtractor.extractDate(rec.metadata);
                if (recDate == null || timeFilter.overlaps(recDate))
                {
                    if (recDate != null)
                    {
                        ts = TimeSpan.get(recDate, recDate);
                    }

                    // Perform coordinate transformation if necessary.
                    ShapeRecord shapeRec = performCSTransformation(origShapeRec, csTransformer);

                    if (shapeRec instanceof PolygonRecord)
                    {
                        mgs = loadPolygonRecord((PolygonRecord)shapeRec);
                    }
                    else if (shapeRec instanceof PolyLineRecord)
                    {
                        mgs = loadPolylineRecord((PolyLineRecord)shapeRec);
                    }
                    else if (shapeRec instanceof PointRecord)
                    {
                        mgs = loadPointRecord(source, filterColumns, rec, (PointRecord)shapeRec);
                    }
                    else if (shapeRec instanceof MultiPointRecord)
                    {
                        LOGGER.info("MultiPointRecord IS NOT IMPLEMENTED.");
                    }
                    else
                    {
                        LOGGER.info("Unprocessed type " + shapeRec.getClass());
                    }

                    if (mgs != null)
                    {
                        MetaDataProvider mdp = loadMetadataFromRecord(typeInfo, loadResult, source.getColumnNames(),
                                filterColumns, rec.metadata, shapeType, mgs);
                        mgs.setColor(mySource.getShapeColor(), null);
                        MapDataElement mde = new DefaultMapDataElement(ourIDCounter.incrementAndGet(), ts, typeInfo, mdp, mgs);
                        mde.getVisualizationState().setColor(mySource.getShapeColor());
                        loadResult.getDataElements().add(mde);
                    }
                }
                else
                {
                    continue;
                }
            }
        }
    }

    /**
     * Get the coordinate transformation.
     *
     * @param esf The shape file.
     * @return The transformation.
     */
    private CoordinateTransformation getCoordinateTransformation(ESRIShapefile esf)
    {
        // Set up a coordinate transformation to wgs84.
        CoordinateTransformation csTransformer = null;
        try
        {
            ProjectionPortion prj = esf.getPrj();
            if (prj != null)
            {
                SpatialReference wgs84 = new SpatialReference(osrConstants.SRS_WKT_WGS84);

                String wkt = prj.getWkt();
                SpatialReference sourceCS = new SpatialReference();

                // Each of following SpatialReference methods will throw
                // a runtime exception if it doesn't succeed. Success
                // indicates a transformation can be performed.
                sourceCS.ImportFromWkt(wkt);
                sourceCS.MorphFromESRI();
                // Calling Validate may fail even when the source can
                // actually be read.
//                        sourceCS.Validate();

                csTransformer = new CoordinateTransformation(sourceCS, wgs84);
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Unsupported projection type: " + e);
            // Remove all dbf/shp records from layer to bypass rest of
            // processing, or rethrow an exception here?
            esf.clear();
        }
        return csTransformer;
    }

    /**
     * Generate a map geometry support from a point record.
     *
     * @param source The shape file source.
     * @param filterColumns The filter columns.
     * @param rec The shape file record.
     * @param shapeRec The point record.
     * @return The map geometry support.
     */
    private MapGeometrySupport loadPointRecord(ShapeFileSource source, Collection<String> filterColumns, ShapefileRecord rec,
            PointRecord shapeRec)
    {
        MapGeometrySupport mgs;
        mgs = processPoint(shapeRec.getPoint(), source.getColumnNames(), filterColumns, rec.metadata);
        return mgs;
    }

    /**
     * Generate a map geometry support from a polygon record.
     *
     * @param shapeRec The polygon record.
     * @return The map geometry support.
     */
    private MapGeometrySupport loadPolygonRecord(PolygonRecord shapeRec)
    {
        // At this point, a polygon which has multiple
        // exterior rings or has inner rings has been misread. The
        // parts list will contain only one part.
        Point2D.Double[] pointList = shapeRec.getPoints();
        int[] partsList = shapeRec.getParts();
        if (!isBadPolylineOrPolygon(pointList))
        {
            return processPolylineOrPolygonData(true, pointList, partsList);
        }
        else
        {
            LOGGER.warn("Bad PolygonRecord Detected and Discarded.");
            return null;
        }
    }

    /**
     * Generate a map geometry support from a polyline record.
     *
     * @param shapeRec The polyline record.
     * @return The map geometry support.
     */
    private MapGeometrySupport loadPolylineRecord(PolyLineRecord shapeRec)
    {
        Point2D.Double[] pointList = shapeRec.getPoints();
        int[] partsList = shapeRec.getParts();
        boolean isBad = pointList == null || pointList.length == 0 || isBadPolylineOrPolygon(shapeRec.getPoints());
        if (!isBad)
        {
            return processPolylineOrPolygonData(false, pointList, partsList);
        }
        else
        {
            LOGGER.warn("Bad PolyLineRecord Detected and Discarded.");
            return null;
        }
    }

    /**
     * Creates a new shape record in the correct coordinate system.
     *
     * @param shapeRecord The original shape record to convert.
     * @param transformer To perform the coordinate system transformation.
     * @return The converted shape record.
     */
    private ShapeRecord performCSTransformation(ShapeRecord shapeRecord, CoordinateTransformation transformer)
    {
        if (transformer == null)
        {
            return shapeRecord;
        }

        ShapeRecord transformed = null;

        if (shapeRecord instanceof PolygonRecord)
        {
            PolygonRecord shape = (PolygonRecord)shapeRecord;
            Point2D.Double[] pts = shape.getPoints();
            if (pts != null)
            {
                List<Point2D.Double> converted = transformPoints(pts, transformer);
                transformed = new PolygonRecord(converted);
            }
        }
        else if (shapeRecord instanceof PolyLineRecord)
        {
            PolyLineRecord shape = (PolyLineRecord)shapeRecord;
            Point2D.Double[] pts = shape.getPoints();
            List<Point2D.Double> converted = transformPoints(pts, transformer);
            transformed = new PolyLineRecord(converted);
        }
        else if (shapeRecord instanceof PointRecord)
        {
            PointRecord shape = (PointRecord)shapeRecord;
            Point2D.Double[] pts = new Point2D.Double[1];
            pts[0] = shape.getPoint();
            List<Point2D.Double> converted = transformPoints(pts, transformer);
            transformed = new PointRecord(converted.get(0));
        }
        else
        {
            transformed = shapeRecord;
        }
        return transformed == null ? shapeRecord : transformed;
    }

    /**
     * Performs coordinate transformation on a list of points.
     *
     * @param pts The points to convert
     * @param transformer Transformer to perform the coordinate system
     *            transformation.
     * @return A list of transformed points.
     */
    private List<Point2D.Double> transformPoints(Point2D.Double[] pts, CoordinateTransformation transformer)
    {
        List<Point2D.Double> newPoints = New.list();
        for (int i = 0; i < pts.length; i++)
        {
            Point2D.Double meters = pts[i];
            double[] out = transformer.TransformPoint(meters.x, meters.y);
            Point2D.Double pt = new Point2D.Double(out[0], out[1]);
            newPoints.add(pt);
        }
        return newPoints;
    }
}
