//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.22 at 10:22:41 AM MST 
//

package com.bitsys.fade.mist.state.v4;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import net.opengis.kml._220.CameraType;

/**
 * 
 * A complex type in which the details of the Map projection and camera location
 * are encapsulated.
 * 
 * 
 * <p>
 * Java class for MapType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="MapType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Camera"/>
 *         &lt;element name="projection" type="{http://www.bit-sys.com/mist/state/v4}ProjectionType"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MapType", propOrder = {

})
public class MapType
{

    @XmlElement(name = "Camera", namespace = "http://www.opengis.net/kml/2.2", required = true)
    protected CameraType camera;

    @XmlElement(required = true)
    @XmlSchemaType(name = "token")
    protected ProjectionType projection;

    /**
     * 
     * The specifications of the camera location, expressed as a KML CameraType
     * element, including location, tilt, roll, and zoom.
     * 
     * 
     * @return possible object is {@link CameraType }
     * 
     */
    public CameraType getCamera()
    {
        return camera;
    }

    /**
     * Sets the value of the camera property.
     * 
     * @param value allowed object is {@link CameraType }
     * 
     */
    public void setCamera(CameraType value)
    {
        this.camera = value;
    }

    public boolean isSetCamera()
    {
        return (this.camera != null);
    }

    /**
     * Gets the value of the projection property.
     * 
     * @return possible object is {@link ProjectionType }
     * 
     */
    public ProjectionType getProjection()
    {
        return projection;
    }

    /**
     * Sets the value of the projection property.
     * 
     * @param value allowed object is {@link ProjectionType }
     * 
     */
    public void setProjection(ProjectionType value)
    {
        this.projection = value;
    }

    public boolean isSetProjection()
    {
        return (this.projection != null);
    }

}
