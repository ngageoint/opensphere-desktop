package io.opensphere.wfs.state.save;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.bitsys.fade.mist.state.v4.BasicFeatureStyleType;
import com.bitsys.fade.mist.state.v4.IconDefaultType;
import com.bitsys.fade.mist.state.v4.IconStyleType;
import com.bitsys.fade.mist.state.v4.LabelColumnType;
import com.bitsys.fade.mist.state.v4.LabelColumnsType;
import com.bitsys.fade.mist.state.v4.LayerParamsType;
import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.TagsType;

import io.opensphere.core.modulestate.TagList;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wfs.state.model.BasicFeatureStyle;
import io.opensphere.wfs.state.model.BasicFeatureStyle.LblCfgRec;
import io.opensphere.wfs.state.model.IconStyle;
import io.opensphere.wfs.state.model.WFSLayerState;
import io.opensphere.wfs.state.model.WFSStateParameters;

/** Translates v3 objects to v4 for saving. */
public final class SaveStateV3ToV4Translator
{
    /**
     * Converts a WFSLayerState to a LayerType.
     *
     * @param layerV3 the layer state
     * @return the layer type
     */
    public static LayerType toLayerType(WFSLayerState layerV3)
    {
        LayerType layer = new LayerType();
        layer.setAltitudeEnabled(Boolean.valueOf(layerV3.getBasicFeatureStyle().isUseAltitude()));
        layer.setAnimate(Boolean.toString(layerV3.isAnimate()));
        layer.setBaseColor(toColorString(layerV3.getBasicFeatureStyle().getPointColor()));
        layer.setBasicFeatureStyle(toFeatureStyle(layerV3.getBasicFeatureStyle()));
        layer.setDisabledEmptyColumns(Boolean.toString(layerV3.isDisableEmptyColumns()));
        layer.setIconStyle(toIconStyle(layerV3.getIconStyle()));
        layer.setId(layerV3.getId());
        layer.setLoad(Boolean.TRUE);
        if (layerV3.getWFSParameters() != null)
        {
            layer.setParams(toLayerParams(layerV3.getWFSParameters()));
        }
        layer.setServerId(layerV3.getServerId());
        layer.setShowLabels(Boolean.toString(layerV3.getBasicFeatureStyle().isUseLabels()));
        layer.setSpatial(Boolean.TRUE);
        layer.setTags(toTags(layerV3.getTags()));
        layer.setTemporal(Boolean.valueOf(layerV3.getLoadsTo() == null || layerV3.getLoadsTo().isTimelineEnabled()));
        layer.setTitle(layerV3.getDisplayName());
        layer.setType(StateConstants.WFS_LAYER_TYPE);
        layer.setUrl(layerV3.getUrl());
        layer.setVisible(layerV3.isVisible());
        return layer;
    }

    /**
     * Converts a WFSStateParameters to a LayerParamsType.
     *
     * @param paramsV3 the WFSStateParameters
     * @return the LayerParamsType
     */
    private static LayerParamsType toLayerParams(WFSStateParameters paramsV3)
    {
        LayerParamsType params = new LayerParamsType();
        params.getAny().add(new JAXBElement<>(new QName("service"), String.class, "WFS"));
        params.getAny().add(new JAXBElement<>(new QName("version"), String.class, paramsV3.getVersion()));
        params.getAny().add(new JAXBElement<>(new QName("request"), String.class, "GetFeature"));
        params.getAny().add(new JAXBElement<>(new QName("typename"), String.class, paramsV3.getTypeName()));
        return params;
    }

    /**
     * Converts a TagList to a TagsType.
     *
     * @param tagsV3 the TagList
     * @return the TagsType
     */
    private static TagsType toTags(TagList tagsV3)
    {
        TagsType tags = new TagsType();
        tags.getTag().addAll(tagsV3.getTags());
        return tags;
    }

    /**
     * Converts a BasicFeatureStyle to a BasicFeatureStyleType.
     *
     * @param styleV3 the BasicFeatureStyle
     * @return the BasicFeatureStyleType
     */
    private static BasicFeatureStyleType toFeatureStyle(BasicFeatureStyle styleV3)
    {
        if (styleV3 == null)
        {
            return null;
        }

        BasicFeatureStyleType style = new BasicFeatureStyleType();
        style.setAltitudeColumn(styleV3.getAltitudeColumn());
        style.setAltColUnits(styleV3.getAltitudeColUnits());
        style.setAltitudeRef(styleV3.getAltitudeRef());
        style.setLabelColor(toColorString(styleV3.getLabelColor()));
        style.setLabelColumn(styleV3.getLabelColumn());
        style.setLabelColumns(toLabelColumn(styleV3.getLabelRecs()));
        style.setLabelSize(BigInteger.valueOf(styleV3.getLabelSize()));
        style.setLift(BigDecimal.valueOf(styleV3.getLift()));
        style.setPointColor(toColorString(styleV3.getPointColor()));
        style.setPointOpacity(Integer.valueOf(styleV3.getPointOpacity()));
        style.setPointSize(BigDecimal.valueOf(styleV3.getPointSize()));
        return style;
    }

    /**
     * Converts a LblConfig to a LabelColumnsType.
     *
     * @param columnsV3 the LblConfig entries
     * @return the LabelColumnsType
     */
    private static LabelColumnsType toLabelColumn(Collection<LblCfgRec> columnsV3)
    {
        LabelColumnsType columns = new LabelColumnsType();
        for (LblCfgRec columnV3 : columnsV3)
        {
            LabelColumnType column = new LabelColumnType();
            column.setColumn(columnV3.column);
            column.setShowColumn(columnV3.showColumn);
            columns.getLabel().add(column);
        }
        return columns;
    }

    /**
     * Converts a IconStyle to a IconStyleType.
     *
     * @param styleV3 the IconStyle
     * @return the IconStyleType
     */
    private static IconStyleType toIconStyle(IconStyle styleV3)
    {
        if (styleV3 == null)
        {
            return null;
        }

        IconStyleType style = new IconStyleType();
        style.setIconDefaultPointSize(BigDecimal.valueOf(styleV3.getDefaultPointSize()));
        style.setIconDefaultTo(IconDefaultType.fromValue(styleV3.getDefaultTo()));
        style.setIconScale(BigDecimal.valueOf(styleV3.getIconScale()));
        style.setDefaultIconURL(styleV3.getIconURL());
        style.setIconXOffset(BigInteger.valueOf(styleV3.getIconXOffset()));
        style.setIconYOffset(BigInteger.valueOf(styleV3.getIconYOffset()));
        style.setMixIconElementColor(Boolean.valueOf(styleV3.isMixIconElementColor()));
        return style;
    }

    /**
     * Converts a v3 color string to a v4 color string.
     *
     * @param colorV3 the v3 color
     * @return the v4 color
     */
    private static String toColorString(String colorV3)
    {
        return colorV3 != null ? "0x" + colorV3 : null;
    }

    /** Disallow instantiation. */
    private SaveStateV3ToV4Translator()
    {
    }
}
