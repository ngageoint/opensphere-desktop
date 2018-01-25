package io.opensphere.kml.common.util;

import java.util.Date;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.GroundOverlay;
import de.micromata.opengis.kml.v_2_2_0.Link;
import de.micromata.opengis.kml.v_2_2_0.NetworkLink;
import de.micromata.opengis.kml.v_2_2_0.RefreshMode;
import de.micromata.opengis.kml.v_2_2_0.ViewRefreshMode;
import io.opensphere.core.common.connection.HttpHeaders.HttpResponseHeader;
import io.opensphere.core.util.Constants;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLFeatureUtils;

/**
 * Data source utility methods.
 */
public final class KMLDataSourceUtils
{
    /**
     * Converts a NetworkLink refresh interval from a double to an int.
     *
     * @param dataSource The data source.
     * @return The refresh interval rounded to the nearest integer (in seconds)
     */
    public static int getRefreshInterval(KMLDataSource dataSource)
    {
        if (dataSource.getRefreshRate() > 0)
        {
            return dataSource.getRefreshRate();
        }

        Feature creatingFeature = dataSource.getCreatingFeature();

        int refreshInterval = 0;
        RefreshMode refreshMode = null;
        if (creatingFeature instanceof NetworkLink)
        {
            NetworkLink networkLink = (NetworkLink)creatingFeature;

            Link link = KMLFeatureUtils.getLink(networkLink);
            if (link != null)
            {
                refreshMode = link.getRefreshMode();
                if (link.getRefreshMode() == RefreshMode.ON_INTERVAL)
                {
                    refreshInterval = (int)Math.round(link.getRefreshInterval());
                }
                else if (link.getRefreshMode() == RefreshMode.ON_EXPIRE)
                {
                    refreshInterval = getExpireInterval(dataSource);
                }
                else if (link.getViewRefreshMode() == ViewRefreshMode.ON_STOP)
                {
                    refreshInterval = (int)Math.round(link.getViewRefreshTime());
                }
            }
        }
        else if (creatingFeature instanceof GroundOverlay)
        {
            GroundOverlay groundOverlay = (GroundOverlay)creatingFeature;

            refreshMode = groundOverlay.getIcon().getRefreshMode();
            if (groundOverlay.getIcon().getRefreshMode() == RefreshMode.ON_INTERVAL)
            {
                refreshInterval = (int)Math.round(groundOverlay.getIcon().getRefreshInterval());
            }
            else if (groundOverlay.getIcon().getRefreshMode() == RefreshMode.ON_EXPIRE)
            {
                refreshInterval = getExpireInterval(dataSource);
            }
            else if (groundOverlay.getIcon().getViewRefreshMode() == ViewRefreshMode.ON_STOP)
            {
                refreshInterval = (int)Math.round(groundOverlay.getIcon().getViewRefreshTime());
            }
        }

        // Google Earth defaults to 4 if refreshInterval not specified.
        if (refreshInterval < 1 && refreshMode != RefreshMode.ON_EXPIRE)
        {
            refreshInterval = 4;
        }

        return refreshInterval;
    }

    /**
     * Gets the refresh mode for the given feature.
     *
     * @param dataSource The data source.
     * @return The refresh mode.
     */
    public static RefreshMode getRefreshMode(final KMLDataSource dataSource)
    {
        RefreshMode refreshMode = null;
        if (dataSource.getRefreshRate() > 0)
        {
            refreshMode = RefreshMode.ON_INTERVAL;
        }
        else
        {
            Feature creatingFeature = dataSource.getCreatingFeature();
            if (creatingFeature instanceof NetworkLink)
            {
                NetworkLink networkLink = (NetworkLink)creatingFeature;

                Link link = KMLFeatureUtils.getLink(networkLink);
                if (link != null)
                {
                    refreshMode = link.getRefreshMode();
                }
            }
            else if (creatingFeature instanceof GroundOverlay)
            {
                GroundOverlay groundOverlay = (GroundOverlay)creatingFeature;

                refreshMode = groundOverlay.getIcon().getRefreshMode();
            }
        }
        return refreshMode;
    }

    /**
     * Gets the view refresh mode for the given feature.
     *
     * @param dataSource The data source.
     * @return The view refresh mode.
     */
    public static ViewRefreshMode getViewRefreshMode(final KMLDataSource dataSource)
    {
        ViewRefreshMode viewRefreshMode = null;
        if (dataSource.getRefreshRate() > 0)
        {
            return viewRefreshMode;
        }

        Feature creatingFeature = dataSource.getCreatingFeature();
        if (creatingFeature instanceof NetworkLink)
        {
            NetworkLink networkLink = (NetworkLink)creatingFeature;

            Link link = KMLFeatureUtils.getLink(networkLink);
            if (link != null)
            {
                viewRefreshMode = link.getViewRefreshMode();
            }
        }
        else if (creatingFeature instanceof GroundOverlay)
        {
            GroundOverlay groundOverlay = (GroundOverlay)creatingFeature;

            viewRefreshMode = groundOverlay.getIcon().getViewRefreshMode();
        }
        return viewRefreshMode;
    }

    /**
     * Gets the expire interval for the given data source.
     *
     * @param dataSource The data source.
     * @return The expire interval in seconds, or 0 to ignore it
     */
    private static int getExpireInterval(KMLDataSource dataSource)
    {
        int expireInterval = 0;

        // Note the KML spec says the NetworkLinkControl expire time takes
        // precedence, but at least in Google Earth 5, it seems to be ignoring
        // it. So not implementing this for now.
        String linkControlExpires = null;
        String cacheControl = dataSource.getResponseHeaders().get(HttpResponseHeader.CACHE_CONTROL.getFieldName());
        String expires = dataSource.getResponseHeaders().get(HttpResponseHeader.EXPIRES.getFieldName());

        Date expireTime = KMLSpatialTemporalUtils.getExpireTime(linkControlExpires, cacheControl, expires);
        if (expireTime != null)
        {
            expireInterval = (int)Math
                    .round((expireTime.getTime() - System.currentTimeMillis()) / Constants.MILLI_PER_UNIT_DOUBLE);
        }

        return expireInterval;
    }

    /**
     * Private constructor.
     */
    private KMLDataSourceUtils()
    {
    }
}
