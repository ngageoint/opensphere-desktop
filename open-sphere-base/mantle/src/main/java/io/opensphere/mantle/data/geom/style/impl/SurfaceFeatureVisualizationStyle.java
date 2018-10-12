package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.AbstractGroupHeightGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GroupRectangleGeometry;
import io.opensphere.core.geometry.GroupSpikeGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultZOrderRenderProperties;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationStyleCategory;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.style.FeatureCombinedGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.FeatureIndividualGeometryBuilderData;
import io.opensphere.mantle.data.geom.style.LocationVisualizationStyle;
import io.opensphere.mantle.data.geom.style.MutableVisualizationStyle;
import io.opensphere.mantle.data.geom.style.ParameterHint;
import io.opensphere.mantle.data.geom.style.StyleAltitudeReference;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameterFlags;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.CheckBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ColorChooserStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.EditorPanelVisibilityDependency;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedMiniStyleEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.GroupedStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.IntegerSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.style.impl.ui.ParameterVisibilityConstraint;
import io.opensphere.mantle.data.geom.style.impl.ui.StyleParameterEditorGroupPanel;

/**
 * The Class SurfaceFeatureVisualizationStyle.
 */
@SuppressWarnings("PMD.GodClass")
public class SurfaceFeatureVisualizationStyle extends AbstractFeatureVisualizationStyle implements LocationVisualizationStyle
{
    /** The Constant DEFAULT_WIDTH. */
    private static final int DEFAULT_WIDTH = 20;

    /** The Constant ourPropertyKeyPrefix. */
    public static final String ourPropertyKeyPrefix = "SurfaceFeatureVisualizationStyle";

    /** The Constant ourPowerPropertyKey. */
    public static final String ourPowerPropertyKey = ourPropertyKeyPrefix + ".Power";

    /** The Constant ourGridSizePropertyKey. */
    public static final String ourGridSizePropertyKey = ourPropertyKeyPrefix + ".GridSize";

    /** The Constant ourSpikeShapePropertyKey. */
    public static final String ourSpikeShapePropertyKey = ourPropertyKeyPrefix + ".SpikeShape";

    /** The Constant ourBaseWidthPropertyKey. */
    public static final String ourBaseWidthPropertyKey = ourPropertyKeyPrefix + ".BaseWidth";

    /** The Constant ourTopColorSameAsBase. */
    public static final String ourDifferntTopColorKey = ourPropertyKeyPrefix + ".DifferentTopColor";

    /** The Constant ourTopColorKey. */
    public static final String ourTopColorKey = ourPropertyKeyPrefix + ".TopColor";

    /** The Constant ourDefaultPowerParameter. */
    public static final VisualizationStyleParameter ourDefaultPowerParameter = new VisualizationStyleParameter(
            ourPowerPropertyKey, "Power", Float.valueOf(1.2f), Float.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /** The Constant ourDefaultGridSizeParameter. */
    public static final VisualizationStyleParameter ourDefaultGridSizeParameter = new VisualizationStyleParameter(
            ourGridSizePropertyKey, "Grid Size", Integer.valueOf(100), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultPowerParameter. */
    public static final VisualizationStyleParameter ourDefaultBaseWidthParamter = new VisualizationStyleParameter(
            ourBaseWidthPropertyKey, "Base Width", Integer.valueOf(DEFAULT_WIDTH), Integer.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultSpikeShapeParameter. */
    public static final VisualizationStyleParameter ourDefaultSpikeShapeParameter = new VisualizationStyleParameter(
            ourSpikeShapePropertyKey, "Spike Shape", SpikeShape.SPIKE, SpikeShape.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultTopColorSameAsBaseParameter. */
    public static final VisualizationStyleParameter ourDefaultTopColorSameAsBaseParameter = new VisualizationStyleParameter(
            ourDifferntTopColorKey, "Different Top Color", Boolean.FALSE, Boolean.class,
            new VisualizationStyleParameterFlags(false, false), ParameterHint.hint(false, false));

    /** The Constant ourDefaultTopColorParameter. */
    public static final VisualizationStyleParameter ourDefaultTopColorParameter = new VisualizationStyleParameter(ourTopColorKey,
            "Top Color", Color.white, Color.class, new VisualizationStyleParameterFlags(false, false),
            ParameterHint.hint(false, false));

    /**
     * Instantiates a new surface feature visualization style.
     *
     * @param tb the {@link Toolbox}
     */
    public SurfaceFeatureVisualizationStyle(Toolbox tb)
    {
        super(tb);
    }

    /**
     * Instantiates a new surface feature visualization style.
     *
     * @param tb the {@link Toolbox}
     * @param dtiKey the {@link DataTypeInfo}
     */
    public SurfaceFeatureVisualizationStyle(Toolbox tb, String dtiKey)
    {
        super(tb, dtiKey);
    }

    @Override
    public SurfaceFeatureVisualizationStyle clone()
    {
        return (SurfaceFeatureVisualizationStyle)super.clone();
    }

    @Override
    public void createCombinedGeometry(Set<Geometry> setToAddTo, FeatureCombinedGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        DefaultMeshScalableRenderProperties props = new DefaultMeshScalableRenderProperties(0, true, false);
        props.setBaseAltitude((float)getLift());
        Color topColor = getTopColor();
        Color typeColor = getColor();
        topColor = new Color(topColor.getRed(), topColor.getGreen(), topColor.getBlue(), typeColor.getAlpha());
        props.setColor(isTopColorDifferent() ? topColor : typeColor);
        props.setBaseColor(typeColor);
        props.setWidth(getBaseWidth() / (float)1000);
        props.setHeight(500);
        props.setHidden(!renderPropertyPool.getDataType().isVisible());

        AbstractGroupHeightGeometry.Builder builder = new AbstractGroupHeightGeometry.Builder();

        builder.setGridSize(getGridSize());
        builder.setPowerValue(getPower());
        builder.setRenderProperties(props);

        List<GeographicPosition> posList = New.linkedList();
        Iterator<FeatureIndividualGeometryBuilderData> itr = builderData.iterator();
        FeatureIndividualGeometryBuilderData iBuilder = null;
        long firstGeomId = 0;
        while (itr.hasNext())
        {
            iBuilder = itr.next();
            if (iBuilder != null && iBuilder.getVS() != null && iBuilder.getVS().isVisible()
                    && iBuilder.getMGS() instanceof MapLocationGeometrySupport)
            {
                if (firstGeomId == 0)
                {
                    firstGeomId = iBuilder.getGeomId();
                }
                MapLocationGeometrySupport mlgs = (MapLocationGeometrySupport)iBuilder.getMGS();

                StyleAltitudeReference altRef = getAltitudeReference();
                Altitude.ReferenceLevel refLevel = altRef.isAutomatic()
                        ? mlgs.followTerrain() ? Altitude.ReferenceLevel.TERRAIN : mlgs.getLocation().getAltitudeReference()
                        : altRef.getReference();
                GeographicPosition gp = new GeographicPosition(LatLonAlt.createFromDegreesMeters(mlgs.getLocation().getLatD(),
                        mlgs.getLocation().getLonD(), 0.0d, refLevel));

                posList.add(gp);
            }
        }
        builder.setDataModelId(firstGeomId);
        builder.setLocations(posList);

        if (getSpikeShape() == SpikeShape.RECTANGLE)
        {
            setToAddTo.add(new GroupRectangleGeometry(builder, new DefaultZOrderRenderProperties(0, false)));
        }
        else
        {
            setToAddTo.add(new GroupSpikeGeometry(builder, new DefaultZOrderRenderProperties(0, false)));
        }
    }

    @Override
    public void createIndividualGeometry(Set<Geometry> setToAddTo, FeatureIndividualGeometryBuilderData builderData,
            RenderPropertyPool renderPropertyPool)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Can't create individual geometry.");
    }

    @Override
    public VisualizationStyle deriveForType(String dtiKey)
    {
        SurfaceFeatureVisualizationStyle clone = clone();
        clone.setDTIKey(dtiKey);
        clone.initializeFromDataType();
        return clone;
    }

    @Override
    public AppliesTo getAppliesTo()
    {
        return AppliesTo.ALL_ELEMENTS;
    }

    /**
     * Gets the base width. ( 1 to 100 ), if outside of range will be clipped to
     * nearest range bound.
     *
     * @return the base width
     */
    public int getBaseWidth()
    {
        Integer val = (Integer)getStyleParameterValue(ourBaseWidthPropertyKey);
        return val == null || val.intValue() < 1 ? 1 : val.intValue();
    }

    @Override
    public Class<? extends MapGeometrySupport> getConvertedClassType()
    {
        return MapLocationGeometrySupport.class;
    }

    /**
     * Gets the grid size. ( 1 to 1000 ), if outside of range will be clipped to
     * nearest range bound.
     *
     * @return the grid size
     */
    public int getGridSize()
    {
        Integer val = (Integer)getStyleParameterValue(ourGridSizePropertyKey);
        return val == null || val.intValue() < 1 ? 1 : val.intValue();
    }

    @Override
    public GroupedMiniStyleEditorPanel getMiniUIPanel()
    {
        GroupedMiniStyleEditorPanel panel = super.getMiniUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDifferntTopColorKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get("Different Top Color"), style,
                ourDifferntTopColorKey, true));

        param = style.getStyleParameter(ourTopColorKey);
        ColorChooserStyleParameterEditorPanel ccPanel = new ColorChooserStyleParameterEditorPanel(
                StyleUtils.createBasicMiniPanelBuilder(param.getName()), style, ourTopColorKey, true, false);
        paramList.add(ccPanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, ccPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourDifferntTopColorKey, true, Boolean.TRUE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        param = style.getStyleParameter(ourPowerPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourPowerPropertyKey, true, false, 0.0f, 2.0f,
                new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)));

        param = style.getStyleParameter(ourGridSizePropertyKey);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourGridSizePropertyKey, true, true, 1, 500, null));

        param = style.getStyleParameter(ourBaseWidthPropertyKey);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(StyleUtils.createSliderMiniPanelBuilder(param.getName()), style,
                ourBaseWidthPropertyKey, true, true, 1, 500, null));

        param = style.getStyleParameter(ourSpikeShapePropertyKey);
        paramList.add(new ComboBoxStyleParameterEditorPanel(StyleUtils.createComboBoxMiniPanelBuilder(param.getName()), style,
                ourSpikeShapePropertyKey, true, false, false, Arrays.asList(SpikeShape.values())));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel(null, paramList, false, 1);
        panel.addGroupAtTop(paramGrp);

        return panel;
    }

    /**
     * Gets the power ( 0.0 to 2.0 ).
     *
     * @return the power
     */
    public float getPower()
    {
        Float val = (Float)getStyleParameterValue(ourPowerPropertyKey);
        return val == null ? 0.0f : val.floatValue();
    }

    /**
     * Gets the spike shape.
     *
     * @return the {@link SpikeShape}
     */
    public SpikeShape getSpikeShape()
    {
        return (SpikeShape)getStyleParameterValue(ourSpikeShapePropertyKey);
    }

    @Override
    public MapVisualizationStyleCategory getStyleCategory()
    {
        return MapVisualizationStyleCategory.LOCATION_FEATURE;
    }

    @Override
    public String getStyleDescription()
    {
        return "Feature visualization controls for surface.";
    }

    @Override
    public String getStyleName()
    {
        return "Surfaces";
    }

    /**
     * Gets the top color.
     *
     * @return the top color
     */
    public Color getTopColor()
    {
        return (Color)getStyleParameterValue(ourTopColorKey);
    }

    @Override
    public GroupedStyleParameterEditorPanel getUIPanel()
    {
        GroupedStyleParameterEditorPanel panel = super.getUIPanel();

        List<AbstractStyleParameterEditorPanel> paramList = New.list();
        MutableVisualizationStyle style = panel.getChangedStyle();

        VisualizationStyleParameter param = style.getStyleParameter(ourDifferntTopColorKey);
        paramList.add(new CheckBoxStyleParameterEditorPanel(PanelBuilder.get("Different Top Color"), style,
                ourDifferntTopColorKey, true));

        param = style.getStyleParameter(ourTopColorKey);
        ColorChooserStyleParameterEditorPanel ccPanel = new ColorChooserStyleParameterEditorPanel(
                PanelBuilder.get(param.getName()), style, ourTopColorKey, true, false);
        paramList.add(ccPanel);

        EditorPanelVisibilityDependency visDepend = new EditorPanelVisibilityDependency(panel, ccPanel);
        visDepend.addConstraint(new ParameterVisibilityConstraint(ourDifferntTopColorKey, true, Boolean.TRUE));
        visDepend.evaluateStyle();
        panel.addVisibilityDependency(visDepend);

        param = style.getStyleParameter(ourPowerPropertyKey);
        paramList.add(new FloatSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourPowerPropertyKey,
                true, false, 0.0f, 2.0f, new FloatSliderStyleParameterEditorPanel.BasicIntFloatConvertor(2, null)));

        param = style.getStyleParameter(ourGridSizePropertyKey);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourGridSizePropertyKey,
                true, true, 1, 500, null));

        param = style.getStyleParameter(ourBaseWidthPropertyKey);
        paramList.add(new IntegerSliderStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style,
                ourBaseWidthPropertyKey, true, true, 1, 500, null));

        param = style.getStyleParameter(ourSpikeShapePropertyKey);
        paramList.add(new ComboBoxStyleParameterEditorPanel(PanelBuilder.get(param.getName()), style, ourSpikeShapePropertyKey,
                true, false, false, Arrays.asList(SpikeShape.values())));

        StyleParameterEditorGroupPanel paramGrp = new StyleParameterEditorGroupPanel("Surface Style", paramList);
        panel.addGroup(paramGrp);

        return panel;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        setParameter(ourDefaultPowerParameter);
        setParameter(ourDefaultGridSizeParameter);
        setParameter(ourDefaultSpikeShapeParameter);
        setParameter(ourDefaultBaseWidthParamter);
        setParameter(ourDefaultTopColorSameAsBaseParameter);
        setParameter(ourDefaultTopColorParameter);
    }

    @Override
    public void initialize(Set<VisualizationStyleParameter> paramSet)
    {
        super.initialize(paramSet);
        paramSet.stream().filter(p -> p.getKey() != null && p.getKey().startsWith(ourPropertyKeyPrefix))
                .forEach(this::setParameter);
    }

    /**
     * Checks to see if the top color should be the same as the base.
     *
     * @return true, if top color should be the same as the base.
     */
    public boolean isTopColorDifferent()
    {
        Boolean val = (Boolean)getStyleParameterValue(ourDifferntTopColorKey);
        return val != null && val.booleanValue();
    }

    @Override
    public VisualizationStyle newInstance(Toolbox tb)
    {
        VisualizationStyle vs = new SurfaceFeatureVisualizationStyle(tb);
        vs.initialize();
        return vs;
    }

    /**
     * Gets the base width. ( 1 to 500 ), if outside of range will be clipped to
     * nearest range bound.
     *
     * @param baseWidth the base width ( 0 to 500 ).
     * @param source the source
     */
    public void setBaseWidth(int baseWidth, Object source)
    {
        setParameter(ourBaseWidthPropertyKey, Integer.valueOf(baseWidth < 1 ? 1 : baseWidth > 500 ? 500 : baseWidth), source);
    }

    /**
     * Sets the top color to be the same as the base color.
     *
     * @param topSameAsBase the top color to the same as the base color
     * @param source the source
     */
    public void setDifferentTopColor(boolean topSameAsBase, Object source)
    {
        setParameter(ourDifferntTopColorKey, Boolean.valueOf(topSameAsBase), source);
    }

    /**
     * Gets the grid size. ( 1 to 500 ), if outside of range will be clipped to
     * nearest range bound.
     *
     * @param gridSize the grid size ( 0 to 500 ).
     * @param source the source
     */
    public void setGridSize(int gridSize, Object source)
    {
        setParameter(ourGridSizePropertyKey, Integer.valueOf(gridSize < 1 ? 1 : gridSize > 500 ? 500 : gridSize), source);
    }

    /**
     * Sets the power ( must be in range 0 &lt;= power &lt;= 2.0 ) if outside
     * range will be clipped to nearest range bound.
     *
     * @param power the power ( 0.0 to 2.0 )
     * @param source the source of the change.
     */
    public void setPower(float power, Object source)
    {
        setParameter(ourPowerPropertyKey, Float.valueOf(power < 0.0 ? 0.0f : power > 2.0f ? 2.0f : power), source);
    }

    /**
     * Sets the spike shape.
     *
     * @param shape the {@link SpikeShape}
     * @param source the source
     */
    public void setSpikeShape(SpikeShape shape, Object source)
    {
        SpikeShape aShape = shape == null ? SpikeShape.SPIKE : shape;
        setParameter(ourSpikeShapePropertyKey, aShape, source);
    }

    /**
     * Sets the top color.
     *
     * @param c the top {@link Color}
     * @param source the source
     */
    public void setTopColor(Color c, Object source)
    {
        setParameter(ourTopColorKey, c, source);
    }

    @Override
    public boolean supportsLabels()
    {
        return false;
    }

    /**
     * The Enum SpikeShape.
     */
    public enum SpikeShape
    {
        /** The SPIKE. */
        SPIKE("Spike"),

        /** The RECTANGLE. */
        RECTANGLE("Rectangle");

        /** The Label. */
        private final String myLabel;

        /**
         * Instantiates a new spike shape.
         *
         * @param label the label
         */
        SpikeShape(String label)
        {
            myLabel = label;
        }

        @Override
        public String toString()
        {
            return myLabel;
        }
    }
}
