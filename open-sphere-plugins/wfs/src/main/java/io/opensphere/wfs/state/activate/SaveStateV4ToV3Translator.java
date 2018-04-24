package io.opensphere.wfs.state.activate;

import com.bitsys.fade.mist.state.v4.BasicFeatureStyleType;
import com.bitsys.fade.mist.state.v4.IconDefaultType;
import com.bitsys.fade.mist.state.v4.IconStyleType;
import com.bitsys.fade.mist.state.v4.LabelColumnType;
import com.bitsys.fade.mist.state.v4.LabelColumnsType;
import com.bitsys.fade.mist.state.v4.LayerParamsType;
import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.TagsType;

import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.TagList;
import io.opensphere.core.util.lang.BooleanUtilities;
import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.wfs.state.model.BasicFeatureStyle;
import io.opensphere.wfs.state.model.IconStyle;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.model.WFSStateParameters;

/** Translates v4 objects to v3 for saving. */
public final class SaveStateV4ToV3Translator
{
    /**
     * Converts a LayerType to a WFSLayerState.
     *
     * @param layerV4 the layer type
     * @return the layer state
     */
    public static WFSLayerState toLayerState(LayerType layerV4)
    {
        WFSLayerState layer = new WFSLayerState();
        populateLayerState(layerV4, layer);
        return layer;
    }

    /**
     * Populates the WFSLayerState with data in the LayerType.
     *
     * @param layerV4 the LayerType
     * @param layer the WFSLayerState
     */
    public static void populateLayerState(LayerType layerV4, WFSLayerState layer)
    {
        // layerV3.getDisabledColumns();
        // layerV3.getEllipseStyle();
        // layerV3.getLineOfBearingStyle();
        layer.setAnimate(Boolean.parseBoolean(layerV4.getAnimate()));
        layer.setBasicFeatureStyle(toFeatureStyle(layerV4.getBasicFeatureStyle()));
        layer.setDisableEmptyColumns(Boolean.parseBoolean(layerV4.getDisabledEmptyColumns()));
        layer.setIconStyle(toIconStyle(layerV4.getIconStyle()));
        layer.setId(layerV4.getId());
        layer.setWFSParameters(toLayerParams(layerV4.getParams()));
        layer.setServerId(layerV4.getServerId());
        layer.getBasicFeatureStyle().setUseLabels(Boolean.parseBoolean(layerV4.getShowLabels()));
        layer.setTags(toTags(layerV4.getTags()));
        layer.setDisplayName(layerV4.getTitle());
        layer.setType(layerV4.getType().toLowerCase());
        layer.setUrl(layerV4.getUrl());
        layer.setVisible(layerV4.isVisible());
        layer.setLoadsTo(BooleanUtilities.toBoolean(layerV4.isTemporal()) ? LoadsTo.TIMELINE : LoadsTo.STATIC);
    }

    /**
     * Converts a LayerParamsType to a WFSStateParameters.
     *
     * @param paramsV4 the LayerParamsType
     * @return the WFSStateParameters
     */
    private static WFSStateParameters toLayerParams(LayerParamsType paramsV4)
    {
        WFSStateParameters params = new WFSStateParameters();
        if (paramsV4 != null && paramsV4.isSetAny())
        {
            params.setTypeName(StateUtilities.getValue(paramsV4.getAny(), "typename"));
            params.setVersion(StateUtilities.getValue(paramsV4.getAny(), "version"));
        }
        return params;
    }

    /**
     * Converts a TagsType to a TagList.
     *
     * @param tagsV4 the TagsType
     * @return the TagList
     */
    private static TagList toTags(TagsType tagsV4)
    {
        TagList tags = new TagList();
        if (tagsV4 != null)
        {
            tags.setTags(tagsV4.getTag());
        }
        return tags;
    }

    /**
     * Converts a BasicFeatureStyleType to a BasicFeatureStyle.
     *
     * @param styleV4 the BasicFeatureStyleType
     * @return the BasicFeatureStyle
     */
    private static BasicFeatureStyle toFeatureStyle(BasicFeatureStyleType styleV4)
    {
        if (styleV4 == null)
        {
            return null;
        }

        BasicFeatureStyle style = new BasicFeatureStyle();
        style.setAltitudeColumn(styleV4.getAltitudeColumn());
        if (styleV4.getAltColUnits() != null)
        {
            style.setAltitudeColUnits(styleV4.getAltColUnits());
        }
        if (styleV4.getAltitudeRef() != null)
        {
            style.setAltitudeRef(styleV4.getAltitudeRef());
        }
        style.setLabelColor(toColorString(styleV4.getLabelColor()));
        if (styleV4.getLabelColumn() != null)
        {
            style.setLabelColumn(styleV4.getLabelColumn());
        }
        populateLabelColumn(styleV4.getLabelColumns(), style);
        style.setLabelSize(NumberUtilities.toInt(styleV4.getLabelSize()));
        style.setLift(NumberUtilities.toDouble(styleV4.getLift()));
        if (styleV4.getPointColor() != null)
        {
            style.setPointColor(toColorString(styleV4.getPointColor()));
        }
        if (styleV4.getPointOpacity() != null)
        {
            style.setPointOpacity(NumberUtilities.toInt(styleV4.getPointOpacity()));
        }
        if (styleV4.getPointSize() != null)
        {
            style.setPointSize(NumberUtilities.toFloat(styleV4.getPointSize()));
        }
        style.setUseAltitude(true);
        return style;
    }

    /**
     * Populates a BasicFeatureStyle's "LabelRec".
     *
     * @param columnsV4 the LabelColumnsType
     * @param style the v3 feature style
     */
    private static void populateLabelColumn(LabelColumnsType columnsV4, BasicFeatureStyle style)
    {
        if (columnsV4 != null)
        {
            for (LabelColumnType columnV4 : columnsV4.getLabel())
            {
                style.addLabelRec(Boolean.toString(columnV4.isSetColumn()), columnV4.getColumn());
            }
        }
    }

    /**
     * Converts a IconStyleType to a IconStyle.
     *
     * @param styleV4 the IconStyleType
     * @return the IconStyle
     */
    private static IconStyle toIconStyle(IconStyleType styleV4)
    {
        if (styleV4 == null)
        {
            return null;
        }

        IconStyle style = new IconStyle();
        style.setDefaultPointSize(NumberUtilities.toFloat(styleV4.getIconDefaultPointSize()));
        style.setDefaultTo(styleV4.getIconDefaultTo() != null ? styleV4.getIconDefaultTo().value() : IconDefaultType.ICON.value());
        style.setIconScale(NumberUtilities.toFloat(styleV4.getIconScale()));
        style.setIconURL(styleV4.getDefaultIconURL());
        style.setIconXOffset(NumberUtilities.toInt(styleV4.getIconXOffset()));
        style.setIconYOffset(NumberUtilities.toInt(styleV4.getIconYOffset()));
        style.setMixIconElementColor(BooleanUtilities.toBoolean(styleV4.isMixIconElementColor()));
        return style;
    }

    /**
     * Converts a v4 color string to a v3 color string.
     *
     * @param colorV4 the v4 color
     * @return the v3 color
     */
    private static String toColorString(String colorV4)
    {
        return colorV4 != null ? StringUtilities.removePrefix(colorV4, "0x") : null;
    }

    /** Disallow instantiation. */
    private SaveStateV4ToV3Translator()
    {
    }
}
