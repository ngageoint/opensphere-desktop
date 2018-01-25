package io.opensphere.kml.gx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import io.opensphere.core.util.collections.New;

/**
 * This class represents the gx:Track in Kml.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Track", propOrder = { "myWhen", "myCoord" })
@XmlRootElement(name = "Track", namespace = "http://www.google.com/kml/ext/2.2")
public class Track extends Geometry
{
    /**
     * The when elements.
     */
    @XmlElement(name = "when", namespace = "http://www.opengis.net/kml/2.2", type = String.class)
    @XmlJavaTypeAdapter(DateConverter.class)
    private List<Date> myWhen;

    /**
     * The coord elements.
     */
    @XmlElement(name = "coord", namespace = "http://www.google.com/kml/ext/2.2", type = String.class)
    @XmlJavaTypeAdapter(CoordConverter.class)
    private List<Coordinate> myCoord;

    @Override
    public Track clone()
    {
        Track copy = (Track)super.clone();

        copy.myWhen = New.list();
        copy.myWhen.addAll(getWhen());

        copy.myCoord = New.list();
        for (Coordinate coordinate : getCoordinates())
        {
            copy.getCoordinates().add(coordinate.clone());
        }

        return copy;
    }

    /**
     * Gets the coordinates of the track.
     *
     * @return The list of track coordinates.
     */
    public List<Coordinate> getCoordinates()
    {
        if (myCoord == null)
        {
            myCoord = new ArrayList<>();
        }
        return myCoord;
    }

    /**
     * Gets the time for each individual coordinate. If a coordinate does not
     * have time there will be an empty element.
     *
     * @return The list of times for each coordinate.
     */
    public List<Date> getWhen()
    {
        if (myWhen == null)
        {
            myWhen = new ArrayList<>();
        }

        return myWhen;
    }
}
