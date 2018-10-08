package io.opensphere.core.geometry;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.BoundingBox;
import io.opensphere.core.model.ColorArrayList;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTIntList;

/**
 * A geometry representing a polygon mesh.
 */
@SuppressWarnings("PMD.GodClass")
public class PolygonMeshGeometry extends AbstractColorGeometry implements ImageProvidingGeometry<PolygonMeshGeometry>
{
    /** This holds on to the executor used for requesting data. */
    private final DataRequestAgent myDataRequestAgent = new DataRequestAgent();

    /** The image manager. */
    private final ImageManager myImageManager;

    /** Helper for providing images. */
    private final ImageProvidingGeometryHelper<PolygonMeshGeometry> myImageProvidingGeometryHelper = new ImageProvidingGeometryHelper<>(
            this);

    /** The polygon mesh. */
    private final PolygonMesh myPolygonMesh;

    /**
     * Construct a PolygonMeshGeometry.
     *
     * @param builder builder for the geometry
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public PolygonMeshGeometry(Builder<? extends Position> builder, PolygonMeshRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);

        myPolygonMesh = new PolygonMesh();
        List<? extends Vector3d> normals = builder.getNormals();
        if (normals != null)
        {
            setNormals(normals);
        }
        List<? extends Color> colors = builder.getColors();
        if (colors != null)
        {
            setColors(colors);
        }
        List<? extends Position> positions = builder.getPositions();
        if (positions != null)
        {
            setPositions(positions);
        }
        PetrifyableTIntList indices = builder.getIndices();
        if (indices != null)
        {
            setIndices(indices);
        }

        List<? extends Vector2d> textCoords = builder.getTextureCoords();
        if (textCoords != null)
        {
            setTextureCoords(textCoords);
        }

        myPolygonMesh.setPolygonVertexCount(builder.getPolygonVertexCount());

        myImageManager = builder.getImageManager();

        validate();
    }

    /**
     * Constructor.
     *
     * @param builder The builder.
     * @param renderProperties The render properties.
     * @param constraints The constraints.
     */
    public PolygonMeshGeometry(MeshBuilder<? extends Position> builder, ColorRenderProperties renderProperties,
            Constraints constraints)
    {
        super(builder, renderProperties, constraints);

        myPolygonMesh = builder.getPolygonMesh();
        myImageManager = null;
    }

    @Override
    public void addObserver(io.opensphere.core.geometry.ImageProvidingGeometry.Observer<PolygonMeshGeometry> observer)
    {
        if (myImageManager != null)
        {
            myImageManager.addObserver(myImageProvidingGeometryHelper.getObserver(observer));
        }
    }

    @Override
    public PolygonMeshGeometry clone()
    {
        return (PolygonMeshGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.doCreateBuilder();
        builder.setIndices(getIndices());
        builder.setNormals(getNormals());
        builder.setColors(getColors());
        builder.setPolygonVertexCount(getPolygonVertexCount());
        builder.setPositions(getPositions());
        builder.setImageManager(getImageManager());
        builder.setTextureCoords(getTextureCoords());
        return builder;
    }

    @Override
    public PolygonMeshGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PolygonMeshGeometry(createBuilder(), (PolygonMeshRenderProperties)renderProperties, constraints);
    }

    /**
     * Access the colors.
     *
     * @return The colors.
     */
    public List<? extends Color> getColors()
    {
        return myPolygonMesh.getColors();
    }

    @Override
    public DataRequestAgent getDataRequestAgent()
    {
        return myDataRequestAgent;
    }

    @Override
    public ImageManager getImageManager()
    {
        return myImageManager;
    }

    /**
     * Get the indices.
     *
     * @return the indices
     */
    public PetrifyableTIntList getIndices()
    {
        return myPolygonMesh.getIndices();
    }

    /**
     * Get the normals.
     *
     * @return the normals
     */
    public List<? extends Vector3d> getNormals()
    {
        return myPolygonMesh.getNormals();
    }

    /**
     * Get the polygon mesh for this geometry.
     *
     * @return The polygon mesh.
     */
    public PolygonMesh getPolygonMesh()
    {
        return myPolygonMesh;
    }

    /**
     * Get the polygonVertexCount.
     *
     * @return the polygonVertexCount
     */
    public int getPolygonVertexCount()
    {
        return myPolygonMesh.getPolygonVertexCount();
    }

    /**
     * Get the positions.
     *
     * @return the positions
     */
    public List<? extends Position> getPositions()
    {
        return myPolygonMesh.getPositions();
    }

    @Override
    public Class<? extends Position> getPositionType()
    {
        return getPositions().get(0).getClass();
    }

    @Override
    public Position getReferencePoint()
    {
        return getPositions().get(0);
    }

    @Override
    public PolygonMeshRenderProperties getRenderProperties()
    {
        return (PolygonMeshRenderProperties)super.getRenderProperties();
    }

    /**
     * Get the texture coordinates.
     *
     * @return the texture coordinates.
     */
    public List<? extends Vector2d> getTextureCoords()
    {
        return myPolygonMesh.getTextureCoords();
    }

    @Override
    public <T extends Position> boolean overlaps(BoundingBox<T> boundingBox, double tolerance)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeObserver(io.opensphere.core.geometry.ImageProvidingGeometry.Observer<PolygonMeshGeometry> observer)
    {
        if (myImageManager != null)
        {
            ImageManager.Observer obs = myImageProvidingGeometryHelper.removeObserver(observer);
            if (obs != null)
            {
                myImageManager.removeObserver(obs);
            }
        }
    }

    @Override
    public void requestImageData()
    {
        requestImageData(null, TimeBudget.INDEFINITE);
    }

    @Override
    public void requestImageData(Comparator<? super PolygonMeshGeometry> comparator, TimeBudget timeBudget)
    {
        getImageManager().requestImageData(comparator, getObservable(), getDataRequestAgent().getDataRetrieverExecutor(),
                timeBudget);
    }

    @Override
    public boolean sharesImage()
    {
        return false;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>();
    }

    /**
     * Get the observable (correctly cast {@code this}) for the image manager
     * observer.
     *
     * @return The observable.
     */
    protected PolygonMeshGeometry getObservable()
    {
        return this;
    }

    /**
     * Set the colors.
     *
     * @param colors The colors to set.
     */
    protected final void setColors(List<? extends Color> colors)
    {
        myPolygonMesh.setColors(colors);
    }

    /**
     * Set the indices.
     *
     * @param indices the indices to set
     */
    protected final void setIndices(PetrifyableTIntList indices)
    {
        myPolygonMesh.setIndices(indices);
    }

    /**
     * Set the normals.
     *
     * @param normals the normals to set
     */
    protected final void setNormals(List<? extends Vector3d> normals)
    {
        myPolygonMesh.setNormals(normals);
    }

    /**
     * Set the polygonVertexCount.
     *
     * @param polygonVertexCount the polygonVertexCount to set
     */
    protected final void setPolygonVertexCount(int polygonVertexCount)
    {
        myPolygonMesh.setPolygonVertexCount(polygonVertexCount);
    }

    /**
     * Set the positions.
     *
     * @param positions the positions to set
     */
    protected final void setPositions(List<? extends Position> positions)
    {
        myPolygonMesh.setPositions(positions);
    }

    /**
     * Set the texture coords.
     *
     * @param coords the coords to set
     */
    protected final void setTextureCoords(List<? extends Vector2d> coords)
    {
        myPolygonMesh.setTextureCoords(coords);
    }

    /**
     * Validate the sanity of the geometry.
     */
    protected final void validate()
    {
        myPolygonMesh.validate();
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> position type for the geometry.
     */
    public static class Builder<T extends Position> extends AbstractGeometry.Builder
    {
        /** Colors for the mesh. */
        private List<? extends Color> myColors;

        /**
         * The image manager.
         */
        private ImageManager myImageManager;

        /** Indices to describe the usage of the positions. */
        private PetrifyableTIntList myIndices;

        /** Normals for the mesh. */
        private List<? extends Vector3d> myNormals;

        /** Number of vertices per polygon in the mesh. */
        private int myPolygonVertexCount;

        /** Vertices within the mesh. */
        private List<? extends T> myPositions;

        /**
         * The texture coordinates for this geometry.
         */
        private List<? extends Vector2d> myTextureCoords;

        /**
         * Access the colors.
         *
         * @return The colors.
         */
        public List<? extends Color> getColors()
        {
            return myColors;
        }

        /**
         * Accessor for the imageManager.
         *
         * @return The imageManager.
         */
        public ImageManager getImageManager()
        {
            return myImageManager;
        }

        /**
         * Access the indices.
         *
         * @return the indices
         */
        public PetrifyableTIntList getIndices()
        {
            return myIndices;
        }

        /**
         * Access the normals.
         *
         * @return the normals
         */
        public List<? extends Vector3d> getNormals()
        {
            return myNormals;
        }

        /**
         * Access the polygonVertexCount.
         *
         * @return the polygonVertexCount
         */
        public int getPolygonVertexCount()
        {
            return myPolygonVertexCount;
        }

        /**
         * Access the positions.
         *
         * @return the positions
         */
        public List<? extends T> getPositions()
        {
            return myPositions;
        }

        /**
         * Gets the texture coordinates for this geometry.
         *
         * @return Th texture coordinates.
         */
        public List<? extends Vector2d> getTextureCoords()
        {
            return myTextureCoords;
        }

        /**
         * Set the colors.
         *
         * @param colors The colors to set.
         */
        public void setColors(List<? extends Color> colors)
        {
            myColors = colors;
        }

        /**
         * Mutator for the imageManager.
         *
         * @param imageManager The imageManager to set.
         */
        public void setImageManager(ImageManager imageManager)
        {
            myImageManager = imageManager;
        }

        /**
         * Set the indices.
         *
         * @param indices the indices to set
         */
        public void setIndices(PetrifyableTIntList indices)
        {
            myIndices = indices;
        }

        /**
         * Set the normals.
         *
         * @param normals the normals to set
         */
        public void setNormals(List<? extends Vector3d> normals)
        {
            myNormals = normals;
        }

        /**
         * Set the polygonVertexCount.
         *
         * @param polygonVertexCount the polygonVertexCount to set
         */
        public void setPolygonVertexCount(int polygonVertexCount)
        {
            myPolygonVertexCount = polygonVertexCount;
        }

        /**
         * Set the positions.
         *
         * @param positions the positions to set
         */
        public void setPositions(List<? extends T> positions)
        {
            myPositions = positions;
        }

        /**
         * Sets the texture coordinates for this geometry.
         *
         * @param textureCoords The texture coordinates.
         */
        public void setTextureCoords(List<? extends Vector2d> textureCoords)
        {
            myTextureCoords = textureCoords;
        }
    }

    /**
     * Builder for the geometry that takes an input polygon mesh.
     *
     * @param <T> position type for the geometry.
     */
    public static class MeshBuilder<T extends Position> extends AbstractGeometry.Builder
    {
        /** The polygon mesh. */
        private PolygonMesh myPolygonMesh;

        /**
         * Get the polygon mesh.
         *
         * @return The polygon mesh.
         */
        public PolygonMesh getPolygonMesh()
        {
            return myPolygonMesh;
        }

        /**
         * Set the polygon mesh.
         *
         * @param polygonMesh The polygon mesh.
         */
        public void setPolygonMesh(PolygonMesh polygonMesh)
        {
            myPolygonMesh = polygonMesh;
        }
    }

    /**
     * Specification of the polygon mesh, which may be shared between multiple
     * geometries.
     */
    public static class PolygonMesh
    {
        /** Colors for the mesh. */
        private List<Color> myColors;

        /** Indices to describe the usage of the positions. */
        private PetrifyableTIntList myIndices;

        /** Normals for the mesh. */
        private List<? extends Vector3d> myNormals;

        /** Number of vertices per polygon in the mesh. */
        private int myPolygonVertexCount;

        /** Vertices within the mesh. */
        private List<? extends Position> myPositions;

        /** Texture coordinates for the mesh. */
        private List<? extends Vector2d> myTextureCoords;

        /**
         * Access the colors.
         *
         * @return The colors.
         */
        public List<? extends Color> getColors()
        {
            return myColors;
        }

        /**
         * Get the indices.
         *
         * @return the indices
         */
        public PetrifyableTIntList getIndices()
        {
            return myIndices;
        }

        /**
         * Get the normals.
         *
         * @return the normals
         */
        public List<? extends Vector3d> getNormals()
        {
            return myNormals;
        }

        /**
         * Get the polygonVertexCount.
         *
         * @return the polygonVertexCount
         */
        public int getPolygonVertexCount()
        {
            return myPolygonVertexCount;
        }

        /**
         * Get the positions.
         *
         * @return the positions
         */
        public List<? extends Position> getPositions()
        {
            return myPositions;
        }

        /**
         * Get the normals.
         *
         * @return the normals
         */
        public List<? extends Vector2d> getTextureCoords()
        {
            return myTextureCoords;
        }

        /**
         * Set the colors.
         *
         * @param colors The colors to set.
         */
        protected final void setColors(List<? extends Color> colors)
        {
            if (myColors != null)
            {
                throw new IllegalStateException("Colors cannot be set more than once.");
            }
            myColors = ColorArrayList.getColorArrayList(colors);
        }

        /**
         * Set the indices.
         *
         * @param indices the indices to set
         */
        protected final void setIndices(PetrifyableTIntList indices)
        {
            if (myIndices != null)
            {
                throw new IllegalStateException("Indices cannot be set more than once.");
            }
            indices.petrify();
            myIndices = indices;
        }

        /**
         * Set the normals.
         *
         * @param normals the normals to set
         */
        protected final void setNormals(List<? extends Vector3d> normals)
        {
            if (myNormals != null)
            {
                throw new IllegalStateException("Normals cannot be set more than once.");
            }
            myNormals = New.unmodifiableList(normals);
        }

        /**
         * Set the polygonVertexCount.
         *
         * @param polygonVertexCount the polygonVertexCount to set
         */
        protected final void setPolygonVertexCount(int polygonVertexCount)
        {
            if (myPolygonVertexCount != 0)
            {
                throw new IllegalStateException("Polygon vertex count cannot be set more than once.");
            }
            myPolygonVertexCount = polygonVertexCount;
        }

        /**
         * Set the positions.
         *
         * @param positions the positions to set
         */
        protected final void setPositions(List<? extends Position> positions)
        {
            if (myPositions != null)
            {
                throw new IllegalStateException("Positions cannot be set more than once.");
            }
            myPositions = New.unmodifiableList(positions);
        }

        /**
         * Set the texture coordinates.
         *
         * @param coords the texture coordinates to set
         */
        protected final void setTextureCoords(List<? extends Vector2d> coords)
        {
            if (myTextureCoords != null)
            {
                throw new IllegalStateException("Texture coordinates cannot be set more than once.");
            }
            myTextureCoords = New.unmodifiableList(coords);
        }

        /**
         * Validate the sanity of the geometry.
         */
        protected final void validate()
        {
            if (myIndices == null)
            {
                if (myPositions != null && myPositions.size() % myPolygonVertexCount != 0)
                {
                    throw new IllegalArgumentException("Size of positions [" + myPositions.size()
                            + "] is not a multiple of the vertex count [" + myPolygonVertexCount + "].");
                }
            }
            else
            {
                if (myIndices.size() % myPolygonVertexCount != 0)
                {
                    throw new IllegalArgumentException("Size of indices [" + myIndices.size()
                            + "] is not a multiple of the vertex count [" + myPolygonVertexCount + "].");
                }
                if (myPositions == null)
                {
                    throw new IllegalArgumentException("Positions must be supplied if indices are supplied.");
                }
                for (int i = 0; i < myIndices.size(); ++i)
                {
                    int value = myIndices.get(i);
                    if (value < 0 || value > myPositions.size())
                    {
                        throw new IllegalArgumentException(
                                "Index supplied [" + value + "] is out of range [0," + myPositions.size() + ").");
                    }
                }
            }

            if (myNormals != null)
            {
                if (myPositions == null)
                {
                    throw new IllegalArgumentException("Positions must be supplied if normals are supplied.");
                }
                if (myPositions.size() != myNormals.size())
                {
                    throw new IllegalArgumentException("Size of positions [" + myPositions.size()
                            + "] must be equal to size of normals [" + myNormals.size() + "].");
                }
            }
        }
    }
}
