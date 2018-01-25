package io.opensphere.core.collada;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.opensphere.core.collada.jaxb.Geometry;
import io.opensphere.core.collada.jaxb.Input;
import io.opensphere.core.collada.jaxb.Mesh;
import io.opensphere.core.collada.jaxb.Source;
import io.opensphere.core.collada.jaxb.Vertices;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.ModelPosition;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/** Utilities for parsing COLLADA models. */
public final class ColladaUtilities
{
    /**
     * Transform an array of normal data into {@link Vector3d}s.
     *
     * @param normalOffset The normal offset.
     * @param offsetCount The number of offsets there are into the primitives
     *            array.
     * @param normalData The source data.
     * @param primitives The indices into the source data.
     * @param matrix Optional transform matrix to be applied to the geometries'
     *            coordinates.
     * @return The normals.
     */
    public static List<Vector3d> getNormals(int normalOffset, int offsetCount, float[] normalData, int[] primitives,
            Matrix4d matrix)
    {
        List<Vector3d> normals;
        if (normalData == null)
        {
            normals = null;
        }
        else
        {
            normals = New.list(primitives.length / offsetCount);
            for (int i = normalOffset; i < primitives.length; i += offsetCount)
            {
                int index = primitives[i];
                normals.add(new Vector3d(normalData[index * 3], normalData[index * 3 + 1], normalData[index * 3 + 2]));
            }
            if (matrix != null)
            {
                Matrix4d rot = matrix.clone();
                rot.setTranslation(0, 0, 0);
                for (int index = 0; index < normals.size(); ++index)
                {
                    normals.set(index, rot.mult(normals.get(index)));
                }
            }
        }
        return normals;
    }

    /**
     * Transform an array of data into {@link ModelPosition}s.
     *
     * @param vertexOffset The vertex offset.
     * @param offsetCount The number of offsets there are into the primitives
     *            array.
     * @param positionData The source data.
     * @param primitives The indices into the source data.
     * @param matrix Optional transform matrix to be applied to the geometries'
     *            coordinates.
     * @return The positions.
     */
    public static List<ModelPosition> getPositions(int vertexOffset, int offsetCount, float[] positionData, int[] primitives,
            Matrix4d matrix)
    {
        List<ModelPosition> positions = New.list(primitives.length / offsetCount);
        for (int i = vertexOffset; i < primitives.length; i += offsetCount)
        {
            int index = primitives[i];
            positions.add(new ModelPosition(positionData[index * 3], positionData[index * 3 + 1], positionData[index * 3 + 2]));
        }
        if (matrix != null)
        {
            for (int index = 0; index < positions.size(); ++index)
            {
                positions.set(index, new ModelPosition(matrix.mult(positions.get(index).asVector3d())));
            }
        }
        return positions;
    }

    /**
     * Reads data from the matching source in the geometry.
     *
     * @param inputs the inputs
     * @param primitives the primitives
     * @param geometry the geometry
     * @param semantic the semantic
     * @return the source data
     */
    public static List<float[]> getSourceData(Collection<Input> inputs, int[] primitives, Geometry geometry, String semantic)
    {
        int offsetCount = inputs.size();
        List<float[]> data = New.list(primitives.length / offsetCount);

        Optional<Input> inputResult = inputs.stream().filter(i -> semantic.equals(i.getSemantic())).findAny();
        if (inputResult.isPresent())
        {
            Input input = inputResult.get();

            String sourceId = StringUtilities.removePrefix(input.getSource(), "#");
            Optional<Source> sourceResult = geometry.getMesh().getSources().stream().filter(s -> sourceId.equals(s.getId()))
                    .findAny();
            if (!sourceResult.isPresent())
            {
                Optional<Vertices> vertex = geometry.getMesh().getVertices().stream().filter(v -> sourceId.equals(v.getId()))
                        .findAny();
                if (vertex.isPresent())
                {
                    Optional<Input> positionInput = vertex.get().getInputs().stream()
                            .filter(i -> "POSITION".equals(i.getSemantic())).findAny();
                    if (positionInput.isPresent())
                    {
                        String posSourceId = StringUtilities.removePrefix(positionInput.get().getSource(), "#");
                        sourceResult = geometry.getMesh().getSources().stream().filter(s -> posSourceId.equals(s.getId()))
                                .findAny();
                    }
                }
            }

            if (sourceResult.isPresent())
            {
                Source source = sourceResult.get();

                float[] floatArray = source.getFloatArray();
                int stride = source.getTechniqueCommon().getAccessor().getStride();
                for (int i = input.getOffset(); i < primitives.length; i += offsetCount)
                {
                    int index = primitives[i];

                    float[] coordinate = new float[stride];
                    int startIndex = index * stride;
                    for (int c = 0; c < stride; c++)
                    {
                        coordinate[c] = floatArray[startIndex + c];
                    }

                    data.add(coordinate);
                }
            }
        }

        return data;
    }

    /**
     * Find the source whose id matches the given tag, and return its data.
     *
     * @param mesh The mesh.
     * @param tag The tag.
     * @return The data from the source, or {@code null} if a matching source
     *         was not found.
     */
    public static float[] getDataFromSources(Mesh mesh, String tag)
    {
        float[] positionData = null;
        for (Source source : mesh.getSources())
        {
            if (("#" + source.getId()).equals(tag))
            {
                positionData = source.getFloatArray();
            }
        }
        return positionData;
    }

    /**
     * Get a map of "vertices" ids to "source" ids for a particular semantic.
     *
     * @param mesh The input mesh.
     * @param semantic The input semantic of interest.
     * @return The map.
     */
    public static Map<String, String> getVerticesIdToSourceIdMap(Mesh mesh, String semantic)
    {
        Map<String, String> vertexToPositionIdMap = New.map();
        for (Vertices vertices : mesh.getVertices())
        {
            vertices.getInputs().stream().filter(i -> semantic.equals(i.getSemantic()))
                    .forEach(i -> vertexToPositionIdMap.put("#" + vertices.getId(), i.getSource()));
        }
        return vertexToPositionIdMap;
    }

    /**
     * Parses the color string into a Color.
     *
     * @param colorString the color string
     * @return the Color
     */
    public static Color parseColor(String colorString)
    {
        String[] components = colorString.split(" ");
        int r = parseColorComponent(components[0]);
        int g = parseColorComponent(components[1]);
        int b = parseColorComponent(components[2]);
        int a = parseColorComponent(components[3]);
        return new Color(r, g, b, a);
    }

    /**
     * Parses a color component.
     *
     * @param c the component
     * @return the value (0-255)
     */
    private static int parseColorComponent(String c)
    {
        return (int)Math.round(Double.parseDouble(c) * 255);
    }

    /** Disallow instantiation. */
    private ColladaUtilities()
    {
    }
}
