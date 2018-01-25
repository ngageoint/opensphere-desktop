package io.opensphere.wfs.placenames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.GeographicPosition;

/** The place names which may be cached for a particular key. */
public class PlaceNameData implements Serializable
{
    /** Property descriptor for use in the data registry. */
    public static final PropertyDescriptor<PlaceNameData> PROPERTY_DESCRIPTOR = new PropertyDescriptor<PlaceNameData>("value",
            PlaceNameData.class);

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Place names in this group. */
    private final List<PlaceName> myPlaceNames = new ArrayList<>();

    /**
     * Get the placeNames.
     *
     * @return the placeNames
     */
    public List<PlaceName> getPlaceNames()
    {
        return myPlaceNames;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (PlaceName name : myPlaceNames)
        {
            builder.append(name);
        }
        return builder.toString();
    }

    /** A place name. */
    public static class PlaceName implements Serializable
    {
        /** serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** Location of the place name. */
        private GeographicPosition myLocation;

        /** Name of the place. */
        private String myName;

        /**
         * Get the location.
         *
         * @return the location
         */
        public GeographicPosition getLocation()
        {
            return myLocation;
        }

        /**
         * Get the name.
         *
         * @return the name
         */
        public String getName()
        {
            return myName;
        }

        /**
         * Set the location.
         *
         * @param location the location to set
         */
        public void setLocation(GeographicPosition location)
        {
            myLocation = location;
        }

        /**
         * Set the name.
         *
         * @param name the name to set
         */
        public void setName(String name)
        {
            myName = name;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(myName).append(" : ").append(myLocation).append('\n');
            return builder.toString();
        }
    }
}
