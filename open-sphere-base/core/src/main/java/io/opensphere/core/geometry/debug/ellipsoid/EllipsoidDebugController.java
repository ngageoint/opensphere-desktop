package io.opensphere.core.geometry.debug.ellipsoid;

import io.opensphere.core.MapManager;
import io.opensphere.core.geometry.EllipsoidGeometry;
import io.opensphere.core.geometry.EllipsoidGeometryBuilder;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonMeshRenderProperties;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.ColorMaterialModeParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.FaceParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightModelVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.MaterialVectorParameterType;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Publishes the ellipsoid geometry to the globe when user requests it from the
 * {@link EllipsoidDebugUI}.
 */
public class EllipsoidDebugController implements Runnable
{
    /**
     * Used to publish the ellipsoids.
     */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * Used to build the geometry.
     */
    private final MapManager myMapManager;

    /**
     * Contains all the user inputs.
     */
    private final EllipsoidDebugModel myModel;

    /**
     * Used to store previous values.
     */
    private final PreferencesRegistry myPrefs;

    /**
     * Constructs a new debug controller.
     *
     * @param mapManager Used to build the geometry.
     * @param geometryRegistry Used to publish the ellipsoids.
     * @param prefsRegistry Used to save the inputs.
     */
    public EllipsoidDebugController(MapManager mapManager, GeometryRegistry geometryRegistry, PreferencesRegistry prefsRegistry)
    {
        myMapManager = mapManager;
        myGeometryRegistry = geometryRegistry;
        myPrefs = prefsRegistry;
        myModel = restoreModel();
    }

    /**
     * Gets the model to edit.
     *
     * @return The model to edit.
     */
    public EllipsoidDebugModel getModel()
    {
        return myModel;
    }

    /**
     * Publishes the ellipsoid to the globe.
     */
    @Override
    public void run()
    {
        saveModel();

        if (myModel.isRemovePrevious().get())
        {
            myGeometryRegistry.removeGeometriesForSource(this.getClass());
        }

        EllipsoidGeometryBuilder<GeographicPosition> builder = new EllipsoidGeometryBuilder<>(myMapManager);
        builder.setAxisAMeters(myModel.getAxisA().get());
        builder.setAxisBMeters(myModel.getAxisB().get());
        builder.setAxisCMeters(myModel.getAxisC().get());
        builder.setColor(ColorUtilities.opacitizeColor(myModel.getColor(), myModel.getOpacity().get()));
        builder.setHeading(myModel.getHeading().get());
        builder.setQuality(myModel.getQuality().get());

        LatLonAlt location = LatLonAlt.createFromDegreesMeters(myModel.getLatitude().get(), myModel.getLongitude().get(),
                myModel.getAltitude().get(), ReferenceLevel.TERRAIN);
        builder.setLocation(new GeographicPosition(location));

        builder.setPitch(myModel.getPitch().get());
        builder.setRoll(myModel.getRoll().get());

        DefaultPolygonMeshRenderProperties meshProps = new DefaultPolygonMeshRenderProperties(ZOrderRenderProperties.TOP_Z, true,
                true, false);
        if (myModel.isUseLighting().get())
        {
            meshProps.setLighting(getLightingModel());
        }
        EllipsoidGeometry geom = new EllipsoidGeometry(builder, meshProps, null);
        myGeometryRegistry.addGeometriesForSource(this.getClass(), New.list(geom));
    }

    /**
     * Gets the lighting model to test light rendering ellipsoids.
     *
     * @return The lighting model.
     */
    private LightingModelConfigGL getLightingModel()
    {
        LightingModelConfigGL.Builder builder = new LightingModelConfigGL.Builder();
        builder.setLightNumber(0);
        builder.setFace(FaceParameterType.FRONT);
        builder.setColorMaterialMode(ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE);

        final float[] ambientLight = { 0.50f, 0.50f, 0.50f, 1f };
        final float[] diffuseLight = { 1f, 1f, 1f, 1f };
        final float[] position = { -0.17f, 0.61f, 0.81f, 0f };
        final float[] specular = { 0.74f, 0.77f, 0.77f, 1f };
        final float[] specularReflectivity = { 0.9f, 0.9f, 0.9f, 1f };
        final float shininess = 80f;

        builder.addLightModelVectorParameter(LightModelVectorParameterType.LIGHT_MODEL_AMBIENT, ambientLight);
        builder.addLightParameterVector(LightVectorParameterType.DIFFUSE, diffuseLight);
        builder.addLightParameterVector(LightVectorParameterType.POSITION, position);
        builder.addLightParameterVector(LightVectorParameterType.SPECULAR, specular);
        builder.addMaterialVectorParameter(MaterialVectorParameterType.SPECULAR, specularReflectivity);
        builder.addMaterialShininessParameter(shininess);

        return new LightingModelConfigGL(builder);
    }

    /**
     * Restores the previous model values.
     *
     * @return The restored saved model.
     */
    private EllipsoidDebugModel restoreModel()
    {
        Preferences prefs = myPrefs.getPreferences(EllipsoidDebugController.class);
        EllipsoidDebugModel model = new EllipsoidDebugModel();
        model.getAltitude().set(prefs.getDouble("Altitude", model.getAltitude().get()));
        model.getAxisA().set(prefs.getDouble("AxisA", model.getAxisA().get()));
        model.getAxisB().set(prefs.getDouble("AxisB", model.getAxisB().get()));
        model.getAxisC().set(prefs.getDouble("AxisC", model.getAxisC().get()));
        model.setColor(ColorUtilities
                .convertFromColorString(prefs.getString("Color", ColorUtilities.convertToRGBAColorString(model.getColor()))));
        model.getHeading().set(prefs.getDouble("Heading", model.getHeading().get()));
        model.getLatitude().set(prefs.getDouble("Latitude", model.getLatitude().get()));
        model.getLongitude().set(prefs.getDouble("Longitude", model.getLongitude().get()));
        model.getOpacity().set(prefs.getInt("Opacity", model.getOpacity().get()));
        model.getPitch().set(prefs.getDouble("Pitch", model.getPitch().get()));
        model.getQuality().set(prefs.getInt("Quality", model.getQuality().get()));
        model.getRoll().set(prefs.getDouble("Roll", model.getRoll().get()));
        model.getAltitude().set(prefs.getDouble("Altitude", model.getAltitude().get()));
        model.isUseLighting().set(prefs.getBoolean("UseLighting", model.isUseLighting().get()));
        model.isRemovePrevious().set(prefs.getBoolean("RemovePrevious", model.isRemovePrevious().get()));

        return model;
    }

    /**
     * Saves the model to the preferences.
     */
    private void saveModel()
    {
        Preferences prefs = myPrefs.getPreferences(EllipsoidDebugController.class);
        prefs.putDouble("Altitude", myModel.getAltitude().get(), this);
        prefs.putDouble("AxisA", myModel.getAxisA().get(), this);
        prefs.putDouble("AxisB", myModel.getAxisB().get(), this);
        prefs.putDouble("AxisC", myModel.getAxisC().get(), this);
        prefs.putString("Color", ColorUtilities.convertToRGBAColorString(myModel.getColor()), this);
        prefs.putDouble("Heading", myModel.getHeading().get(), this);
        prefs.putDouble("Latitude", myModel.getLatitude().get(), this);
        prefs.putDouble("Longitude", myModel.getLongitude().get(), this);
        prefs.putInt("Opacity", myModel.getOpacity().get(), this);
        prefs.putDouble("Pitch", myModel.getPitch().get(), this);
        prefs.putInt("Quality", myModel.getQuality().get(), this);
        prefs.putDouble("Roll", myModel.getRoll().get(), this);
        prefs.putDouble("Altitude", myModel.getAltitude().get(), this);
        prefs.putBoolean("UseLighting", myModel.isUseLighting().get(), this);
        prefs.putBoolean("RemovePrevious", myModel.isRemovePrevious().get(), this);
    }
}
