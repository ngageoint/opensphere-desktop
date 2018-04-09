//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * There is precisely one positively directed and one negatively directed node
 * in the boundary of every edge. The negatively and positively directed nodes
 * correspond to the start and end nodes respectively. The optional coboundary
 * of an edge is a circular sequence of directed faces which are incident on
 * this edge in document order. Faces which use a particular boundary edge in
 * its positive orientation appear with positive orientation on the coboundary
 * of the same edge. In the 2D case, the orientation of the face on the left of
 * the edge is "+"; the orientation of the face on the right on its right is
 * "-". An edge may optionally be realised by a 1-dimensional (curve) geometric
 * primitive.
 * 
 * <p>
 * Java class for EdgeType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="EdgeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractTopoPrimitiveType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}directedNode" maxOccurs="2" minOccurs="2"/>
 *         &lt;element ref="{http://www.opengis.net/gml}directedFace" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}curveProperty" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EdgeType", propOrder = { "directedNode", "directedFace", "curveProperty" })
public class EdgeType extends AbstractTopoPrimitiveType
{

    @XmlElement(required = true)
    protected List<DirectedNodePropertyType> directedNode;

    protected List<DirectedFacePropertyType> directedFace;

    protected CurvePropertyType curveProperty;

    /**
     * Gets the value of the directedNode property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the directedNode property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDirectedNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DirectedNodePropertyType }
     * 
     * 
     */
    public List<DirectedNodePropertyType> getDirectedNode()
    {
        if (directedNode == null)
        {
            directedNode = new ArrayList<DirectedNodePropertyType>();
        }
        return this.directedNode;
    }

    /**
     * Gets the value of the directedFace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the directedFace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDirectedFace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DirectedFacePropertyType }
     * 
     * 
     */
    public List<DirectedFacePropertyType> getDirectedFace()
    {
        if (directedFace == null)
        {
            directedFace = new ArrayList<DirectedFacePropertyType>();
        }
        return this.directedFace;
    }

    /**
     * Gets the value of the curveProperty property.
     * 
     * @return possible object is {@link CurvePropertyType }
     * 
     */
    public CurvePropertyType getCurveProperty()
    {
        return curveProperty;
    }

    /**
     * Sets the value of the curveProperty property.
     * 
     * @param value allowed object is {@link CurvePropertyType }
     * 
     */
    public void setCurveProperty(CurvePropertyType value)
    {
        this.curveProperty = value;
    }

}
