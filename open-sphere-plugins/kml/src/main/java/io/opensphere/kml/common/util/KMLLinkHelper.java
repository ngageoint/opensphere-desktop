package io.opensphere.kml.common.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.BasicLink;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.Overlay;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import io.opensphere.core.Toolbox;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.impl.MutableGlobeProjection;
import io.opensphere.core.terrain.util.AbsoluteElevationProvider;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.UrlBuilder;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer3D;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.kml.common.model.KMLFeatureUtils;

/**
 * Helps with formatting link URLs.
 */
@SuppressWarnings("PMD.GodClass")
public final class KMLLinkHelper
{
    /** The horizontal field of view in degrees (Google Earth uses 60) . */
    private static final double HORIZ_FOV_DEG = 60.;

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KMLLinkHelper.class);

    /** The decimal format. */
    private static final DecimalFormat ourDecimalFormat = new DecimalFormat("##0.######");

    /**
     * Adds link parameters to a URL.
     *
     * @param dataSource The data source
     * @param url The data source URL
     * @param bbox The bounding box for the view format
     * @param pixelHeight The pixel height for the view format
     * @param pixelWidth The pixel width for the view format
     * @return The fully populated URL
     */
    public static String addURLParameters(KMLDataSource dataSource, String url, GeographicBoundingBox bbox, int pixelWidth,
            int pixelHeight)
    {
        String newURL = url;

        Feature creatingFeature = dataSource.getCreatingFeature();
        if (creatingFeature instanceof Overlay)
        {
            Overlay overlay = (Overlay)creatingFeature;

            try
            {
                UrlBuilder urlBuilder = new UrlBuilder(newURL);
                urlBuilder.addQuery(getHttpQuery(overlay.getIcon().getHttpQuery()));
                urlBuilder.addQuery(getViewFormat(overlay.getIcon().getViewFormat(), overlay.getIcon().getViewRefreshMode(), bbox,
                        pixelWidth, pixelHeight));
                newURL = urlBuilder.toString();
            }
            catch (MalformedURLException e)
            {
                LOGGER.error(e, e);
            }
        }
        return newURL;
    }

    /**
     * Adds link parameters to a URL.
     *
     * @param dataSource The data source
     * @param url The data source URL
     * @return The fully populated URL
     */
    public static URL addURLParameters(KMLDataSource dataSource, URL url)
    {
        URL newURL = url;

        Feature creatingFeature = dataSource.getCreatingFeature();
        if (creatingFeature instanceof NetworkLink)
        {
            NetworkLink networkLink = (NetworkLink)creatingFeature;
            Link link = KMLFeatureUtils.getLink(networkLink);

            if (link != null)
            {
                UrlBuilder urlBuilder = new UrlBuilder(newURL);
                urlBuilder.addQuery(getHttpQuery(link.getHttpQuery()));
                urlBuilder.addQuery(getViewFormat(link.getViewFormat(), link.getViewRefreshMode(), null, -1, -1));
                try
                {
                    newURL = urlBuilder.toURL();
                }
                catch (MalformedURLException e)
                {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return newURL;
    }

    /**
     * Appends the path to the URL.
     *
     * @param url the URL
     * @param path the path
     * @return the new URL, or null if a problem occurred
     */
    public static URL appendPath(URL url, String path)
    {
        URL appendedUrl = null;
        String urlString = StringUtilities.removeSuffix(url.toExternalForm(), "/");
        StringBuilder builder = new StringBuilder(urlString);
        builder.append('/');
        builder.append(StringUtilities.removePrefix(path, "/"));
        try
        {
            appendedUrl = new URL(builder.toString());
        }
        catch (MalformedURLException e)
        {
            LOGGER.error(e.getMessage());
        }
        return appendedUrl;
    }

    /**
     * Creates a URL from the given URL string and data source.
     *
     * @param urlString The URL string
     * @param dataSource The data source
     * @return The URL, or null
     */
    public static URL createFullURL(String urlString, KMLDataSource dataSource)
    {
        URL url = null;
        // Create the URL
        try
        {
            url = new URL(urlString);
        }
        // Append the data source base URL to the front if it's a relative URL
        catch (MalformedURLException e)
        {
            if (StringUtilities.startsWith(urlString, '/') || StringUtilities.startsWith(urlString, '\\'))
            {
                try
                {
                    url = new URL("file://" + urlString);
                }
                catch (MalformedURLException e1)
                {
                    LOGGER.error(e1, e1);
                }
            }
            else
            {
                URL baseUrl = toBaseURL(dataSource);
                if (baseUrl != null)
                {
                    url = appendPath(baseUrl, urlString);
                }
            }
        }
        return url;
    }

    /**
     * Gets a full URL from the given link.
     *
     * @param basicLink The BasicLink
     * @param dataSource The data source
     * @return The URL or null
     */
    public static URL getFullUrlFromBasicLink(BasicLink basicLink, KMLDataSource dataSource)
    {
        URL url = null;
        if (basicLink != null)
        {
            url = createFullURL(StringUtilities.trim(basicLink.getHref()), dataSource);
        }
        return url;
    }

    /**
     * Converts this data source to a base URL.
     *
     * @param dataSource The data source
     * @return The base URL
     */
    public static URL toBaseURL(KMLDataSource dataSource)
    {
        return UrlUtilities.toURLNew(getBasePath(dataSource));
    }

    /**
     * Converts this data source to a URL.
     *
     * @param dataSource The data source
     * @return The URL
     */
    public static URL toURL(KMLDataSource dataSource)
    {
        return UrlUtilities.toURLNew(dataSource.getPath());
    }

    /**
     * Converts the given path to its base path.
     *
     * @param dataSource The data source
     * @return The base path
     */
    private static String getBasePath(KMLDataSource dataSource)
    {
        String path = dataSource.getPath();
        if (path != null)
        {
            char separatorChar = dataSource.getType() == Type.URL ? '/' : File.separatorChar;
            int index = path.lastIndexOf(separatorChar);
            if (index != -1)
            {
                path = path.substring(0, index);
            }
        }
        return path;
    }

    /**
     * Gets the camera location (where the camera is).
     *
     * @return The view location
     */
    private static LatLonAlt getCameraLocation()
    {
        Toolbox toolbox = KMLToolboxUtils.getToolbox();

        DynamicViewer view = toolbox.getMapManager().getStandardViewer();
        Projection projection = toolbox.getMapManager().getProjection();

        Vector3d cameraModel = view.getPosition().getLocation();
        GeographicPosition cameraPosition = projection.convertToPosition(cameraModel, ReferenceLevel.ELLIPSOID);
        return cameraPosition.getLatLonAlt();
    }

    /**
     * Substitutes the given http query with various parameters from the tool.
     *
     * @param httpQuery The http query
     * @return The updated http query
     */
    private static String getHttpQuery(final String httpQuery)
    {
        String newHttpQuery = httpQuery;
        if (!StringUtils.isBlank(newHttpQuery) && newHttpQuery.indexOf('[') != -1 && newHttpQuery.indexOf(']') != -1)
        {
            newHttpQuery = newHttpQuery.replace("[clientVersion]", "5.0");
            newHttpQuery = newHttpQuery.replace("[kmlVersion]", "2.2");
            newHttpQuery = newHttpQuery.replace("[clientName]", "Google+Earth+EC");
            newHttpQuery = newHttpQuery.replace("[language]", "en");
        }
        return newHttpQuery;
    }

    /**
     * Gets the geographic location of where the camera is looking.
     *
     * @return The lookat geographic location
     */
    private static LatLonAlt getLookAtLocation()
    {
        Projection projection = KMLToolboxUtils.getToolbox().getMapManager().getProjection();

        Vector3d screenCenterModel = getLookAtModelLocation();
        GeographicPosition screenCenterPosition = projection.convertToPosition(screenCenterModel, ReferenceLevel.ELLIPSOID);
        LatLonAlt screenCenterPoint = screenCenterPosition.getLatLonAlt();
        return LatLonAlt.createFromDegrees(screenCenterPoint.getLatD(), screenCenterPoint.getLonD());
    }

    /**
     * Gets the model location of where the camera is looking.
     *
     * @return The lookat model location
     */
    private static Vector3d getLookAtModelLocation()
    {
        DynamicViewer view = KMLToolboxUtils.getToolbox().getMapManager().getStandardViewer();

        Vector2i screenCenter = new Vector2i(view.getViewportWidth() / 2, view.getViewportHeight() / 2);
        return view.windowToModelCoords(screenCenter);
    }

    /**
     * Substitutes the given view format with various parameters from the tool.
     *
     * @param viewFormat The view format
     * @param viewRefreshMode The view refresh mode
     * @param visibleBbox The bounding box to put in the query, or {@code null}
     *            to get it from the view bounds
     * @param pixelHeight The pixel height to put in the query, or -1 to get it
     *            from the viewer
     * @param pixelWidth pixel width to put in the query, or -1 to get it from
     *            the viewer
     * @return The updated view format
     */
    private static String getViewFormat(final String viewFormat, ViewRefreshMode viewRefreshMode,
            GeographicBoundingBox visibleBbox, int pixelWidth, int pixelHeight)
    {
        String newViewFormat;

        if (viewRefreshMode == null || viewRefreshMode == ViewRefreshMode.NEVER)
        {
            newViewFormat = viewFormat;
        }
        else if (viewFormat == null)
        {
            if (viewRefreshMode == ViewRefreshMode.ON_STOP)
            {
                GeographicBoundingBox bbox = visibleBbox == null
                        ? KMLToolboxUtils.getToolbox().getMapManager().getVisibleBoundingBox() : visibleBbox;
                StringBuilder bboxBuilder = new StringBuilder("BBOX=");
                bboxBuilder.append(bbox.getMinLonD()).append(',');
                bboxBuilder.append(bbox.getMinLatD()).append(',');
                bboxBuilder.append(bbox.getMaxLonD()).append(',');
                bboxBuilder.append(bbox.getMaxLatD());

                newViewFormat = bboxBuilder.toString();
            }
            else
            {
                newViewFormat = "";
            }
        }
        // It contains substitution parameters
        else if (viewFormat.indexOf('[') != -1 && viewFormat.indexOf(']') != -1)
        {
            newViewFormat = substituteViewFormatParameters(viewFormat, visibleBbox, pixelHeight, pixelWidth);
        }
        else
        {
            newViewFormat = viewFormat;
        }

        return newViewFormat;
    }

    /**
     * Determine if terrain is enabled for the look at location.
     *
     * @return Whether terrain is enabled
     */
    private static boolean isTerrainEnabled()
    {
        boolean isTerrainEnabled = false;
        Toolbox toolbox = KMLToolboxUtils.getToolbox();
        if (toolbox.getMapManager().getRawProjection() instanceof MutableGlobeProjection)
        {
            MutableGlobeProjection projection = (MutableGlobeProjection)toolbox.getMapManager().getRawProjection();

            GeographicPosition lookAtGeoLocation = new GeographicPosition(getLookAtLocation());
            AbsoluteElevationProvider provider = projection.getModel().getCelestialBody().getElevationManager()
                    .getProviderForPosition(lookAtGeoLocation);
            isTerrainEnabled = provider != null;
        }
        return isTerrainEnabled;
    }

    /**
     * Replace the bounding box parameters in the view format.
     *
     * @param visibleBbox The bounding box.
     * @param viewFormat The view format.
     * @return The new view format.
     */
    private static String replaceBBOX(GeographicBoundingBox visibleBbox, String viewFormat)
    {
        GeographicBoundingBox bbox = visibleBbox == null ? KMLToolboxUtils.getToolbox().getMapManager().getVisibleBoundingBox()
                : visibleBbox;

        String newViewFormat = viewFormat.replace("[bboxWest]", String.valueOf(bbox.getMinLonD()));
        newViewFormat = newViewFormat.replace("[bboxSouth]", String.valueOf(bbox.getMinLatD()));
        newViewFormat = newViewFormat.replace("[bboxEast]", String.valueOf(bbox.getMaxLonD()));
        newViewFormat = newViewFormat.replace("[bboxNorth]", String.valueOf(bbox.getMaxLatD()));
        return newViewFormat;
    }

    /**
     * Replace camera parameters.
     *
     * @param viewFormat The view format.
     * @return The new view format.
     */
    private static String replaceCamera(String viewFormat)
    {
        LatLonAlt cameraLocation = getCameraLocation();

        String newViewFormat = viewFormat.replace("[cameraLon]", ourDecimalFormat.format(cameraLocation.getLonD()));
        newViewFormat = newViewFormat.replace("[cameraLat]", ourDecimalFormat.format(cameraLocation.getLatD()));
        newViewFormat = newViewFormat.replace("[cameraAlt]", ourDecimalFormat.format(cameraLocation.getAltM()));
        return newViewFormat;
    }

    /**
     * Replace the FOV parameters in the view format.
     *
     * @param viewFormat The view format string.
     * @param toolbox The toolbox.
     * @return The new view format string.
     */
    private static String replaceFOV(String viewFormat, Toolbox toolbox)
    {
        double horizFov;
        double vertFov;
        DynamicViewer view = toolbox.getMapManager().getStandardViewer();
        if (view instanceof Viewer3D)
        {
            horizFov = Math.toDegrees(((Viewer3D)view).getHorizontalFOV());
            vertFov = Math.toDegrees(((Viewer3D)view).getVerticalFOV());
        }
        else
        {
            horizFov = HORIZ_FOV_DEG;
            double aspectRatio = (double)view.getViewportWidth() / (double)view.getViewportHeight();
            vertFov = Math.toDegrees(Math.atan(Math.tan(Math.toRadians(HORIZ_FOV_DEG) / 2) / aspectRatio) * 2);
        }

        String newViewFormat = viewFormat.replace("[horizFov]", String.valueOf(horizFov));
        newViewFormat = newViewFormat.replace("[vertFov]", String.valueOf(vertFov));
        return newViewFormat;
    }

    /**
     * Replace lookat parameters.
     *
     * @param viewFormat The view format.
     * @param toolbox The toolbox.
     * @return The new view format.
     */
    private static String replaceLookAt(String viewFormat, Toolbox toolbox)
    {
        DynamicViewer view = toolbox.getMapManager().getStandardViewer();

        LatLonAlt lookAtGeoLocation = getLookAtLocation();
        Vector3d lookAtModelLocation = getLookAtModelLocation();
        double range = view.getPosition().getLocation().subtract(lookAtModelLocation).getLength();
        double heading = Math.toDegrees(view.getHeading());
        double tilt = Math.abs(Math.toDegrees(view.getPitch()));

        String newViewFormat = viewFormat.replace("[lookatLon]", ourDecimalFormat.format(lookAtGeoLocation.getLonD()));
        newViewFormat = newViewFormat.replace("[lookatLat]", ourDecimalFormat.format(lookAtGeoLocation.getLatD()));
        newViewFormat = newViewFormat.replace("[lookatRange]", ourDecimalFormat.format(range));
        newViewFormat = newViewFormat.replace("[lookatTilt]", ourDecimalFormat.format(tilt));
        newViewFormat = newViewFormat.replace("[lookatHeading]", ourDecimalFormat.format(heading));
        // TODO the following could be improved
        newViewFormat = newViewFormat.replace("[lookatTerrainLon]", ourDecimalFormat.format(lookAtGeoLocation.getLonD()));
        newViewFormat = newViewFormat.replace("[lookatTerrainLat]", ourDecimalFormat.format(lookAtGeoLocation.getLatD()));
        newViewFormat = newViewFormat.replace("[lookatTerrainAlt]", ourDecimalFormat.format(lookAtGeoLocation.getAltM()));
        return newViewFormat;
    }

    /**
     * Replace pixels parameters.
     *
     * @param viewFormat The view format.
     * @param pixelHeight The pixel height, or -1 to get it from the viewer.
     * @param pixelWidth The pixel width, or -1 to get it from the viewer.
     * @param toolbox The toolbox.
     * @return The new view format.
     */
    private static String replacePixels(String viewFormat, int pixelHeight, int pixelWidth, Toolbox toolbox)
    {
        int horizPixels = pixelWidth == -1 ? toolbox.getMapManager().getStandardViewer().getViewportWidth() : pixelWidth;
        int vertPixels = pixelHeight == -1 ? toolbox.getMapManager().getStandardViewer().getViewportHeight() : pixelHeight;

        String newViewFormat = viewFormat.replace("[horizPixels]", String.valueOf(horizPixels));
        newViewFormat = newViewFormat.replace("[vertPixels]", String.valueOf(vertPixels));
        return newViewFormat;
    }

    /**
     * Substitutes the given view format with various parameters from the tool.
     *
     * @param viewFormat The view format
     * @param visibleBbox The visible bounding box, or {@code null} to get it
     *            from the viewer
     * @param pixelHeight The pixel height for the view format, or -1 to get it
     *            from the viewer
     * @param pixelWidth The pixel width for the view format, or -1 to get it
     *            from the viewer
     * @return The updated view format
     */
    private static String substituteViewFormatParameters(final String viewFormat, GeographicBoundingBox visibleBbox,
            int pixelHeight, int pixelWidth)
    {
        String newViewFormat = viewFormat;

        Toolbox toolbox = KMLToolboxUtils.getToolbox();

        if (newViewFormat.contains("[bbox"))
        {
            newViewFormat = replaceBBOX(visibleBbox, newViewFormat);
        }

        if (newViewFormat.contains("[lookat"))
        {
            newViewFormat = replaceLookAt(newViewFormat, toolbox);
        }

        if (newViewFormat.contains("[camera"))
        {
            newViewFormat = replaceCamera(newViewFormat);
        }

        if (newViewFormat.contains("Fov]"))
        {
            newViewFormat = replaceFOV(newViewFormat, toolbox);
        }

        if (newViewFormat.contains("Pixels]"))
        {
            newViewFormat = replacePixels(newViewFormat, pixelHeight, pixelWidth, toolbox);
        }

        if (newViewFormat.contains("[terrain"))
        {
            boolean isTerrainEnabled = isTerrainEnabled();

            newViewFormat = newViewFormat.replace("[terrainEnabled]", String.valueOf(isTerrainEnabled));
        }

        return newViewFormat;
    }

    /**
     * Default constructor.
     */
    private KMLLinkHelper()
    {
    }
}
