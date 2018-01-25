package io.opensphere.controlpanels.roipanel.config.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;

/** A region of interest. */
@XmlRootElement(name = "RegionOfInterest")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegionOfInterest
{
    /** The name. */
    @XmlAttribute(name = "name")
    private String myName;

    /** The points. */
    @XmlElement(name = "point")
    private List<PointOfInterest> myPoints = new ArrayList<>();

    /**
     * Default constructor.
     */
    public RegionOfInterest()
    {
    }

    /**
     * Copy Constructor.
     *
     * @param other The other RegionOfInterest to initialize with.
     */
    public RegionOfInterest(RegionOfInterest other)
    {
        this();
        setEqualTo(other);
    }

    /**
     * Creates and returns a deep-copy of this RegionOfInterest.
     *
     * @return the deep copy, where only primitives and immutable members may be
     *         shared.
     */
    public RegionOfInterest copy()
    {
        return new RegionOfInterest(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        RegionOfInterest other = (RegionOfInterest)obj;
        return myName.equals(other.myName) && myPoints.equals(other.myPoints);
    }

    /**
     * Find the center of this region of interest.
     *
     * @return The center.
     */
    public GeographicPosition getCenter()
    {
        Coordinate[] coord = new Coordinate[myPoints.size() + 1];
        boolean crosses180 = false;
        for (int i = 0; i < myPoints.size(); i++)
        {
            if (i > 0)
            {
                double lonTotal = myPoints.get(i).getLon() - myPoints.get(i - 1).getLon();
                if (Math.abs(lonTotal) > 180)
                {
                    crosses180 = true;
                }
            }
            coord[i] = new Coordinate(myPoints.get(i).getLon(), myPoints.get(i).getLat());
        }
        coord[myPoints.size()] = new Coordinate(myPoints.get(0).getLon(), myPoints.get(0).getLat());

        // If we don't cross 180 then find center through JTS, if we do just use
        // first point.
        // TODO Create method to find center for any circumstance.
        if (!crosses180)
        {
            GeometryFactory geomFactory = new GeometryFactory();
            Polygon poly = new Polygon(geomFactory.createLinearRing(coord), null, geomFactory);

            Coordinate centerCoord = poly.getCentroid().getCoordinate();

            LatLonAlt lla = LatLonAlt.createFromDegrees(centerCoord.y, centerCoord.x);
            return new GeographicPosition(lla);
        }
        else
        {
            return new GeographicPosition(myPoints.get(0).getPoint());
        }
    }

    /**
     * Get the points for this region of interest as geographic positions.
     *
     * @return The points for this region of interest as geographic positions.
     */
    public Collection<GeographicPosition> getGeoPoints()
    {
        Collection<GeographicPosition> geos = New.collection();
        for (PointOfInterest point : myPoints)
        {
            geos.add(new GeographicPosition(LatLonAlt.createFromDegrees(point.getLat(), point.getLon())));
        }
        return geos;
    }

    /**
     * Standard getter.
     *
     * @return The region of interest name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Standard getter.
     *
     * @return The list of points describing this region of interest.
     */
    public List<PointOfInterest> getPoints()
    {
        return myPoints;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (myName == null ? 0 : myName.hashCode());
        result = prime * result + (myPoints == null ? 0 : myPoints.hashCode());
        return result;
    }

    /**
     * Set my values from another region of interest.
     *
     * @param other The other region of interest.
     */
    public final void setEqualTo(RegionOfInterest other)
    {
        myName = other.getName();

        myPoints = null;
        if (other.getPoints() != null)
        {
            myPoints = new ArrayList<>(other.getPoints().size());
            for (PointOfInterest poi : other.getPoints())
            {
                myPoints.add(new PointOfInterest(poi));
            }
        }
    }

    /**
     * Standard setter.
     *
     * @param name The region of interest name.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Standard setter.
     *
     * @param pPoints The points to use to describe this region of interest.
     */
    public void setPoints(List<PointOfInterest> pPoints)
    {
        myPoints = pPoints;
    }
}
