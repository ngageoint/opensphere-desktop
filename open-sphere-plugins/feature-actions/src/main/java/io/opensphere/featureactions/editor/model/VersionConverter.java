package io.opensphere.featureactions.editor.model;

import java.awt.Color;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.bitsys.fade.mist.state.v4.AbstractActionType;
import com.bitsys.fade.mist.state.v4.FeatureActionArrayType;
import com.bitsys.fade.mist.state.v4.FeatureActionType;
import com.bitsys.fade.mist.state.v4.FeatureLabelActionType;
import com.bitsys.fade.mist.state.v4.FeatureStyleActionType;
import com.bitsys.fade.mist.state.v4.LabelColumnType;
import com.bitsys.fade.mist.state.v4.ShapeType;

import io.opensphere.controlpanels.columnlabels.model.ColumnLabel;
import io.opensphere.controlpanels.columnlabels.model.ColumnLabels;
import io.opensphere.controlpanels.styles.model.LabelOptions;
import io.opensphere.controlpanels.styles.model.StyleOptions;
import io.opensphere.controlpanels.styles.model.Styles;
import io.opensphere.core.util.ObservableList;
import io.opensphere.featureactions.model.CustomColumnAction;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;
import io.opensphere.featureactions.model.LabelAction;
import io.opensphere.featureactions.model.StyleAction;
import io.opensphere.filterbuilder.filter.v1.Filter;
import net.opengis.ogc._100t.FilterType;

/**
 * A utility class used to convert between XML Schema Model versions. As the
 * Feature Actions API's model is based on the v2 version of the schema, instead
 * of rewriting to adhere to the v4 version, this class back-converts v4 objects
 * into v2 objects.
 */
public class VersionConverter
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(VersionConverter.class);

    /**
     * Converts the supplied V4 {@link FilterType} object to a V2 {@link Filter}
     * object.
     *
     * @param filter the object to convert.
     * @return the converted object.
     */
    public static Filter convert(FilterType filter)
    {
        OGC100FilterToDataFilterConverter converter = new OGC100FilterToDataFilterConverter();
        return converter.apply(filter);
    }

    /**
     * Converts the supplied feature action type from the v4 version to the v2
     * object.
     *
     * @param source the source object to convert.
     * @return a {@link FeatureAction} generated from the supplied object.
     */
    public static FeatureAction convert(FeatureActionType source)
    {
        FeatureAction returnValue = new FeatureAction();

        returnValue.setFilter(convert(source.getFilter()));

        returnValue.setName(source.getTitle());
        returnValue.setEnabled(source.isActive());
        returnValue.setId(UUID.randomUUID().toString());
        List<JAXBElement<? extends AbstractActionType>> action = source.getActions().getAction();
        for (JAXBElement<? extends AbstractActionType> jaxbElement : action)
        {
            AbstractActionType abstractActionType = jaxbElement.getValue();
            if (abstractActionType instanceof FeatureLabelActionType)
            {
                FeatureLabelActionType labelAction = (FeatureLabelActionType)abstractActionType;
                returnValue.getActions().add(convert(labelAction));

                if (StringUtils.isNotBlank(labelAction.getCustomName()) && StringUtils.isNotBlank(labelAction.getCustomValue()))
                {
                    returnValue.getActions().add(convert(labelAction.getCustomName(), labelAction.getCustomValue()));
                }
            }
            else if (abstractActionType instanceof FeatureStyleActionType)
            {
                returnValue.getActions().add(convert((FeatureStyleActionType)abstractActionType));
            }
            else
            {
                LOG.warn("Unsupported action type: " + abstractActionType.getClass().getName());
            }
        }

        return returnValue;
    }

    /**
     * Converts the supplied array of feature actions to the corresponding v2
     * object.
     *
     * @param source the array of feature actions to convert.
     * @return a {@link FeatureActions} object generated from the supplied
     *         array.
     */
    public static SimpleFeatureActionGroup convert(FeatureActionArrayType source)
    {
        SimpleFeatureActionGroup group = new SimpleFeatureActionGroup();

        source.getFeatureAction().stream().map(sfa -> convert(sfa)).forEach(a ->
        {
            SimpleFeatureAction action = new SimpleFeatureAction(a);
            a.setGroupName("Feature Actions 1");
            group.getActions().add(action);
        });

        return group;
    }

    /**
     * Converts the supplied label feature action type to the corresponding v2
     * object.
     *
     * @param source the object to convert.
     * @return the converted label action.
     */
    public static LabelAction convert(FeatureLabelActionType source)
    {
        LabelAction returnValue = new LabelAction();
        LabelOptions labelOptions = returnValue.getLabelOptions();
        ColumnLabels columnLabels = labelOptions.getColumnLabels();

        ObservableList<ColumnLabel> columnsInLabel = columnLabels.getColumnsInLabel();
        source.getLabels().getLabel().stream().map(c -> convert(c)).forEach(columnsInLabel::add);

        labelOptions.setColor(Color.decode(source.getColor()));
        labelOptions.setSize(source.getSize().intValue());

        return returnValue;
    }

    /**
     * Converts the supplied column type to the corresponding v2 object.
     *
     * @param source the object to convert.
     * @return the converted label type.
     */
    public static ColumnLabel convert(LabelColumnType source)
    {
        ColumnLabel returnValue = new ColumnLabel();

        returnValue.setColumn(source.getColumn());
        returnValue.setShowColumnName(source.isShowColumn());

        return returnValue;
    }

    /**
     * Converts the supplied style action to the corresponding v2 object.
     *
     * @param source the source object to convert.
     * @return a converted v2 object.
     */
    public static StyleAction convert(FeatureStyleActionType source)
    {
        StyleAction returnValue = new StyleAction();
        StyleOptions styleOptions = returnValue.getStyleOptions();

        Color color = Color.decode(source.getColor());
        styleOptions.setColor(
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(255 * source.getOpacity().doubleValue())));
        styleOptions.setSize(source.getSize().intValue());
        styleOptions.setStyle(convert(source.getCenterShape()));

        // todo figure out icons:

        return returnValue;
    }

    /**
     * Generates a new {@link CustomColumnAction} from the supplied source.
     *
     * @param name the name of the action to generate.
     * @param value the value of the action to generated.
     * @return a new {@link CustomColumnAction} generated from the supplied
     *         values.
     */
    public static CustomColumnAction convert(String name, String value)
    {
        CustomColumnAction customColumn = new CustomColumnAction();
        customColumn.setColumn(name);
        customColumn.setValue(value);

        return customColumn;
    }

    /**
     * Converts the supplied {@link ShapeType} to a {@link Styles} instance.
     *
     * @param source the shape type to convert.
     * @return the converted shape type as a Styles enum instance.
     */
    public static Styles convert(ShapeType source)
    {
        switch (source)
        {
            case DEFAULT:
            case POINT:
                return Styles.POINT;
            case ELLIPSE:
            case SELECTED_ELLIPSE:
                return Styles.ELLIPSE;
            case ELLIPSE_WITH_CENTER:
            case SELECTED_ELLIPSE_WITH_CENTER:
                return Styles.ELLIPSE_WITH_CENTER;
            case ICON:
                return Styles.ICON;
            case LINE_OF_BEARING:
            case LINE_OF_BEARING_WITH_CENTER:
                return Styles.LINE_OF_BEARING;
            case SQUARE:
                return Styles.SQUARE;
            case TRIANGLE:
                return Styles.TRIANGLE;
            default:
                LOG.warn("Unrecognized shape type: '" + source.name() + "'");
                return Styles.POINT;
        }
    }
}
