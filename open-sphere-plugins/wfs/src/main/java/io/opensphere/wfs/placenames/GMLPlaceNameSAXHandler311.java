package io.opensphere.wfs.placenames;

import java.util.Stack;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;

/** Parser for the GML place names. */
public class GMLPlaceNameSAXHandler311 extends org.xml.sax.helpers.DefaultHandler
{
    /** GML feature member tag. */
    private static final String GML_FEATURE_MEMBER = "gml:featureMember";

    /** Full name tag. */
    private static final String TOPP_FULL_NAME_ND = "ASCIINAME";

    /** Latitude tag. */
    private static final String TOPP_LATITUDE = "LAT";

    /** Longitude tag. */
    private static final String TOPP_LONGITUDE = "LON";

    /** The latitude of the current name. */
    private double myCurrentLat;

    /** The longitude of the current name. */
    private double myCurrentLon;

    /** The current place name being populated. */
    private PlaceNameData.PlaceName myCurrentName;

    /** PlaceNameData which stores the values parsed by this handler. */
    private final PlaceNameData myPlaceNameData = new PlaceNameData();

    /** Interned names. */
    private final Stack<String> myTags = new Stack<>();

    @Override
    public void characters(char[] ch, int start, int length)
    {
        if (myCurrentName == null)
        {
            return;
        }

        String qName = myTags.peek();
        if (TOPP_LATITUDE.equals(qName))
        {
            myCurrentLat = Double.parseDouble(new String(ch, start, length));
        }
        else if (TOPP_LONGITUDE.equals(qName))
        {
            myCurrentLon = Double.parseDouble(new String(ch, start, length));
        }
        else if (TOPP_FULL_NAME_ND.equals(qName))
        {
            myCurrentName.setName(new String(ch, start, length));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if (GML_FEATURE_MEMBER.equals(qName))
        {
            GeographicPosition location = new GeographicPosition(LatLonAlt.createFromDegrees(myCurrentLat, myCurrentLon));
            myCurrentName.setLocation(location);
            myPlaceNameData.getPlaceNames().add(myCurrentName);
            myCurrentName = null;
        }
        myTags.pop();
    }

    /**
     * Get the placeNameData.
     *
     * @return the placeNameData
     */
    public PlaceNameData getPlaceNameData()
    {
        return myPlaceNameData;
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
    {
        if (GML_FEATURE_MEMBER.equals(qName))
        {
            myCurrentName = new PlaceNameData.PlaceName();
        }
        myTags.push(qName);
    }
}
