package io.opensphere.infinity.json;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

/** Elasticsearch geometry filter JSON bean. */
@JsonPropertyOrder({ "shape", "relation" })
public class GeometryFilter
{
    /** The shape. */
    private Shape myShape;

    /** The relation. */
    private String myRelation;

    /**
     * Constructor.
     */
    public GeometryFilter()
    {
    }

    /**
     * Constructor.
     *
     * @param shape the shape
     * @param relation the relation
     */
    public GeometryFilter(Shape shape, String relation)
    {
        myShape = shape;
        myRelation = relation;
    }

    /**
     * Gets the shape.
     *
     * @return the shape
     */
    public Shape getShape()
    {
        return myShape;
    }

    /**
     * Sets the shape.
     *
     * @param shape the shape
     */
    public void setShape(Shape shape)
    {
        myShape = shape;
    }

    /**
     * Gets the relation.
     *
     * @return the relation
     */
    public String getRelation()
    {
        return myRelation;
    }

    /**
     * Sets the relation.
     *
     * @param relation the relation
     */
    public void setRelation(String relation)
    {
        myRelation = relation;
    }
}
