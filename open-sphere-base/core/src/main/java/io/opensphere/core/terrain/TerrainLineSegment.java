package io.opensphere.core.terrain;

import io.opensphere.core.projection.AbstractGeographicProjection.GeographicTesseraVertex;

/**
 * A line segment which is part of a globe's terrain.
 */
public class TerrainLineSegment
{
    /** First vertex. */
    private GeographicTesseraVertex myVertexA;

    /** First vertex. */
    private GeographicTesseraVertex myVertexB;

    /** Construct me. */
    public TerrainLineSegment()
    {
    }

    /**
     * Construct me.
     *
     * @param vertA First vertex.
     * @param vertB Second vertex.
     */
    public TerrainLineSegment(GeographicTesseraVertex vertA, GeographicTesseraVertex vertB)
    {
        myVertexA = vertA;
        myVertexB = vertB;
    }

    /**
     * Construct a copy of the given segment.
     *
     * @param line segment to copy.
     */
    public TerrainLineSegment(TerrainLineSegment line)
    {
        myVertexA = line.getVertexA();
        myVertexB = line.getVertexB();
    }

    /**
     * Get the first vertex.
     *
     * @return first vertex.
     */
    public GeographicTesseraVertex getVertexA()
    {
        return myVertexA;
    }

    /**
     * Get the second vertex.
     *
     * @return second vertex.
     */
    public GeographicTesseraVertex getVertexB()
    {
        return myVertexB;
    }

    /**
     * Set the first vertex.
     *
     * @param vertexA first vertex to set.
     */
    public void setVertexA(GeographicTesseraVertex vertexA)
    {
        myVertexA = vertexA;
    }

    /**
     * Set the second vertex.
     *
     * @param vertexB second vertex to set.
     */
    public void setVertexB(GeographicTesseraVertex vertexB)
    {
        myVertexB = vertexB;
    }
}
