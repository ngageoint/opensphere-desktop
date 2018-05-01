package io.opensphere.mantle.data.geom.style.impl;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.units.length.Length;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.BasicVisualizationInfo;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationInfo;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.style.VisualizationStyle;
import io.opensphere.mantle.data.geom.style.impl.ui.AbstractStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.ComboBoxStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.FloatSliderStyleParameterEditorPanel;
import io.opensphere.mantle.data.geom.style.impl.ui.PanelBuilder;
import io.opensphere.mantle.data.geom.util.ListSupport;
import io.opensphere.mantle.util.MantleConstants;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * A collection of utility methods used in creating and maintaining styles.
 */
public final class StyleUtils
{
    /** For separating fields out of a single String representation. */
    public static final ListSupport listSupp = new ListSupport('\\', ',');

    /** Classes used for styling map geometry features. */
    public static final List<Class<? extends VisualizationStyle>> FEATURE_STYLES = New.unmodifiableList(
            PointFeatureVisualizationStyle.class, EllipseGeometryFeatureVisualizationStyle.class,
            CircleGeometryFeatureVisualizationStyle.class, DynamicEllipseFeatureVisualization.class,
            LOBGeometryFeatureVisualizationStyle.class, IconFeatureVisualizationStyle.class, DynamicLOBFeatureVisualization.class,
            PolygonFeatureVisualizationStyle.class, PolylineFeatureVisualizationStyle.class,
            SpikeGeometryFeatureVisualizationStyle.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private StyleUtils()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * appends to the buffer with line-breaks; ignores blank lines.
     *
     * @param buf the buffer to which the line will be appended.
     * @param line the line to append to the buffer.
     */
    public static void appendLine(StringBuilder buf, String line)
    {
        if (line == null || line.isEmpty())
        {
            return;
        }
        if (buf.length() > 0)
        {
            buf.append('\n');
        }
        buf.append(line);
    }

    /**
     * parses the input String and constructs one line of label text.
     *
     * @param in the string to parse.
     * @param mdp the metadata provider from which fields are extracted.
     * @return the string form of the column extracted from the input text.
     */
    public static String labelString(String in, MetaDataProvider mdp)
    {
        List<String> fields = StyleUtils.listSupp.parseList(in);
        if (fields.size() > 1)
        {
            Boolean showCol = Boolean.valueOf(fields.get(0));
            String colName = fields.get(1);
            if (Boolean.TRUE.equals(showCol))
            {
                return colName + ":  " + Objects.toString(mdp.getValue(colName));
            }
            return Objects.toString(mdp.getValue(colName));
        }
        if (fields.size() == 1)
        {
            return Objects.toString(mdp.getValue(fields.get(0)));
        }
        return "";
    }

    /**
     * Sets the meta data column key property but checks to see if the key is
     * valid for the data type. Or if this FeatureVisualizationStyle does not
     * have an associated data type throws IllegalArgumentException.
     *
     * @param style the {@link AbstractFeatureVisualizationStyle}
     * @param parameterKey the parameter key
     * @param metaDataKey the meta data key
     * @param source the source making the change.
     */
    public static void setMetaDataColumnKeyProperty(AbstractFeatureVisualizationStyle style, String parameterKey,
            String metaDataKey, Object source)
    {
        if (style.getDTIKey() == null)
        {
            throw new IllegalArgumentException(
                    "Cannot set " + parameterKey + " name for a visualization style with no dedicated data type.");
        }
        String curValue = (String)style.getStyleParameterValue(parameterKey);
        if (!EqualsHelper.equals(metaDataKey, curValue))
        {
            DataTypeInfo dataType = StyleUtils.getDataTypeInfoFromKey(style.getToolbox(), style.getDTIKey());
            if (dataType == null)
            {
                throw new IllegalStateException("Could not retrieve DataTypeInfo for key [" + style.getDTIKey() + "]");
            }
            if (dataType.getMetaDataInfo() == null)
            {
                throw new IllegalStateException("Can't set the " + parameterKey + " for a data type that has no meta data");
            }
            if (!dataType.getMetaDataInfo().hasKey(metaDataKey))
            {
                throw new IllegalArgumentException(
                        "The data type " + dataType.getDisplayName() + " does not have column key \"" + metaDataKey + "\"");
            }
            style.setParameter(parameterKey, metaDataKey, source);
        }
    }

    /**
     * Determine color.
     *
     * @param currentGeomColor the current geom color
     * @param vs the vs
     * @return the color
     */
    public static Color determineColor(Color currentGeomColor, VisualizationState vs)
    {
        Color toSetTo = vs.isSelected() ? MantleConstants.SELECT_COLOR : vs.getColor();
        return toSetTo.equals(currentGeomColor) ? currentGeomColor : toSetTo;
    }

    /**
     * Creates the time constraints if applicable.
     *
     * @param basicVisInfo the basic vis info
     * @param mapVisInfo the map vis info
     * @param mgs the mgs
     * @param dgi the dgi
     * @return the constraints
     */
    public static Constraints createTimeConstraintsIfApplicable(BasicVisualizationInfo basicVisInfo,
            MapVisualizationInfo mapVisInfo, MapGeometrySupport mgs, DataGroupInfo dgi)
    {
        Constraints constraints = null;
        if (dgi != null && mapVisInfo != null && basicVisInfo.getLoadsTo().isTimelineEnabled() && !mgs.getTimeSpan().isTimeless())
        {
            constraints = Constraints.createTimeOnlyConstraint(dgi.getId(), mgs.getTimeSpan());
        }
        return constraints;
    }

    /**
     * Convert value to double.
     *
     * @param value the value
     * @return the double
     * @throws NumberFormatException the number format exception
     */
    public static double convertValueToDouble(Object value) throws NumberFormatException
    {
        double result = 0.0;
        if (value != null)
        {
            if (value instanceof Number)
            {
                result = ((Number)value).doubleValue();
            }
            else
            {
                result = Double.parseDouble(value.toString());
            }
        }
        return result;
    }

    /**
     * Gets the value in meters.
     *
     * @param inputValue the input value
     * @param unit the input value's units
     * @return the value in meters
     */
    public static double getValueInMeters(double inputValue, Class<? extends Length> unit)
    {
        return Length.create(unit, inputValue).inMeters();
    }

    /**
     * Creates the slider mini panel builder.
     *
     * @param label the label
     * @return the panel builder
     */
    public static PanelBuilder createSliderMiniPanelBuilder(String label)
    {
        PanelBuilder pb = StyleUtils.createBasicMiniPanelBuilder(label);
        pb.setOtherParameter(FloatSliderStyleParameterEditorPanel.SHOW_SLIDER_LABELS, Boolean.FALSE);
        return pb;
    }

    /**
     * Creates the combo box mini panel builder.
     *
     * @param label the label
     * @return the panel builder
     */
    public static PanelBuilder createComboBoxMiniPanelBuilder(String label)
    {
        PanelBuilder pb = StyleUtils.createBasicMiniPanelBuilder(label);
        pb.setOtherParameter(ComboBoxStyleParameterEditorPanel.COMBOBOX_BACKGROUND, Colors.LF_SECONDARY3);
        return pb;
    }

    /**
     * Gets the data group info associated with a data type info.
     *
     * @param toolbox the {@link Toolbox}
     * @param dataType the {@link DataTypeInfo}.
     * @return the {@link DataTypeInfo} or null if not found
     */
    public static DataGroupInfo getDataGroupInfoFromDti(Toolbox toolbox, DataTypeInfo dataType)
    {
        return dataType.getParent();
    }

    /**
     * Creates the basic mini panel builder.
     *
     * @param label the label
     * @return the panel builder
     */
    public static PanelBuilder createBasicMiniPanelBuilder(String label)
    {
        PanelBuilder pb = PanelBuilder.get(label, 20, 0, 0, 5);
        pb.setOtherParameter(AbstractStyleParameterEditorPanel.PANEL_HEIGHT, Integer.valueOf(24));
        return pb;
    }

    /**
     * Gets the data type info from key.
     *
     * @param toolbox the {@link Toolbox}
     * @param typeKey the {@link DataTypeInfo} key.
     * @return the {@link DataTypeInfo} or null if not found
     */
    public static DataTypeInfo getDataTypeInfoFromKey(Toolbox toolbox, String typeKey)
    {
        return MantleToolboxUtils.getMantleToolbox(toolbox).getDataTypeInfoFromKey(typeKey);
    }
}
