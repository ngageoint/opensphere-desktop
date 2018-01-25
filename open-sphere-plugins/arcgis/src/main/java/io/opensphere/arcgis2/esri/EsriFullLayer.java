package io.opensphere.arcgis2.esri;

import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.util.lang.enums.EnumUtilities;

/** The Class EsriFullLayer. */
@JsonAutoDetect(JsonMethod.NONE)
public class EsriFullLayer extends EsriLayerReference
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** My capabilities. */
    @JsonProperty("capabilities")
    private String myCapabilities;

    /** My copyright text. */
    @JsonProperty("copyrightText")
    private String myCopyrightText;

    /** My current ArcGIS version. */
    @JsonProperty("currentVersion")
    private String myCurrentVersion;

    /** My default visibility. */
    @JsonProperty("defaultVisibility")
    private boolean myDefaultVisibility;

    /** My definition expression. */
    @JsonProperty("definitionExpression")
    private String myDefinitionExpression;

    /** My description. */
    @JsonProperty("description")
    private String myDescription;

    /** My display field. */
    @JsonProperty("displayField")
    private String myDisplayField;

    /** My drawing info. */
    @JsonProperty("drawingInfo")
    private EsriDrawingInfo myDrawingInfo;

    /** My extent. */
    @JsonProperty("extent")
    private EsriExtent myExtent;

    /** My fields. */
    @JsonProperty("fields")
    private List<EsriField> myFields;

    /** My geometry type. */
    @JsonProperty("geometryType")
    private EsriGeometryType myGeometryType;

    /** My has attachments. */
    @JsonProperty("hasAttachments")
    private boolean myHasAttachments;

    /** My html popup type. */
    @JsonProperty("htmlPopupType")
    private String myHtmlPopupType;

    /** My parent layer. */
    @JsonProperty("parentLayer")
    private EsriLayerReference myParentLayer;

    /** My relationships. */
    @JsonProperty("relationships")
    private List<Object> myRelationships;

    /** My sub layers. */
    @JsonProperty("subLayers")
    private List<EsriLayerReference> mySubLayers;

    /** My time info. */
    @JsonProperty("timeInfo")
    private EsriTimeInfo myTimeInfo;

    /** My type. */
    @JsonProperty("type")
    private String myType;

    /** My type id field. */
    @JsonProperty("typeIdField")
    private Object myTypeIdField;

    /** My types. */
    @JsonProperty("types")
    private Object myTypes;

    /**
     * Gets the capabilities.
     *
     * @return the capabilities
     */
    public String getCapabilities()
    {
        return myCapabilities;
    }

    /**
     * Gets the copyright text.
     *
     * @return the copyright text
     */
    public String getCopyrightText()
    {
        return myCopyrightText;
    }

    /**
     * Gets the current ArcGIS version.
     *
     * @return the current ArcGIS version
     */
    public String getCurrentVersion()
    {
        return myCurrentVersion;
    }

    /**
     * Gets the definition expression.
     *
     * @return the definition expression
     */
    public String getDefinitionExpression()
    {
        return myDefinitionExpression;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the display field.
     *
     * @return the display field
     */
    public String getDisplayField()
    {
        return myDisplayField;
    }

    /**
     * Gets the drawing info.
     *
     * @return the drawing info
     */
    public EsriDrawingInfo getDrawingInfo()
    {
        return myDrawingInfo;
    }

    /**
     * Gets the extent.
     *
     * @return the extent
     */
    public EsriExtent getExtent()
    {
        return myExtent;
    }

    /**
     * Gets the fields.
     *
     * @return the fields
     */
    public List<EsriField> getFields()
    {
        return myFields == null ? null : Collections.unmodifiableList(myFields);
    }

    /**
     * Gets the geometry type.
     *
     * @return the geometry type
     */
    public EsriGeometryType getGeometryType()
    {
        return myGeometryType;
    }

    /**
     * Gets the html popup type.
     *
     * @return the html popup type
     */
    public String getHtmlPopupType()
    {
        return myHtmlPopupType;
    }

    /**
     * Gets the parent layer.
     *
     * @return the parent layer
     */
    public EsriLayerReference getParentLayer()
    {
        return myParentLayer;
    }

    /**
     * Gets the relationships.
     *
     * @return the relationships
     */
    public List<Object> getRelationships()
    {
        return Collections.unmodifiableList(myRelationships);
    }

    /**
     * Gets the sub layers.
     *
     * @return the sub layers
     */
    public List<EsriLayerReference> getSubLayers()
    {
        return Collections.unmodifiableList(mySubLayers);
    }

    /**
     * Gets the time info.
     *
     * @return the time info
     */
    public EsriTimeInfo getTimeInfo()
    {
        return myTimeInfo;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Gets the type id field.
     *
     * @return the type id field
     */
    public Object getTypeIdField()
    {
        return myTypeIdField;
    }

    /**
     * Gets the types.
     *
     * @return the types
     */
    public Object getTypes()
    {
        return myTypes;
    }

    /**
     * Checks if is default visibility.
     *
     * @return true, if is default visibility
     */
    public boolean isDefaultVisibility()
    {
        return myDefaultVisibility;
    }

    /**
     * Checks if is checks for attachments.
     *
     * @return true, if is checks for attachments
     */
    public boolean isHasAttachments()
    {
        return myHasAttachments;
    }

    /**
     * Sets the capabilities.
     *
     * @param capabilities the new capabilities
     */
    public void setCapabilities(String capabilities)
    {
        myCapabilities = capabilities;
    }

    /**
     * Sets the copyright text.
     *
     * @param copyrightText the new copyright text
     */
    public void setCopyrightText(String copyrightText)
    {
        myCopyrightText = copyrightText;
    }

    /**
     * Sets the current ArcGIS version.
     *
     * @param currentVersion the new version
     */
    public void setCurrentVersion(String currentVersion)
    {
        myCurrentVersion = currentVersion;
    }

    /**
     * Sets the default visibility.
     *
     * @param defaultVisibility the new default visibility
     */
    public void setDefaultVisibility(boolean defaultVisibility)
    {
        myDefaultVisibility = defaultVisibility;
    }

    /**
     * Sets the definition expression.
     *
     * @param definitionExpression the new definition expression
     */
    public void setDefinitionExpression(String definitionExpression)
    {
        myDefinitionExpression = definitionExpression;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the display field.
     *
     * @param displayField the new display field
     */
    public void setDisplayField(String displayField)
    {
        myDisplayField = displayField;
    }

    /**
     * Sets the drawing info.
     *
     * @param drawingInfo the new drawing info
     */
    public void setDrawingInfo(EsriDrawingInfo drawingInfo)
    {
        myDrawingInfo = drawingInfo;
    }

    /**
     * Sets the extent.
     *
     * @param extent the new extent
     */
    public void setExtent(EsriExtent extent)
    {
        myExtent = extent;
    }

    /**
     * Sets the fields.
     *
     * @param fields the new fields
     */
    public void setFields(List<EsriField> fields)
    {
        myFields = fields;
    }

    /**
     * Sets the geometry type.
     *
     * @param geometryType the new geometry type
     */
    public void setGeometryType(EsriGeometryType geometryType)
    {
        myGeometryType = geometryType;
    }

    /**
     * Sets the checks for attachments.
     *
     * @param hasAttachments the new checks for attachments
     */
    public void setHasAttachments(boolean hasAttachments)
    {
        myHasAttachments = hasAttachments;
    }

    /**
     * Sets the html popup type.
     *
     * @param htmlPopupType the new html popup type
     */
    public void setHtmlPopupType(String htmlPopupType)
    {
        myHtmlPopupType = htmlPopupType;
    }

    /**
     * Sets the parent layer.
     *
     * @param parentLayer the new parent layer
     */
    public void setParentLayer(EsriLayerReference parentLayer)
    {
        myParentLayer = parentLayer;
    }

    /**
     * Sets the relationships.
     *
     * @param relationships the new relationships
     */
    public void setRelationships(List<Object> relationships)
    {
        myRelationships = relationships;
    }

    /**
     * Sets the sub layers.
     *
     * @param subLayers the new sub layers
     */
    public void setSubLayers(List<EsriLayerReference> subLayers)
    {
        mySubLayers = subLayers;
    }

    /**
     * Sets the time info.
     *
     * @param timeInfo the new time info
     */
    public void setTimeInfo(EsriTimeInfo timeInfo)
    {
        myTimeInfo = timeInfo;
    }

    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type)
    {
        myType = type;
    }

    /**
     * Sets the type id field.
     *
     * @param typeIdField the new type id field
     */
    public void setTypeIdField(Object typeIdField)
    {
        myTypeIdField = typeIdField;
    }

    /**
     * Sets the types.
     *
     * @param types the new types
     */
    public void setTypes(Object types)
    {
        myTypes = types;
    }

    /** The Enum for ESRI Geometry Types. */
    public enum EsriGeometryType
    {
        /** The ESRI type for envelope (Bounding box) geometries. */
        esriGeometryEnvelope("esriGeometryEnvelope"),

        /** The ESRI type for multi-point geometries. */
        esriGeometryMultipoint("esriGeometryMultipoint"),

        /** The ESRI type for point geometries. */
        esriGeometryPoint("esriGeometryPoint"),

        /** The ESRI type for polygon geometries. */
        esriGeometryPolygon("esriGeometryPolygon"),

        /** The ESRI type for polyline geometries. */
        esriGeometryPolyline("esriGeometryPolyline"),

        /** The default ESRI type for unknown geometry types. */
        esriGeometryUnknown("");

        /** My title. */
        private final String myTitle;

        /**
         * Returns the enum for the given value.
         *
         * @param value the value
         * @return the enum, or null
         */
        @JsonCreator
        public static EsriGeometryType fromString(String value)
        {
            return EnumUtilities.fromString(EsriGeometryType.class, value);
        }

        /**
         * Instantiates a new ESRI geometry type.
         *
         * @param title the title
         */
        EsriGeometryType(String title)
        {
            myTitle = title;
        }

        @Override
        public String toString()
        {
            return myTitle;
        }
    }
}
