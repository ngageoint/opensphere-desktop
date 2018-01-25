package io.opensphere.core.common.shapefile.prj;

/**
 * This enumeration contains the common shapefile formats that this software
 * outputs.
 */
public enum Projection
{

    GCS_WGS_1984(
            "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]");

    private String mWellKnownText;

    private Projection(String wellKnownText)
    {
        mWellKnownText = wellKnownText;
    }

    public String getWellKnownText()
    {
        return mWellKnownText;
    }

}
