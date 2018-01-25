package io.opensphere.core.common.geospatial;

/**
 * This enumeration provides common <b>S</b>patial <b>R</b>eference
 * <b>ID</b>entifier (SRID) values. SRIDs and descriptions can be found in
 * MDSYS.SDO_CS_SRS in Oracle and spatial_ref_sys in PostGIS.
 */
public enum SRID
{
    CRS27("CRS:27", 4268, 8260), CRS83("CRS:83", 4269, 8265), CRS84("CRS:84", 84, 8307), EPSG4326("EPSG:4326", 4326, 8307);

    /**
     * The Oracle-specific SRID.
     */
    private int oracleSrid;

    /**
     * The Coordinate Reference System code.
     */
    private int crsCode;

    /**
     * The Coordinate Reference System identifier.
     */
    private String crsId;

    private SRID(final String crsId, final int crsCode, final int oracleSrid)
    {
        this.crsId = crsId;
        this.crsCode = crsCode;
        this.oracleSrid = oracleSrid;
    }

    /**
     * Returns the Coordinate Reference System identifier (e.g. EPSG:4326 or
     * CRS:84).
     *
     * @return the Coordinate Reference System identifier.
     */
    public String getCrsId()
    {
        return crsId;
    }

    /**
     * Returns the Coordinate Reference System code (e.g. 4326 for EPSG:4326 or
     * 84 for CRS:84).
     *
     * @return the Coordinate Reference System code.
     */
    public int getCrsCode()
    {
        return crsCode;
    }

    /**
     * Returns the Oracle-specific SRID.
     *
     * @return the Oracle-specific SRID.
     */
    public int getOracleSrid()
    {
        return oracleSrid;
    }
}
