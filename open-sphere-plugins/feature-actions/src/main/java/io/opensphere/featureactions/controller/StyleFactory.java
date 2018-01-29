package io.opensphere.featureactions.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.featureactions.model.Action;
import io.opensphere.featureactions.model.LabelAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.style.FeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.VisualizationStyleParameter;
import io.opensphere.mantle.data.geom.style.impl.AbstractFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.AbstractPathVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.DynamicEllipseFeatureVisualization;
import io.opensphere.mantle.data.geom.style.impl.IconFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PointFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolygonFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.PolylineFeatureVisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.StyleUtils;
import io.opensphere.mantle.util.MantleToolboxUtils;

/** Creates mantle styles from feature actions. */
public class StyleFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StyleFactory.class);

    /**
     * Applies the appropriate label styles on the created style.
     */
    private final LabelApplier myLabelApplier;

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public StyleFactory(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myLabelApplier = new LabelApplier();
    }

    /**
     * Creates a style for the given actions.
     *
     * @param actions the actions
     * @param dataType the data type
     * @param defaultStyle the default style
     * @return the style
     */
    public FeatureVisualizationStyle newStyle(Collection<? extends Action> actions, DataTypeInfo dataType,
            FeatureVisualizationStyle defaultStyle)
    {
        FeatureVisualizationStyle style = null;
        FeatureVisualizationStyle defaultStyleCopy = clone(defaultStyle, dataType);
        boolean containsLabelAction = false;
        if (actions.size() == 1)
        {
            Action action = actions.iterator().next();
            if (action instanceof StyleAction)
            {
                style = newStyleStyle((StyleAction)action, dataType, defaultStyleCopy);
            }
            else if (action instanceof LabelAction)
            {
                containsLabelAction = true;
                style = addLabelStyle((LabelAction)action, dataType, defaultStyleCopy);
            }
        }
        else
        {
            Map<Class<?>, Action> classToAction = New.map();
            for (Action action : actions)
            {
                if (action instanceof StyleAction)
                {
                    StyleAction previousAction = (StyleAction)classToAction.get(action.getClass());
                    classToAction.put(action.getClass(), merge(previousAction, (StyleAction)action));
                }
                else
                {
                    classToAction.put(action.getClass(), action);
                }
            }
            StyleAction styleAction = (StyleAction)classToAction.get(StyleAction.class);
            LabelAction labelAction = (LabelAction)classToAction.get(LabelAction.class);

            // The idea here is to create the style one first and then attach
            // the label settings to it, if possible
            if (styleAction != null)
            {
                style = newStyleStyle(styleAction, dataType, defaultStyleCopy);
            }
            if (labelAction != null)
            {
                containsLabelAction = true;
                if (style == null)
                {
                    style = defaultStyleCopy;
                }
                style = addLabelStyle(labelAction, dataType, style);
            }
        }

        myLabelApplier.applyLabel(style, containsLabelAction, defaultStyleCopy);

        return style;
    }

    /**
     * Merges two style actions together.
     *
     * @param action1 the first action
     * @param action2 the second action (takes priority)
     * @return the merged action
     */
    private StyleAction merge(StyleAction action1, StyleAction action2)
    {
        // action 2 wins by default
        StyleAction merged = action2;
        if (action1 != null)
        {
            StyleOptions options1 = action1.getStyleOptions();
            StyleOptions options2 = action2.getStyleOptions();
            // If one of them is color-only
            if (options1.getStyle() == null || options2.getStyle() == null)
            {
                merged = new StyleAction();
                merged.getStyleOptions().setColor(options1.getStyle() == null ? options1.getColor() : options2.getColor());
                merged.getStyleOptions().setStyle(Utilities.getNonNull(options1.getStyle(), options2.getStyle(), null));

                if (options2.getStyle() != null && options2.hasSizeBeenSet())
                {
                    merged.getStyleOptions().setSize(options2.getSize());
                }
                else if (options1.getStyle() != null && options1.hasSizeBeenSet())
                {
                    merged.getStyleOptions().setSize(options1.getSize());
                }

                merged.getStyleOptions().setIconId(options1.getStyle() != null ? options1.getIconId() : options2.getIconId());
            }
        }
        return merged;
    }

    /**
     * Creates a style for the given StyleAction.
     *
     * @param styleAction the StyleAction
     * @param dataType the data type
     * @param defaultStyle the default style
     * @return the style
     */
    private FeatureVisualizationStyle newStyleStyle(StyleAction styleAction, DataTypeInfo dataType,
            FeatureVisualizationStyle defaultStyle)
    {
        FeatureVisualizationStyle style = null;

        StyleOptions styleOptions = styleAction.getStyleOptions();
        if (styleOptions.getStyle() != null)
        {
            if (!(defaultStyle instanceof AbstractPathVisualizationStyle))
            {
                switch (styleOptions.getStyle())
                {
                    case POINT:
                        style = newPointStyle(dataType, styleOptions);
                        break;
                    case ICON:
                        style = newIconStyle(dataType, styleOptions, defaultStyle);
                        break;
                    case ELLIPSE:
                        style = newEllipseStyle(dataType, styleOptions, false);
                        break;
                    case ELLIPSE_WITH_CENTER:
                        style = newEllipseStyle(dataType, styleOptions, true);
                        break;
                    case SQUARE:
                        break;
                    case TRIANGLE:
                        break;
                    default:
                        break;
                }
            }
            else
            {
                style = newPathStyle(dataType, styleOptions, (AbstractPathVisualizationStyle)defaultStyle);
            }
        }
        else
        {
            style = defaultStyle;
        }

        if (style != null)
        {
            style.setParameter(AbstractFeatureVisualizationStyle.COLOR_PROPERTY_KEY, styleOptions.getColor(), this);
        }

        return style;
    }

    /**
     * Creates a new path style.
     *
     * @param dataType The data type to create the style for.
     * @param styleOptions The style options.
     * @param defaultStyle The default style.
     * @return The default style modified with valid options from style options.
     */
    private FeatureVisualizationStyle newPathStyle(DataTypeInfo dataType, StyleOptions styleOptions,
            AbstractPathVisualizationStyle defaultStyle)
    {
        AbstractPathVisualizationStyle style = null;
        if (defaultStyle instanceof PolylineFeatureVisualizationStyle)
        {
            style = getStyleFromConfig(PolylineFeatureVisualizationStyle.class, dataType);
        }
        else if (defaultStyle instanceof PolygonFeatureVisualizationStyle)
        {
            style = getStyleFromConfig(PolygonFeatureVisualizationStyle.class, dataType);
        }
        else
        {
            style = getStyleFromConfig(defaultStyle.getClass(), dataType);
        }

        style.setDTIKey(dataType.getTypeKey());
        style.initialize();
        style.initializeFromDataType();
        if (styleOptions.hasSizeBeenSet())
        {
            defaultStyle.setLineWidth(styleOptions.getSize(), this);
        }
        return style;
    }

    /**
     * Creates a new point style.
     *
     * @param dataType the data type
     * @param styleOptions style options
     * @return the style
     */
    private PointFeatureVisualizationStyle newPointStyle(DataTypeInfo dataType, StyleOptions styleOptions)
    {
        PointFeatureVisualizationStyle style = getStyleFromConfig(PointFeatureVisualizationStyle.class, dataType);
        if (styleOptions.hasSizeBeenSet())
        {
            style.setPointSize(styleOptions.getSize(), this);
        }
        return style;
    }

    /**
     * Creates a new icon style.
     *
     * @param dataType the data type
     * @param styleOptions style options
     * @param defaultStyle the default style
     * @return the style
     */
    private IconFeatureVisualizationStyle newIconStyle(DataTypeInfo dataType, StyleOptions styleOptions,
            FeatureVisualizationStyle defaultStyle)
    {
        IconStyle style = new IconStyle(myToolbox, dataType.getTypeKey(), styleOptions.getIconId());
        style.initialize();
        style.initializeFromDataType();
        if (defaultStyle instanceof IconFeatureVisualizationStyle)
        {
            style.initialize(defaultStyle);
        }

        float size;
        if (styleOptions.hasSizeBeenSet())
        {
            size = styleOptions.getSize();
        }
        else
        {
            if (defaultStyle instanceof IconFeatureVisualizationStyle)
            {
                size = ((IconFeatureVisualizationStyle)defaultStyle).getIconSize();
            }
            else
            {
                PointFeatureVisualizationStyle original = getStyleFromConfig(PointFeatureVisualizationStyle.class, dataType);
                size = original.getPointSize();
            }
        }
        style.setIconSize(size, this);

        return style;
    }

    /**
     * Creates a new ellipse style.
     *
     * @param dataType the data type
     * @param styleOptions style options
     * @param showCenter whether to show the center point
     * @return the style
     */
    private DynamicEllipseFeatureVisualization newEllipseStyle(DataTypeInfo dataType, StyleOptions styleOptions,
            boolean showCenter)
    {
        DynamicEllipseFeatureVisualization style = getStyleFromConfig(DynamicEllipseFeatureVisualization.class, dataType);
        style.setShowCenterPoint(showCenter, this);
        return style;
    }

    /**
     * Gets the style for the style class including configuration settings.
     *
     * @param <T> the style type
     * @param styleClass the style class
     * @param dataType the data type
     * @return the style
     */
    @SuppressWarnings("unchecked")
    private <T extends FeatureVisualizationStyle> T getStyleFromConfig(Class<T> styleClass, DataTypeInfo dataType)
    {
        T style = null;
        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(myToolbox);
        VisualizationStyle configStyle = mantleToolbox.getVisualizationStyleController()
                .getStyleForEditorWithConfigValues(styleClass, MapLocationGeometrySupport.class, dataType.getParent(), dataType);
        if (configStyle != null)
        {
            style = (T)configStyle.clone();
        }
        else
        {
            LOGGER.error("Failed to look up style for " + styleClass + " " + dataType);
        }
        return style;
    }

    /**
     * Adds label style info to a copy of the given style.
     *
     * @param labelAction the LabelAction
     * @param dataType the data type
     * @param style the style to add to
     * @return the style
     */
    private FeatureVisualizationStyle addLabelStyle(LabelAction labelAction, DataTypeInfo dataType,
            FeatureVisualizationStyle style)
    {
        LabelOptions labelOptions = labelAction.getLabelOptions();

        List<String> labels = New.list();
        ColumnLabels columnLabels = labelOptions.getColumnLabels();
        if (columnLabels.isAlwaysShowLabels())
        {
            for (ColumnLabel columnLabel : columnLabels.getColumnsInLabel())
            {
                List<String> args = Arrays.asList(Boolean.toString(columnLabel.isShowColumnName()), columnLabel.getColumn());
                labels.add(StyleUtils.listSupp.writeList(args));
            }
        }
        style.setParameter(AbstractFeatureVisualizationStyle.LABEL_COLUMN_KEY_PROPERTY_KEY, labels, this);
        style.setParameter(AbstractFeatureVisualizationStyle.LABEL_ENABLED_PROPERTY_KEY, Boolean.TRUE, this);
        style.setParameter(AbstractFeatureVisualizationStyle.LABEL_COLOR_PROPERTY_KEY, labelOptions.getColor(), this);
        style.setParameter(AbstractFeatureVisualizationStyle.LABEL_SIZE_PROPERTY_KEY, Integer.valueOf(labelOptions.getSize()),
                this);

        return style;
    }

    /**
     * Clones the style, and updates the type key.
     *
     * @param style the style
     * @param dataType the data type
     * @return the cloned style
     */
    private static FeatureVisualizationStyle clone(FeatureVisualizationStyle style, DataTypeInfo dataType)
    {
        FeatureVisualizationStyle copy = (FeatureVisualizationStyle)style.clone();
        copy.setDTIKey(dataType.getTypeKey());
        return copy;
    }

    /**
     * Icon style that uses a fixed icon ID.
     */
    private static class IconStyle extends IconFeatureVisualizationStyle
    {
        /** The icon ID. */
        private final int myIconId;

        /**
         * Constructor.
         *
         * @param toolbox the toolbox
         * @param typeKey the data type key
         * @param iconId the icon ID
         */
        public IconStyle(Toolbox toolbox, String typeKey, int iconId)
        {
            super(toolbox, typeKey);
            myIconId = iconId;
        }

        /**
         * Initializes this icon style from the argument icon style.
         *
         * @param iconStyle the icon style from which to initialize
         */
        public void initialize(VisualizationStyle iconStyle)
        {
            for (VisualizationStyleParameter param : iconStyle.getStyleParameterSet())
            {
                setParameter(param);
            }
        }

        @Override
        protected int getIconId(long elementId)
        {
            return myIconId;
        }
    }
}
