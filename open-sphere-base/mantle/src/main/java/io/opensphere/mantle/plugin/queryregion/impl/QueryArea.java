package io.opensphere.mantle.plugin.queryregion.impl;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.model.time.ISO8601TimeSpanAdapter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;

/**
 * A model for a query area in a state file.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class QueryArea
{
    /** The id of the query area. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The layers that the query area applies to. */
    @XmlElement(name = "layer")
    private final Collection<String> myLayers = New.collection();

    /** The polygons that make up the area. */
    @XmlElement(name = "Polygon", namespace = "http://www.opengis.net/kml/2.2")
    private Collection<Polygon> myPolygons = New.collection();

    /** The times that the query area is valid. */
    @XmlJavaTypeAdapter(value = ISO8601TimeSpanAdapter.class)
    @XmlElement(name = "validTime")
    private Collection<TimeSpan> myTimes = New.collection();

    /** The title of the query area. */
    @XmlAttribute(name = "title")
    private String myTitle;

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Get the layers.
     *
     * @return The layers.
     */
    public Collection<String> getLayers()
    {
        return myLayers;
    }

    /**
     * Get the polygons.
     *
     * @return The polygons.
     */
    public Collection<Polygon> getPolygons()
    {
        return myPolygons;
    }

    /**
     * Get the times.
     *
     * @return The times.
     */
    public Collection<TimeSpan> getTimes()
    {
        return myTimes;
    }

    /**
     * Get the title.
     *
     * @return The title.
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Set the layers.
     *
     * @param layers The layers.
     */
    public void setLayers(Collection<? extends String> layers)
    {
        myLayers.clear();
        myLayers.addAll(layers);
    }

    /**
     * Set the polygons.
     *
     * @param polygons The polygons.
     */
    public void setPolygons(Collection<Polygon> polygons)
    {
        myPolygons = polygons;
    }

    /**
     * Set the times.
     *
     * @param times The times.
     */
    public void setTimes(Collection<TimeSpan> times)
    {
        myTimes = times;
    }

    /**
     * Set the title.
     *
     * @param title The title.
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }
}
