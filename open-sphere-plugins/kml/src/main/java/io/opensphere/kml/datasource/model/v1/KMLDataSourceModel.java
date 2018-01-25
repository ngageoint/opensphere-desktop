package io.opensphere.kml.datasource.model.v1;

import java.util.Collection;

import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.swing.input.model.NameModel;
import io.opensphere.core.util.swing.input.model.TextModel;
import io.opensphere.core.util.swing.input.model.WrappedModel;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.ScalingMethod;

/**
 * KML data source model.
 */
public class KMLDataSourceModel extends WrappedModel<KMLDataSource>
{
    /** The path. */
    private final TextModel myPath = new TextModel();

    /** The source name. */
    private final NameModel mySourceName = new NameModel();

    /** Whether to include the data source in the timeline. */
    private final BooleanModel myIncludeInTimeline = new BooleanModel();

    /** Whether to auto-refresh the data source. */
    private final BooleanModel myAutoRefresh = new BooleanModel();

    /** The refresh rate in seconds. */
    private final IntegerModel myRefreshRate = new IntegerModel(1, 99999);

    /** The point type. */
    private final ChoiceModel<PointType> myPointType = new ChoiceModel<>(PointType.values());

    /** The feature altitude. */
    private final ChoiceModel<FeatureAltitude> myFeatureAltitude = new ChoiceModel<>(FeatureAltitude.values());

    /** The polygon fill. */
    private final ChoiceModel<PolygonFill> myPolygonFill = new ChoiceModel<>(PolygonFill.values());

    /** The show labels. */
    private final BooleanModel myShowLabels = new BooleanModel();

    /** The scaling method. */
    private final ChoiceModel<ScalingMethod> myScalingMethod = new ChoiceModel<>(ScalingMethod.values());

    /**
     * Constructor.
     *
     * @param disallowedNames The disallowed names
     */
    public KMLDataSourceModel(Collection<String> disallowedNames)
    {
        // Some business logic
        myAutoRefresh.addListener((observable, oldValue, newValue) -> setRefreshRateEnabled());
        mySourceName.setDisallowedNames(disallowedNames);

        myPath.setNameAndDescription("Path", "The file or URL path to the KML file");
        mySourceName.setNameAndDescription("Name", "The name of the layer");
        myIncludeInTimeline.setNameAndDescription("Include in Timeline", "Whether the layer will be included in the timeline");
        myAutoRefresh.setNameAndDescription("Auto Refresh", "Whether to automatically refresh the layer");
        myRefreshRate.setNameAndDescription("Rate (sec)", "The automatic refresh rate in seconds");
        myPointType.setNameAndDescription("Point Type", "How to display points on the map");
        myFeatureAltitude.setNameAndDescription("Feature Altitude", "Where to display features relative to the terrain");
        myPolygonFill.setNameAndDescription("Polygon Fill", "Whether to fill polygons that don't have a fill specified");
        myShowLabels.setNameAndDescription("Show Labels", "Whether to show labels for features");
        myScalingMethod.setNameAndDescription("Scaling Method", "How to scale icons and labels");

        addModel(myPath);
        addModel(mySourceName);
        addModel(myIncludeInTimeline);
        addModel(myAutoRefresh);
        addModel(myRefreshRate);
        addModel(myPointType);
        addModel(myFeatureAltitude);
        addModel(myPolygonFill);
        addModel(myShowLabels);
        addModel(myScalingMethod);
    }

    /**
     * Gets the auto refresh.
     *
     * @return the auto refresh
     */
    public BooleanModel getAutoRefresh()
    {
        return myAutoRefresh;
    }

    /**
     * Gets the feature altitude.
     *
     * @return the feature altitude
     */
    public ChoiceModel<FeatureAltitude> getFeatureAltitude()
    {
        return myFeatureAltitude;
    }

    /**
     * Gets the include in timeline.
     *
     * @return the include in timeline
     */
    public BooleanModel getIncludeInTimeline()
    {
        return myIncludeInTimeline;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public TextModel getPath()
    {
        return myPath;
    }

    /**
     * Gets the point type.
     *
     * @return the point type
     */
    public ChoiceModel<PointType> getPointType()
    {
        return myPointType;
    }

    /**
     * Gets the polygon fill.
     *
     * @return the polygon fill
     */
    public ChoiceModel<PolygonFill> getPolygonFill()
    {
        return myPolygonFill;
    }

    /**
     * Gets the refresh rate.
     *
     * @return the refresh rate
     */
    public IntegerModel getRefreshRate()
    {
        return myRefreshRate;
    }

    /**
     * Gets the scaling method.
     *
     * @return the scaling method
     */
    public ChoiceModel<ScalingMethod> getScalingMethod()
    {
        return myScalingMethod;
    }

    /**
     * Gets the show labels.
     *
     * @return the show labels
     */
    public BooleanModel getShowLabels()
    {
        return myShowLabels;
    }

    /**
     * Gets the source name.
     *
     * @return the source name
     */
    public TextModel getSourceName()
    {
        return mySourceName;
    }

    @Override
    public void setEnabled(boolean isEnabled)
    {
        super.setEnabled(isEnabled);
        setRefreshRateEnabled();
    }

    @Override
    protected void updateDomainModel(KMLDataSource domainModel)
    {
        domainModel.setPath(myPath.get());
        domainModel.setName(mySourceName.get());
        domainModel.setIncludeInTimeline(myIncludeInTimeline.get().booleanValue());
        int refreshRate = myRefreshRate.get().intValue();
        if (!myAutoRefresh.get().booleanValue())
        {
            refreshRate = -refreshRate;
        }
        domainModel.setRefreshRate(refreshRate);
        domainModel.setUseIcons(myPointType.get() == PointType.ICON);
        domainModel.setClampToTerrain(myFeatureAltitude.get() == FeatureAltitude.CLAMP_TO_TERRAIN);
        domainModel.setPolygonsFilled(myPolygonFill.get() == PolygonFill.FILLED);
        domainModel.setShowLabels(myShowLabels.get().booleanValue());
        domainModel.setScalingMethod(myScalingMethod.get());
    }

    @Override
    protected void updateViewModel(KMLDataSource domainModel)
    {
        myPath.set(domainModel.getPath());
        mySourceName.set(domainModel.getName());
        myIncludeInTimeline.set(Boolean.valueOf(domainModel.isIncludeInTimeline()));
        myAutoRefresh.set(Boolean.valueOf(domainModel.getRefreshRate() > 0));
        int refreshRate = domainModel.getRefreshRate();
        if (refreshRate == 0)
        {
            // Default
            refreshRate = 60;
        }
        else if (refreshRate < 0)
        {
            refreshRate = -refreshRate;
        }
        myRefreshRate.set(Integer.valueOf(refreshRate));
        myPointType.set(domainModel.isUseIcons() ? PointType.ICON : PointType.DOT);
        myFeatureAltitude.set(domainModel.isClampToTerrain() ? FeatureAltitude.CLAMP_TO_TERRAIN : FeatureAltitude.AS_PROVIDED);
        myPolygonFill.set(domainModel.isPolygonsFilled() ? PolygonFill.FILLED : PolygonFill.UNFILLED);
        myShowLabels.set(Boolean.valueOf(domainModel.isShowLabels()));
        myScalingMethod.set(domainModel.getScalingMethod());
    }

    /**
     * Updates the enabled state of the refresh rate based on the auto refresh
     * setting.
     */
    private void setRefreshRateEnabled()
    {
        myRefreshRate.setEnabled(myAutoRefresh.isEnabled() && myAutoRefresh.get() != null && myAutoRefresh.get().booleanValue());
    }

    /** Feature altitude enum. */
    private enum FeatureAltitude
    {
        /** As Provided. */
        AS_PROVIDED("As Provided"),

        /** Clamp to Terrain. */
        CLAMP_TO_TERRAIN("Clamp to Terrain");

        /** The display string. */
        private String myDisplay;

        /**
         * Constructor.
         *
         * @param display The display string
         */
        FeatureAltitude(String display)
        {
            myDisplay = display;
        }

        @Override
        public String toString()
        {
            return myDisplay;
        }
    }

    /** Point type enum. */
    private enum PointType
    {
        /** Icons. */
        ICON("Icons (When Provided)"),

        /** Dots. */
        DOT("Dots");

        /** The display string. */
        private String myDisplay;

        /**
         * Constructor.
         *
         * @param display The display string
         */
        PointType(String display)
        {
            myDisplay = display;
        }

        @Override
        public String toString()
        {
            return myDisplay;
        }
    }

    /** Polygon fill enum. */
    private enum PolygonFill
    {
        /** Unfilled. */
        UNFILLED("Unfilled"),

        /** Filled. */
        FILLED("Filled");

        /** The display string. */
        private String myDisplay;

        /**
         * Constructor.
         *
         * @param display The display string
         */
        PolygonFill(String display)
        {
            myDisplay = display;
        }

        @Override
        public String toString()
        {
            return myDisplay;
        }
    }
}
