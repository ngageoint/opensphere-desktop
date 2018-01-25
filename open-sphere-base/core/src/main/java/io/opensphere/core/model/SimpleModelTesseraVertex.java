package io.opensphere.core.model;

import io.opensphere.core.math.Vector3d;

/** A simple vertex which contains a model position. */
public class SimpleModelTesseraVertex extends SimpleTesseraVertex<ModelPosition>
{
    /**
     * Constructor.
     *
     * @param coord The model position at this vertex.
     */
    public SimpleModelTesseraVertex(ModelPosition coord)
    {
        super(coord);
    }

    @Override
    public SimpleModelTesseraVertex adjustToModelCenter(Vector3d modelCenter)
    {
        ModelPosition adjustedVertex = new ModelPosition(getCoordinates().asVector3d().subtract(modelCenter));
        return new SimpleModelTesseraVertex(adjustedVertex);
    }
}
