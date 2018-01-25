package io.opensphere.kml.gx;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;

/**
 * Converts coordinates to gx:coords.
 */
public class CoordConverter extends XmlAdapter<String, Coordinate>
{
    @Override
    public String marshal(Coordinate v)
    {
        return v.getLongitude() + " " + v.getLatitude() + " " + v.getAltitude();
    }

    @Override
    public Coordinate unmarshal(String v)
    {
        String[] coords = v.split(" ");
        double longitude = Double.parseDouble(coords[0]);
        double latitude = Double.parseDouble(coords[1]);
        double altitude = Double.parseDouble(coords[2]);

        return new Coordinate(longitude, latitude, altitude);
    }
}
