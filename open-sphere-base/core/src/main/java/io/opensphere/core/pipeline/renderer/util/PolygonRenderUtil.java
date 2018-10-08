package io.opensphere.core.pipeline.renderer.util;

import java.awt.Color;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import gnu.trove.list.TIntList;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.pipeline.processor.PolygonMeshData;
import io.opensphere.core.pipeline.renderer.AbstractRenderer;
import io.opensphere.core.pipeline.util.GL2Utilities;

/** Utility for common polygon/polygon mesh rendering. */
public final class PolygonRenderUtil
{
    /**
     * Helper method that sends the vertex commands to GL for rendering a
     * polygon mesh.
     *
     * @param gl The OpenGL interface.
     * @param tc The mesh data.
     */
    public static void drawPolygonMesh(GL gl, PolygonMeshData tc)
    {
        List<? extends Vector3d> normals = tc.getVertexData().getNormals();
        List<? extends Vector3d> modelCoords = tc.getVertexData().getModelCoords();
        List<? extends Vector2d> textCoords = tc.getVertexData().getTextureCoords();
        TIntList modelIndices = tc.getModelIndices();
        List<? extends Color> colors = tc.getVertexData().getColors();

        int numVertices = modelIndices == null ? modelCoords.size() : modelIndices.size();

        int drawMode;
        switch (tc.getTesseraVertexCount())
        {
            case AbstractRenderer.TRIANGLE_VERTEX_COUNT:
                drawMode = GL.GL_TRIANGLES;
                break;
            case AbstractRenderer.QUAD_VERTEX_COUNT:
                drawMode = GL2GL3.GL_QUADS;
                break;
            case AbstractRenderer.TRIANGLE_STRIP_VERTEX_COUNT:
                drawMode = GL.GL_TRIANGLE_STRIP;
                break;
            default:
                drawMode = GL2.GL_POLYGON;
                break;
        }

        gl.getGL2().glBegin(drawMode);
        try
        {
            for (int i = 0; i < numVertices; ++i)
            {
                drawVertex(gl, i, modelIndices, modelCoords, normals, colors, textCoords);
            }
        }
        finally
        {
            gl.getGL2().glEnd();
        }
    }

    /**
     * Set up the necessary GL parameters.
     *
     * @param gl The OpenGL interface.
     * @param mode Draw or pick mode.
     * @param depthTest {@code true} when depth testing should be on. Typically,
     *            depth testing should be on for polygons with terrain, so that
     *            the triangles appear correctly. For screen polygons depth
     *            testing should be off so that they always render regardless of
     *            their location in model coordinates.
     * @param linesOn should be true when the lines debug feature is on.
     */
    public static void setupGL(GL gl, RenderMode mode, boolean depthTest, boolean linesOn)
    {
        if (mode == AbstractGeometry.RenderMode.DRAW)
        {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            if (linesOn)
            {
                gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
            }
            else
            {
                gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
            }
        }
        else
        {
            gl.getGL2().glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
        }

        if (depthTest)
        {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }
        else
        {
            gl.glEnable(GL.GL_DEPTH_TEST);
        }

        gl.glEnable(GL.GL_CULL_FACE);
    }

    /**
     * Draws a single vertex for the polygon.
     *
     * @param gl The OpenGL interface.
     * @param vertexIndex The index of the vertex we are drawing.
     * @param modelIndices The model indices.
     * @param modelCoords The model coordinates.
     * @param normals The normals.
     * @param colors The colors.
     * @param textCoords The texture coordinates.
     */
    private static void drawVertex(GL gl, int vertexIndex, TIntList modelIndices, List<? extends Vector3d> modelCoords,
            List<? extends Vector3d> normals, List<? extends Color> colors, List<? extends Vector2d> textCoords)
    {
        Vector3d normal = null;
        Vector3d modelCoord = null;
        Vector2d textCoord = null;
        int index;
        if (modelIndices == null)
        {
            index = vertexIndex;
        }
        else
        {
            index = modelIndices.get(vertexIndex);
        }

        modelCoord = modelCoords.get(index);
        if (normals != null)
        {
            normal = normals.get(index);
        }

        if (textCoords != null)
        {
            textCoord = textCoords.get(index);
        }

        if (normal != null)
        {
            gl.getGL2().glNormal3f((float)normal.getX(), (float)normal.getY(), (float)normal.getZ());
        }
        if (colors != null && textCoord == null && index >= 0 && index < colors.size())
        {
            GL2Utilities.glColor(gl.getGL2(), colors.get(index));
        }
        else if (textCoord != null)
        {
            gl.getGL2().glTexCoord2f((float)textCoord.getX(), (float)textCoord.getY());
        }
        gl.getGL2().glVertex3d((float)modelCoord.getX(), (float)modelCoord.getY(), (float)modelCoord.getZ());
    }

    /** Disallow instantiation. */
    private PolygonRenderUtil()
    {
    }
}
