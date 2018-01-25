package io.opensphere.city.transformer.sun;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.MapManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.animationhelper.RefreshListener;
import io.opensphere.core.animationhelper.TimeRefreshNotifier;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.ColorMaterialModeParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.FaceParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightModelVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.LightVectorParameterType;
import io.opensphere.core.geometry.renderproperties.LightingModelConfigGL.MaterialVectorParameterType;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.math.WGS84EarthConstants;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.GeographicBody3D;
import io.opensphere.core.util.lang.ThreadUtilities;
import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

/**
 * Changes the lighting model as the timeline changes.
 */
public class SunRenderer implements RefreshListener
{
    /**
     * The moon color scalar.
     */
    private static final float ourMoonColorScalar = .2f;

    /**
     * Used to get the camera location.
     */
    private final MapManager myMapManager;

    /**
     * The model to update.
     */
    private final LightModel myModel = new LightModel();

    /**
     * Notifies us when to recalculate sun position.
     */
    private final TimeRefreshNotifier myNotifier;

    /**
     * The previous time we updated the models.
     */
    private long myPreviousTime;

    /**
     * Used to get current time in timeline.
     */
    private final TimeManager myTimeManager;

    /**
     * Constructor.
     *
     * @param mapManager Used to get the camera location.
     * @param timeManager system time manager.
     * @param animationManager system animation manager.
     */
    public SunRenderer(MapManager mapManager, TimeManager timeManager, AnimationManager animationManager)
    {
        myMapManager = mapManager;
        myTimeManager = timeManager;
        myNotifier = new TimeRefreshNotifier(this, timeManager, animationManager);
        myModel.setLight(createLightingModel());
    }

    /**
     * Stops listening to the timeline.
     */
    public void close()
    {
        myNotifier.close();
    }

    /**
     * Gets the model.
     *
     * @return The model.
     */
    public LightModel getModel()
    {
        return myModel;
    }

    @Override
    public void refresh(boolean forceIt)
    {
        ThreadUtilities.runBackground(() ->
        {
            synchronized (this)
            {
                long time = myTimeManager.getActiveTimeSpans().getPrimary().get(0).getEnd();
                if (myPreviousTime != time)
                {
                    myPreviousTime = time;
                    LightingModelConfigGL lightModel = createLightingModel();
                    myModel.setLight(lightModel);
                }
            }
        });
    }

    @Override
    public void refreshNow()
    {
        refresh(true);
    }

    /**
     * Add the sun elevation.
     *
     * @param z The sun's zenith angle.
     * @param sunPos The suns current position.
     * @param cameraPos The location of the camera.
     * @return The suns position.
     */
    private LatLonAlt addSunElevation(double z, LatLonAlt sunPos, LatLonAlt cameraPos)
    {
        /**
         * about 100000 meters in a degree at 42 degrees north. Since we moved
         * the sun's position a degree away from the camera, we must move its
         * elevation a degree as well or the light models angle would not match
         * that of the sun's.
         */
        double metersPerDegree = GeographicBody3D.greatCircleDistanceM(cameraPos, sunPos, WGS84EarthConstants.RADIUS_MEAN_M);
        double elevation = metersPerDegree * Math.cos(Math.toRadians(z));
        return LatLonAlt.createFromDegreesMeters(sunPos.getLatD(), sunPos.getLonD(), elevation + cameraPos.getAltM(),
                ReferenceLevel.ELLIPSOID);
    }

    /**
     * Calculates the suns position and return the vector for the light model.
     *
     * @param color The color changes to make as the sun sets.
     * @return The vector for the light model.
     */
    private float[] calculateSunPos(float[] color)
    {
        Date endDate = myTimeManager.getActiveTimeSpans().getPrimary().get(0).getEndDate();
        GregorianCalendar time = new GregorianCalendar(TimeZone.getDefault());
        time.setTime(endDate);
        GeographicPosition cameraPos = myMapManager.getProjection().convertToPosition(
                myMapManager.getStandardViewer().getPosition().getLocation(), Altitude.ReferenceLevel.ELLIPSOID);
        AzimuthZenithAngle angles = SPA.calculateSolarPosition(time, cameraPos.getLatLonAlt().getLatD(),
                cameraPos.getLatLonAlt().getLonD(), 0, DeltaT.estimate(time));

        double a = angles.getAzimuth();
        double z = angles.getZenithAngle();
        Vector3d sunVector = null;
        // If day time.
        color[0] = 1;
        color[1] = 1;
        color[2] = 1;
        int pinkStart = 75;
        int sunGone = 95;
        int firstLight = 70;
        if (z > firstLight && z < sunGone)
        {
            if (z > pinkStart)
            {
                color[1] = (float)(1 * pinkStart / z);
            }
            color[2] = (float)(1 * firstLight / z);
        }

        LatLonAlt sunPos = getSunLatLon(a, cameraPos.getLatLonAlt());
        sunPos = addSunElevation(z, sunPos, cameraPos.getLatLonAlt());

        sunVector = myMapManager.getProjection().convertToModel(new GeographicPosition(sunPos), Vector3d.ORIGIN);
        Vector3d cameraModel = myMapManager.getProjection().convertToModel(cameraPos, Vector3d.ORIGIN);
        sunVector = sunVector.subtract(cameraModel);

        if (z > sunGone)
        {
            // Now its a moon
            color[0] = ourMoonColorScalar;
            color[1] = ourMoonColorScalar;
            color[2] = ourMoonColorScalar;
            sunVector = sunVector.multiply(-1);
        }

        return new float[] { (float)sunVector.getX(), (float)sunVector.getY(), (float)sunVector.getZ(), 0f };
    }

    /**
     * Calculates the suns position and return the vector for the light model.
     *
     * @return The vector for the light model.
     */
    @SuppressWarnings("unused")
    private float[] calculateSunPosFake()
    {
        Date endDate = myTimeManager.getActiveTimeSpans().getPrimary().get(0).getEndDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        double timeOfDay = hourOfDay * 100 + minute;
        // We want the elevation angle to be 0 deg at 0600 and 180 deg at 1800
        // for now to simulate sunset.
        double elevation = (timeOfDay - 600) / 2400 * 360;
        // 0 deg elevation is a lat 0 and lon of 0. 180 deg elevation is a lat
        // of 0 and lon -180;
        double longitude = elevation;
        if (elevation <= 180)
        {
            longitude = -elevation;
        }
        else
        {
            longitude = 360 - elevation;
        }

        GeographicPosition sunPosition = new GeographicPosition(LatLonAlt.createFromDegrees(0, longitude));
        Vector3d sunVector = myMapManager.getProjection().convertToModel(sunPosition, Vector3d.ORIGIN);

        return new float[] { (float)sunVector.getX(), (float)sunVector.getY(), (float)sunVector.getZ(), 0f };
    }

    /**
     * Creates the lighting model to use.
     *
     * @return the light model.
     */
    private LightingModelConfigGL createLightingModel()
    {
        LightingModelConfigGL.Builder builder = new LightingModelConfigGL.Builder();
        builder.setLightNumber(0);
        builder.setFace(FaceParameterType.FRONT);
        builder.setColorMaterialMode(ColorMaterialModeParameterType.AMBIENT_AND_DIFFUSE);

        float[] colorScalar = new float[3];
        final float[] position = calculateSunPos(colorScalar);
        final float[] ambientLight = { 0.15f, 0.15f, 0.15f, 1f };
        final float[] diffuseLight = { 1f * colorScalar[0], 1f * colorScalar[1], 1f * colorScalar[2], 1f };
        final float[] specular = { 0.74f * colorScalar[0], 0.77f * colorScalar[1], 0.77f * colorScalar[2], 1f };
        final float[] specularReflectivity = { 0.9f * colorScalar[0], 0.9f * colorScalar[1], 0.9f * colorScalar[2], 1f };
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
     * Gets the suns position based on its azimuth.
     *
     * @param a the azimuth.
     * @param original The original position the sun calculation was done at.
     * @return The sun position.
     */
    private LatLonAlt getSunLatLon(double a, LatLonAlt original)
    {
        LatLonAlt latLon = null;
        double latDelta = 0;
        double lonDelta = 0;
        if (a > 0 && a < 90)
        {
            lonDelta = Math.sin(Math.toRadians(a));
            latDelta = Math.cos(Math.toRadians(a));
        }
        else if (a > 90 && a < 180)
        {
            lonDelta = Math.cos(Math.toRadians(a - 90));
            latDelta = -Math.sin(Math.toRadians(a - 90));
        }
        else if (a > 180 && a < 270)
        {
            lonDelta = -Math.sin(Math.toRadians(a - 180));
            latDelta = -Math.cos(Math.toRadians(a - 180));
        }
        else if (a > 270 && a < 360)
        {
            lonDelta = -Math.cos(Math.toRadians(a - 270));
            latDelta = Math.sin(Math.toRadians(a - 270));
        }
        else if (a == 0 || a == 360)
        {
            latDelta = 1;
        }
        else if (a == 90)
        {
            lonDelta = 1;
        }
        else if (a == 180)
        {
            latDelta = -1;
        }
        else if (a == 270)
        {
            lonDelta = -1;
        }

        latLon = LatLonAlt.createFromDegrees(original.getLatD() + latDelta, original.getLonD() + lonDelta);
        return latLon;
    }
}
