package io.opensphere.city.model.json;

import java.io.InputStream;
import java.util.Objects;

import org.codehaus.jackson.annotate.JsonSetter;

/**
 * Json class representing one Result object returned by the cyber city 3d
 * server.
 */
public class Result
{
    /**
     * Actual surface area.
     */
    private double myArea;

    /**
     * If the object becomes a block volume, it is this volume.
     */
    private double myBlockVolume;

    /**
     * Unique Building ID.
     */
    private String myBuildingId;

    /**
     * The collada file content.
     */
    private volatile InputStream myColladaContent;

    /**
     * The download url for the collada model.
     */
    private String myDaeDownloadUrl;

    /**
     * Collada filename.
     */
    private String myDaeFilename;

    /**
     * The data registry id for the result.
     */
    private long myDataRegistryId;

    /**
     * Date in which the object was added to the database.
     */
    private String myDateAdded;

    /**
     * The value of date_added in “DD/MM/YYYY HH:MM:SS” 12-hour format.
     */
    private String myDateAddedFriendly;

    /**
     * Date in which the object was updated in the database.
     */
    private String myDateUpdated;

    /**
     * The value of date_updated in “DD/MM/YYYY HH:MM:SS” 12-hour format.
     */
    private String myDateUpdatedFriendly;

    /**
     * Distance (in kilometers) from object to the latitude/longitude or postal
     * address specified.
     */
    private double myDistance;

    /**
     * Object height after removing any roof faces.
     */
    private double myFlatHeight;

    /**
     * The Z coordinate (in meters) of the lowest foot point of this building
     * (sea level) .
     */
    private double myGroundZ;

    /**
     * Height of object (top_z minus ground_z).
     */
    private double myHeight;

    /**
     * Id of the object.
     */
    private String myId;

    /**
     * The latitude.
     */
    private double myLatitude;

    /**
     * The longitude.
     */
    private double myLongitude;

    /**
     * Compass direction.
     */
    private double myNorthOrientation;

    /**
     * Surface area in flat planar.
     */
    private double myPlanarArea;

    /**
     * N = Northern hemisphere, S = Southern Hemisphere.
     */
    private String myPositionUtmHemisphere;

    /**
     * The utm zone.
     */
    private String myPositionUtmZone;

    /**
     * UTM X coordinate.
     */
    private double myPositionX;

    /**
     * UTM y coordinate.
     */
    private double myPositionY;

    /**
     * Elevation in Meters.
     */
    private double myPositionZ;

    /**
     * The minimum decline angle of roof faces.
     */
    private double myRoofMinDeclineAngle;

    /**
     * Roof polygon ID.
     */
    private double mySolafaceId;

    /**
     * The Z coordinate (in meters) of the highest point of this building (sea
     * level).
     */
    private double myTopZ;

    /**
     * 1 = Building, 2 = Surface.
     */
    private Type myType;

    /**
     * A descriptive name for the object’s type.
     */
    private String myTypeName;

    /**
     * Volume of object.
     */
    private double myVolume;

    /**
     * Pitch angle of roof polygon.
     */
    private double myZOrientation;

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
        Result other = (Result)obj;
        return Objects.equals(myId, other.myId);
    }

    /**
     * Getter.
     *
     * @return the area
     */
    public double getArea()
    {
        return myArea;
    }

    /**
     * Getter.
     *
     * @return the block_volume
     */
    public double getBlockVolume()
    {
        return myBlockVolume;
    }

    /**
     * Getter.
     *
     * @return the building_id
     */
    public String getBuildingId()
    {
        return myBuildingId;
    }

    /**
     * Getter.
     *
     * @return the colladaContent
     */
    public InputStream getColladaContent()
    {
        return myColladaContent;
    }

    /**
     * Getter.
     *
     * @return the dae_download_url
     */
    public String getDaeDownloadUrl()
    {
        return myDaeDownloadUrl;
    }

    /**
     * Getter.
     *
     * @return the dae_filename
     */
    public String getDaeFilename()
    {
        return myDaeFilename;
    }

    /**
     * Getter.
     *
     * @return the dataRegistryId.
     */
    public long getDataRegistryId()
    {
        return myDataRegistryId;
    }

    /**
     * Getter.
     *
     * @return the date_added
     */
    public String getDateAdded()
    {
        String returnDate = null;

        if (myDateAdded != null)
        {
            returnDate = myDateAdded;
        }

        return returnDate;
    }

    /**
     * Getter.
     *
     * @return the date_added_friendly
     */
    public String getDateAddedFriendly()
    {
        return myDateAddedFriendly;
    }

    /**
     * Getter.
     *
     * @return the date_updated
     */
    public String getDateUpdated()
    {
        String returnDate = null;

        if (myDateUpdated != null)
        {
            returnDate = myDateUpdated;
        }

        return returnDate;
    }

    /**
     * Getter.
     *
     * @return the date_updated_friendly
     */
    public String getDateUpdatedFriendly()
    {
        return myDateUpdatedFriendly;
    }

    /**
     * Getter.
     *
     * @return the distance
     */
    public double getDistance()
    {
        return myDistance;
    }

    /**
     * Getter.
     *
     * @return the flat_height
     */
    public double getFlatHeight()
    {
        return myFlatHeight;
    }

    /**
     * Getter.
     *
     * @return the ground_z
     */
    public double getGroundZ()
    {
        return myGroundZ;
    }

    /**
     * Getter.
     *
     * @return the height
     */
    public double getHeight()
    {
        return myHeight;
    }

    /**
     * Getter.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Getter.
     *
     * @return the latitude
     */
    public double getLatitude()
    {
        return myLatitude;
    }

    /**
     * Getter.
     *
     * @return the longitude
     */
    public double getLongitude()
    {
        return myLongitude;
    }

    /**
     * Getter.
     *
     * @return the north_orientation
     */
    public double getNorthOrientation()
    {
        return myNorthOrientation;
    }

    /**
     * Getter.
     *
     * @return the planar_area
     */
    public double getPlanarArea()
    {
        return myPlanarArea;
    }

    /**
     * Getter.
     *
     * @return the position_utm_hemisphere
     */
    public String getPositionUtmHemisphere()
    {
        return myPositionUtmHemisphere;
    }

    /**
     * Getter.
     *
     * @return the position_utm_zome
     */
    public String getPositionUtmZone()
    {
        return myPositionUtmZone;
    }

    /**
     * Getter.
     *
     * @return the position_x
     */
    public double getPositionX()
    {
        return myPositionX;
    }

    /**
     * Getter.
     *
     * @return the position_y
     */
    public double getPositionY()
    {
        return myPositionY;
    }

    /**
     * Getter.
     *
     * @return the position_z
     */
    public double getPositionZ()
    {
        return myPositionZ;
    }

    /**
     * Getter.
     *
     * @return the roof_min_decline_angle
     */
    public double getRoofMinDeclineAngle()
    {
        return myRoofMinDeclineAngle;
    }

    /**
     * Getter.
     *
     * @return the solaface_id
     */
    public double getSolafaceId()
    {
        return mySolafaceId;
    }

    /**
     * Getter.
     *
     * @return the top_z
     */
    public double getTopZ()
    {
        return myTopZ;
    }

    /**
     * Getter.
     *
     * @return the type
     */
    public Type getType()
    {
        return myType;
    }

    /**
     * Getter.
     *
     * @return the type_name
     */
    public String getTypeName()
    {
        return myTypeName;
    }

    /**
     * Getter.
     *
     * @return the volume
     */
    public double getVolume()
    {
        return myVolume;
    }

    /**
     * Getter.
     *
     * @return the z_orientation
     */
    public double getZOrientation()
    {
        return myZOrientation;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myId == null ? 0 : myId.hashCode());
        return result;
    }

    /**
     * Setter.
     *
     * @param area the area to set
     */
    public void setArea(double area)
    {
        myArea = area;
    }

    /**
     * Setter.
     *
     * @param blockVolume the block_volume to set
     */
    @JsonSetter("block_volume")
    public void setBlockVolume(double blockVolume)
    {
        myBlockVolume = blockVolume;
    }

    /**
     * Setter.
     *
     * @param buildingId the building_id to set
     */
    @JsonSetter("building_id")
    public void setBuildingId(String buildingId)
    {
        myBuildingId = buildingId;
    }

    /**
     * Setter.
     *
     * @param colladaContent the colladaContent to set
     */
    public void setColladaContent(InputStream colladaContent)
    {
        myColladaContent = colladaContent;
    }

    /**
     * Setter.
     *
     * @param daeDownloadUrl the dae_download_url to set
     */
    @JsonSetter("dae_download_url")
    public void setDaeDownloadUrl(String daeDownloadUrl)
    {
        myDaeDownloadUrl = daeDownloadUrl;
    }

    /**
     * Setter.
     *
     * @param daeFilename the dae_filename to set
     */
    @JsonSetter("dae_filename")
    public void setDaeFilename(String daeFilename)
    {
        myDaeFilename = daeFilename;
    }

    /**
     * Setter.
     *
     * @param dataRegistryId the dataRegistryId to set
     */
    public void setDataRegistryId(long dataRegistryId)
    {
        myDataRegistryId = dataRegistryId;
    }

    /**
     * Setter.
     *
     * @param dateAdded the date_added to set
     */
    @JsonSetter("date_added")
    public void setDateAdded(String dateAdded)
    {
        if (dateAdded != null)
        {
            myDateAdded = dateAdded;
        }
        else
        {
            myDateAdded = null;
        }
    }

    /**
     * Setter.
     *
     * @param dateAddedFriendly the date_added_friendly to set
     */
    @JsonSetter("date_added_friendly")
    public void setDateAddedFriendly(String dateAddedFriendly)
    {
        myDateAddedFriendly = dateAddedFriendly;
    }

    /**
     * Setter.
     *
     * @param dateUpdated the date_updated to set
     */
    @JsonSetter("date_updated")
    public void setDateUpdated(String dateUpdated)
    {
        if (dateUpdated != null)
        {
            myDateUpdated = dateUpdated;
        }
        else
        {
            myDateUpdated = null;
        }
    }

    /**
     * Setter.
     *
     * @param dateUpdatedFriendly the date_updated_friendly to set
     */
    @JsonSetter("date_updated_friendly")
    public void setDateUpdatedFriendly(String dateUpdatedFriendly)
    {
        myDateUpdatedFriendly = dateUpdatedFriendly;
    }

    /**
     * Setter.
     *
     * @param distance the distance to set
     */
    public void setDistance(double distance)
    {
        myDistance = distance;
    }

    /**
     * Setter.
     *
     * @param flatHeight the flat_height to set
     */
    @JsonSetter("flat_height")
    public void setFlatHeight(double flatHeight)
    {
        myFlatHeight = flatHeight;
    }

    /**
     * Setter.
     *
     * @param groundZ the ground_z to set
     */
    @JsonSetter("ground_z")
    public void setGroundZ(double groundZ)
    {
        myGroundZ = groundZ;
    }

    /**
     * Setter.
     *
     * @param height the height to set
     */
    public void setHeight(double height)
    {
        myHeight = height;
    }

    /**
     * Setter.
     *
     * @param id the id to set
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Setter.
     *
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude)
    {
        myLatitude = latitude;
    }

    /**
     * Setter.
     *
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude)
    {
        myLongitude = longitude;
    }

    /**
     * Setter.
     *
     * @param northOrientation the north_orientation to set
     */
    @JsonSetter("north_orientation")
    public void setNorthOrientation(double northOrientation)
    {
        myNorthOrientation = northOrientation;
    }

    /**
     * Setter.
     *
     * @param planarArea the planar_area to set
     */
    @JsonSetter("planar_area")
    public void setPlanarArea(double planarArea)
    {
        myPlanarArea = planarArea;
    }

    /**
     * Setter.
     *
     * @param positionUtmHemisphere the position_utm_hemisphere to set
     */
    @JsonSetter("position_utm_hemisphere")
    public void setPositionUtmHemisphere(String positionUtmHemisphere)
    {
        myPositionUtmHemisphere = positionUtmHemisphere;
    }

    /**
     * Setter.
     *
     * @param positionUtmZone the position_utm_zome to set
     */
    @JsonSetter("position_utm_zone")
    public void setPositionUtmZone(String positionUtmZone)
    {
        myPositionUtmZone = positionUtmZone;
    }

    /**
     * Setter.
     *
     * @param positionX the position_x to set
     */
    @JsonSetter("position_x")
    public void setPositionX(double positionX)
    {
        myPositionX = positionX;
    }

    /**
     * Setter.
     *
     * @param positionY the position_y to set
     */
    @JsonSetter("position_y")
    public void setPositionY(double positionY)
    {
        myPositionY = positionY;
    }

    /**
     * Setter.
     *
     * @param positionZ the position_z to set
     */
    @JsonSetter("position_z")
    public void setPositionZ(double positionZ)
    {
        myPositionZ = positionZ;
    }

    /**
     * Setter.
     *
     * @param roofMinDeclineAngle the roof_min_decline_angle to set
     */
    @JsonSetter("roof_min_decline_angle")
    public void setRoofMinDeclineAngle(double roofMinDeclineAngle)
    {
        myRoofMinDeclineAngle = roofMinDeclineAngle;
    }

    /**
     * Setter.
     *
     * @param solafaceId the solaface_id to set
     */
    @JsonSetter("solaface_id")
    public void setSolafaceId(double solafaceId)
    {
        mySolafaceId = solafaceId;
    }

    /**
     * Setter.
     *
     * @param topZ the top_z to set
     */
    @JsonSetter("top_z")
    public void setTopZ(double topZ)
    {
        myTopZ = topZ;
    }

    /**
     * Setter.
     *
     * @param type the type to set
     */
    public void setType(Type type)
    {
        myType = type;
    }

    /**
     * Setter.
     *
     * @param typeName the type_name to set
     */
    @JsonSetter("type_name")
    public void setTypeName(String typeName)
    {
        myTypeName = typeName;
    }

    /**
     * Setter.
     *
     * @param volume the volume to set
     */
    public void setVolume(double volume)
    {
        myVolume = volume;
    }

    /**
     * Setter.
     *
     * @param zOrientation the z_orientation to set
     */
    @JsonSetter("z_orientation")
    public void setZOrientation(double zOrientation)
    {
        myZOrientation = zOrientation;
    }
}
