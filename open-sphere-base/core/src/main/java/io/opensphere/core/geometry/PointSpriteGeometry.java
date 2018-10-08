package io.opensphere.core.geometry;

import java.util.Comparator;

import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.model.Position;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;

/**
 * A {@link PointGeometry} that can display an image rather than a simple point.
 */
public class PointSpriteGeometry extends PointGeometry implements ImageProvidingGeometry<PointSpriteGeometry>
{
    /** This holds on to the executor used for requesting data. */
    private final DataRequestAgent myDataRequestAgent = new DataRequestAgent();

    /** The image manager. */
    private final ImageManager myImageManager;

    /** Whether this geometry is sensitive to projection changes. */
    private final boolean myIsProjectionSensitive;

    /** Helper for providing images. */
    private final ImageProvidingGeometryHelper<PointSpriteGeometry> myImageProvidingGeometryHelper = new ImageProvidingGeometryHelper<>(
            this);

    /**
     * Constructor.
     *
     * @param builder The builder for the geometry.
     * @param renderProperties The render properties.
     * @param constraints Constraints on when the geometry is visible (may be
     *            <code>null</code>).
     */
    public PointSpriteGeometry(Builder<?> builder, PointRenderProperties renderProperties, Constraints constraints)
    {
        super(builder, renderProperties, constraints);
        myImageManager = builder.getImageManager();
        myIsProjectionSensitive = builder.isProjectionSensitive();
        Utilities.checkNull(myImageManager, "builder.getImageManager()");
    }

    @Override
    public void addObserver(io.opensphere.core.geometry.ImageProvidingGeometry.Observer<PointSpriteGeometry> observer)
    {
        myImageManager.addObserver(myImageProvidingGeometryHelper.getObserver(observer));
    }

    @Override
    public PointSpriteGeometry clone()
    {
        return (PointSpriteGeometry)super.clone();
    }

    @Override
    public Builder<? extends Position> createBuilder()
    {
        @SuppressWarnings("unchecked")
        Builder<Position> builder = (Builder<Position>)super.createBuilder();
        builder.setImageManager(getImageManager());
        builder.setProjectionSensitive(isProjectionSensitive());
        return builder;
    }

    @Override
    public PointSpriteGeometry derive(BaseRenderProperties renderProperties, Constraints constraints) throws ClassCastException
    {
        return new PointSpriteGeometry(createBuilder(), (PointRenderProperties)renderProperties, constraints);
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

    @Override
    public void removeObserver(io.opensphere.core.geometry.ImageProvidingGeometry.Observer<PointSpriteGeometry> observer)
    {
        ImageManager.Observer obs = myImageProvidingGeometryHelper.removeObserver(observer);
        if (obs != null)
        {
            myImageManager.removeObserver(obs);
        }
    }

    @Override
    public final void requestImageData()
    {
        requestImageData(null, TimeBudget.INDEFINITE);
    }

    @Override
    public final void requestImageData(Comparator<? super PointSpriteGeometry> comparator, TimeBudget timeBudget)
    {
        getImageManager().requestImageData(comparator, getObservable(), getDataRequestAgent().getDataRetrieverExecutor(),
                timeBudget);
    }

    @Override
    public boolean sharesImage()
    {
        return true;
    }

    @Override
    protected Builder<? extends Position> createRawBuilder()
    {
        return new Builder<>();
    }

    /**
     * Gets whether this geometry is sensitive to projection changes.
     *
     * @return whether this geometry is sensitive to projection changes
     */
    public boolean isProjectionSensitive()
    {
        return myIsProjectionSensitive;
    }

    /**
     * Get the observable (correctly cast {@code this}) for the image manager
     * observer.
     *
     * @return The observable.
     */
    protected PointSpriteGeometry getObservable()
    {
        return this;
    }

    /**
     * Builder for the geometry.
     *
     * @param <T> The position type associated with the geometry.
     */
    public static class Builder<T extends Position> extends PointGeometry.Builder<T>
    {
        /** The image provider. */
        private ImageManager myImageManager;

        /** Whether this geometry is sensitive to projection changes. */
        private boolean myIsProjectionSensitive;

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
         * Mutator for the imageManager.
         *
         * @param imageManager The imageManager to set.
         */
        public void setImageManager(ImageManager imageManager)
        {
            myImageManager = imageManager;
        }

        /**
         * Gets whether the geometry is sensitive to projection changes.
         *
         * @return whether the geometry is sensitive to projection changes
         */
        public boolean isProjectionSensitive()
        {
            return myIsProjectionSensitive;
        }

        /**
         * Sets whether the geometry is sensitive to projection changes.
         *
         * @param isProjectionSensitive whether the geometry is sensitive to
         *            projection changes
         */
        public void setProjectionSensitive(boolean isProjectionSensitive)
        {
            myIsProjectionSensitive = isProjectionSensitive;
        }
    }
}
