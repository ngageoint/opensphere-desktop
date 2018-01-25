package io.opensphere.kml.mantle.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import de.micromata.opengis.kml.v_2_2_0.Model;
import io.opensphere.core.collada.ColladaParser;
import io.opensphere.core.collada.jaxb.ColladaModel;
import io.opensphere.core.collada.jaxb.Image;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.PolygonMeshGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.PolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolylineRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;

/**
 * Builds the collada models from a KMZ file.
 */
public class ColladaBuilder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ColladaBuilder.class);

    /**
     * Used to load images.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Constructs a new collada builder.
     *
     * @param dataRegistry Used to load the images.
     */
    public ColladaBuilder(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    /**
     * Builds the model geometries.
     *
     * @param stream The model input stream.
     * @param feature The feature.
     * @param kmlModel The KML model.
     * @param totalFeatures The total number of features in the kml.
     * @return the geometries
     */
    public Pair<ColladaModel, Collection<AbstractGeometry>> buildGeometries(InputStream stream, KMLFeature feature,
            Model kmlModel, int totalFeatures)
    {
        ColladaModel model = null;
        Collection<AbstractGeometry> geometries = New.list();
        try (InputStream is = stream)
        {
            model = createParser(feature, kmlModel, totalFeatures).parseModels(is, geometries);
        }
        catch (JAXBException | IOException e)
        {
            LOGGER.error(e, e);
        }
        List<AbstractGeometry> geoms = New.list();
        for (AbstractGeometry geometry : geometries)
        {
            if (!(geometry instanceof PolygonMeshGeometry) || !((PolygonMeshGeometry)geometry).getPositions().isEmpty())
            {
                geoms.add(geometry);
            }
        }

        return new Pair<>(model, geoms);
    }

    /**
     * Creates a new COLLADA parser.
     *
     * @param feature The feature.
     * @param kmlModel The KML model.
     * @param totalFeatures The total number of features in the kml.
     * @return the COLLADA parser
     */
    private ColladaParser createParser(KMLFeature feature, Model kmlModel, int totalFeatures)
    {
        int zOrder = ZOrderRenderProperties.TOP_Z - 100;
        PolylineRenderProperties lineProps = new DefaultPolylineRenderProperties(zOrder, true, false);
        PolygonMeshRenderProperties meshProps = new DefaultPolygonMeshRenderProperties(zOrder, true, false, true);
        meshProps.setLighting(LightingModelConfigGL.getDefaultLight());
        Constraints constraints = new Constraints(KMLSpatialTemporalUtils.getTimeConstraint(feature), null, null);
        ImageProvider<Pair<Image, UUID>> imageProvider = i -> loadImage(i, feature, kmlModel, totalFeatures);

        return new ColladaParser(lineProps, meshProps, constraints, imageProvider);
    }

    /**
     * Loads the image for the COLLADA image.
     *
     * @param colladaImage the COLLADA image
     * @param feature The feature.
     * @param kmlModel The KML model.
     * @param totalFeatures The total number of features in the kml.
     * @return the image, or null
     */
    private io.opensphere.core.image.Image loadImage(Pair<Image, UUID> colladaImage, KMLFeature feature, Model kmlModel,
            int totalFeatures)
    {
        io.opensphere.core.image.Image coreImage = null;

        // Get the input stream
        String path = colladaImage.getFirstObject().getInitFrom();
        Path modelDir = Paths.get(kmlModel.getLink().getHref()).getParent();
        Path imagePath = modelDir != null ? Paths.get(modelDir.toString(), path) : Paths.get(path);
        path = imagePath.normalize().toString().replace('\\', '/');
        InputStream stream = KMLDataRegistryHelper.queryAndReturn(myDataRegistry, feature.getDataSource(), path);

        // Read the image
        if (stream != null)
        {
            if (totalFeatures > 100)
            {
                stream = ImageUtil.shrinkImage(stream, 50000);
            }

            try
            {
                coreImage = ImageIOImage.read(stream);
            }
            catch (IOException e)
            {
                LOGGER.error(e, e);
            }
        }

        return coreImage;
    }
}
