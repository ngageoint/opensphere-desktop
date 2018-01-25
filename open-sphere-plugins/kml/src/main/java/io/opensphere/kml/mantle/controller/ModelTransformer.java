package io.opensphere.kml.mantle.controller;

import java.awt.Color;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang3.StringUtils;

import de.micromata.opengis.kml.v_2_2_0.Model;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Orientation;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Scale;
import io.opensphere.core.MapManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.collada.jaxb.ColladaModel;
import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.geometry.renderproperties.TransformRenderProperties;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.ProjectionChangeSupport;
import io.opensphere.core.projection.ProjectionChangedEvent;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.math.VectorUtilities;
import io.opensphere.kml.common.model.KMLFeature;
import io.opensphere.kml.common.util.KMLDataRegistryHelper;
import io.opensphere.kml.common.util.KMLSpatialTemporalUtils;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;

/** Handles KML COLLADA models. */
@ThreadSafe
public class ModelTransformer extends AbstractKMLTransformer
{
    /** The map manager. */
    private final MapManager myMapManager;

    /** Listener for projection change events. */
    private final ProjectionChangeSupport.ProjectionChangeListener myProjectionChangeListener = this::handleProjectionChanged;

    /**
     * Builds the collada geometries.
     */
    private final ColladaBuilder myColladaBuilder;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public ModelTransformer(Toolbox toolbox)
    {
        super(toolbox);
        myMapManager = toolbox.getMapManager();
        myColladaBuilder = new ColladaBuilder(toolbox.getDataRegistry());
        toolbox.getMapManager().getProjectionChangeSupport().addProjectionChangeListener(myProjectionChangeListener);
    }

    @Override
    public void addFeatures(Collection<? extends KMLFeature> features, DataTypeInfo dataType)
    {
        List<KMLFeature> existingFeatures = getDataTypeKeyToFeatureMap().getOrDefault(dataType.getTypeKey(),
                Collections.emptyList());
        List<KMLFeature> newFeatures = features.stream().filter(f -> !getModels(f).isEmpty() && !existingFeatures.contains(f))
                .collect(Collectors.toList());
        if (!newFeatures.isEmpty())
        {
            List<LatLonAlt> locations = New.list();
            for (KMLFeature feature : newFeatures)
            {
                for (Model model : getModels(feature))
                {
                    Collection<AbstractGeometry> geometries = buildGeometries(feature, model, features.size());
                    feature.setGeometries(model, geometries);
                    publishGeometries(geometries, Collections.emptyList());
//                    System.out.println("Publishing Geometries : " + geometries.size());
//                    List<AbstractGeometry> toPublish = New.list(geometries);
//                    int batchSize = 30;
//                    for (int i = 0; i < geometries.size(); i += batchSize)
//                    {
//                        int endIndex = i + batchSize;
//                        if (endIndex >= toPublish.size())
//                        {
//                            endIndex = toPublish.size();
//                        }
////                        System.out.println("Publishing batch Geometries");
//                        publishGeometries(toPublish.subList(i, endIndex), Collections.emptyList());
//                        try
//                        {
//                            Thread.sleep(500);
//                        }
//                        catch (InterruptedException e)
//                        {
//                            break;
//                        }
//                    }

                    locations.add(KMLSpatialTemporalUtils.getLatLonAlt(model.getLocation(), model.getAltitudeMode()));
                }
            }
            updateVisibility(newFeatures);
            CollectionUtilities.multiMapAddAll(getDataTypeKeyToFeatureMap(), dataType.getTypeKey(), newFeatures, false);
            ((DefaultDataTypeInfo)dataType).addLocations(locations);
        }
    }

    @Override
    public void removeFeatures(Collection<? extends KMLFeature> features, String dataTypeKey)
    {
        Collection<KMLFeature> handledFeatures = features.stream().filter(f -> !getModels(f).isEmpty())
                .collect(Collectors.toSet());
        if (!handledFeatures.isEmpty())
        {
            CollectionUtilities.multiMapRemoveAll(getDataTypeKeyToFeatureMap(), dataTypeKey, handledFeatures);

            List<Geometry> geoms = handledFeatures.stream().flatMap(f -> getModelGeometries(f).stream())
                    .collect(Collectors.toList());
            publishGeometries(Collections.emptyList(), geoms);

            for (KMLFeature feature : handledFeatures)
            {
                for (Model model : getModels(feature))
                {
                    feature.clearGeometries(model);
                }
            }
        }
    }

    @Override
    public void setOpacity(DataTypeInfo dataTypeInfo, int opacity)
    {
        List<KMLFeature> features = getDataTypeKeyToFeatureMap().get(dataTypeInfo.getTypeKey());
        if (features != null)
        {
            for (KMLFeature feature : features)
            {
                for (Geometry geom : getModelGeometries(feature))
                {
                    RenderProperties renderProperties = geom.getRenderProperties();
                    if (renderProperties instanceof ColorRenderProperties)
                    {
                        ColorRenderProperties colorProps = (ColorRenderProperties)renderProperties;
                        Color color = colorProps.getColor();
                        colorProps.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity));
                    }
                }
            }
        }
    }

    @Override
    protected void setVisibility(KMLFeature feature, boolean isVisible)
    {
        for (Geometry geom : getModelGeometries(feature))
        {
            RenderProperties renderProperties = geom.getRenderProperties();
            if (renderProperties instanceof BaseRenderProperties)
            {
                ((BaseRenderProperties)renderProperties).setHidden(!isVisible);
            }
        }
    }

    /**
     * Handles a projection change.
     *
     * @param event the event
     */
    private void handleProjectionChanged(ProjectionChangedEvent event)
    {
        for (List<KMLFeature> features : getDataTypeKeyToFeatureMap().values())
        {
            for (KMLFeature feature : features)
            {
                for (Model model : getModels(feature))
                {
                    setTransform(model, feature.getGeometries(model));
                }
            }
        }
    }

    /**
     * Gets all the model geometries for the feature.
     *
     * @param feature the feature
     * @return the geometries
     */
    private Collection<Geometry> getModelGeometries(KMLFeature feature)
    {
        return getModels(feature).stream().flatMap(m -> feature.getGeometries(m).stream()).collect(Collectors.toList());
    }

    /**
     * Gets the model from the feature if there is one.
     *
     * @param feature the feature
     * @return the model, or null
     */
    private Collection<Model> getModels(KMLFeature feature)
    {
        Collection<Model> models = Collections.emptyList();
        if (feature.getFeature() instanceof Placemark)
        {
            de.micromata.opengis.kml.v_2_2_0.Geometry geometry = ((Placemark)feature.getFeature()).getGeometry();
            if (geometry instanceof Model)
            {
                models = Collections.singletonList((Model)geometry);
            }
            else if (geometry instanceof MultiGeometry)
            {
                models = CollectionUtilities.filterDowncast(((MultiGeometry)geometry).getGeometry(), Model.class);
            }
        }
        return models;
    }

    /**
     * Builds the model geometries.
     *
     * @param feature The feature.
     * @param model The model.
     * @param totalFeatures The total number of features in the kml.
     * @return the geometries
     */
    private Collection<AbstractGeometry> buildGeometries(KMLFeature feature, Model model, int totalFeatures)
    {
        Collection<AbstractGeometry> geoms = Collections.emptyList();

        InputStream inputStream = KMLDataRegistryHelper.queryAndReturn(getDataRegistry(), feature.getDataSource(),
                model.getLink().getHref());
        if (inputStream != null)
        {
            Pair<ColladaModel, Collection<AbstractGeometry>> result = myColladaBuilder.buildGeometries(inputStream, feature,
                    model, totalFeatures);

            ColladaModel colladaModel = result.getFirstObject();
            updateKmlScale(model, colladaModel);

            geoms = result.getSecondObject();
            setTransform(model, geoms);
        }

        return geoms;
    }

    /**
     * Updates the scale in the KML model to include the scale in the COLLADA
     * model.
     *
     * @param model the KML model
     * @param colladaModel the COLLADA model
     */
    private void updateKmlScale(Model model, ColladaModel colladaModel)
    {
        Scale kmlScale = model.getScale();
        if (colladaModel.getAsset() != null && colladaModel.getAsset().getUnit() != null)
        {
            double colladaScale = colladaModel.getAsset().getUnit().getMeter();
            kmlScale.setX(kmlScale.getX() * colladaScale);
            kmlScale.setY(kmlScale.getY() * colladaScale);
            kmlScale.setZ(kmlScale.getZ() * colladaScale);
        }

        if (colladaModel.getAsset() != null)
        {
            String upAxis = colladaModel.getAsset().getUpAxis();
            if (StringUtils.isEmpty(upAxis) || "Y_UP".equals(upAxis))
            {
                // Since the y axis is up we basically need to rotate around the
                // x axis.
                // So roll stays the same cause x axis doesn't move, tilt needs
                // to move up 90 degrees since y is moved from
                // pointing north to now pointing out of the earth. Heading
                // needs to move 180 degrees since positive z now points
                // south.
                Orientation orientation = model.getOrientation();
                orientation.setTilt(orientation.getTilt() + 90);
                orientation.setHeading(orientation.getHeading() - 180);
            }
        }
    }

    /**
     * Updates the geometries with a new transform created from the model.
     *
     * @param model the model
     * @param geometries the geometries to update
     */
    private void setTransform(Model model, Collection<? extends Geometry> geometries)
    {
        Matrix4d transform = getTransform(model);
        for (Geometry geom : geometries)
        {
            RenderProperties renderProperties = geom.getRenderProperties();
            if (renderProperties instanceof TransformRenderProperties)
            {
                ((TransformRenderProperties)renderProperties).setTransform(transform);
            }
        }
    }

    /**
     * Get the transform to put the model in the correct position and attitude.
     *
     * @param model The model.
     * @return The transform.
     */
    private Matrix4d getTransform(Model model)
    {
        double heading = model.getOrientation().getHeading();
        double pitch = model.getOrientation().getTilt();
        double roll = model.getOrientation().getRoll();
//        double heading = 0;
//        double pitch = 90;
//        double roll = 0;
        LatLonAlt location = KMLSpatialTemporalUtils.getLatLonAlt(model.getLocation(), model.getAltitudeMode());
        Vector3d scaleVector = new Vector3d(model.getScale().getX(), model.getScale().getY(), model.getScale().getZ());

        Vector3d modelVector = myMapManager.getProjection().convertToModel(new GeographicPosition(location), Vector3d.ORIGIN);

        return VectorUtilities.getModelTransform(modelVector, heading, pitch, roll, scaleVector);
    }
}
