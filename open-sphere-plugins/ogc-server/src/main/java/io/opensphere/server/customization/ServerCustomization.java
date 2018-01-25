package io.opensphere.server.customization;

import io.opensphere.core.model.time.TimeSpan;
import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * Interface for Server Customizations.
 */
public interface ServerCustomization
{
    /** The name for the Default ServerCustomization. */
    String DEFAULT_TYPE = "Default";

    /**
     * Given a timespan (single time instant or span), returns the time in the
     * ISO-8601 format required by this server.
     *
     * @param span the time span to format
     * @return the ISO-8601 formatted WMS time
     */
    String getFormattedWMSTime(TimeSpan span);

    /**
     * Gets the order of latitude and longitude components in geometries for
     * this server.
     *
     * @param wfsCap the capabilities document from WFs used to establish
     *            lat/lon order
     * @return the order of latitude and longitude
     */
    LatLonOrder getLatLonOrder(WFSCapabilitiesType wfsCap);

    /**
     * Gets a string that uniquely identifies this server customization.
     *
     * @return the server type
     */
    String getServerType();

    /**
     * Gets the name of the Spatial Reference System (SRS) preferred by this
     * server.
     *
     * @return the SRS name
     */
    String getSrsName();

    /**
     * Enum describing how latitude and longitude are ordered in geometries.
     */
    enum LatLonOrder
    {
        /** LATitude comes before LONgitude. */
        LATLON,

        /** LONgitude comes before LATitude. */
        LONLAT,

        /** UNKNOWN lat/lon order. */
        UNKNOWN
    }
}
