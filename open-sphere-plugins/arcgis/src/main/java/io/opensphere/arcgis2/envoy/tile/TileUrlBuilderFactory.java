package io.opensphere.arcgis2.envoy.tile;

import io.opensphere.arcgis2.model.ArcGISLayer;

/**
 * Creates the appropriate {@link TileUrlBuilder} for a given
 * {@link ArcGISLayer}.
 */
public final class TileUrlBuilderFactory
{
    /**
     * The export url builder.
     */
    private static final ExportUrlBuilder myExportBuilder = new ExportUrlBuilder();

    /**
     * The xyz url builder.
     */
    private static final XYZUrlBuilder myXyzBuilder = new XYZUrlBuilder();

    /**
     * The instance of this class.
     */
    private static final TileUrlBuilderFactory ourInstance = new TileUrlBuilderFactory();

    /**
     * The instance of this class.
     *
     * @return The instance.
     */
    public static TileUrlBuilderFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private TileUrlBuilderFactory()
    {
    }

    /**
     * Creates the appropriate builder for the specified layer.
     *
     * @param layer the layer.
     * @return The builder.
     */
    public TileUrlBuilder createBuilder(ArcGISLayer layer)
    {
        TileUrlBuilder builder = myExportBuilder;
        if (layer.isSingleFusedMapCache())
        {
            builder = myXyzBuilder;
        }

        return builder;
    }
}
