package io.opensphere.wms.state.save;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.bitsys.fade.mist.state.v4.LayerParamsType;
import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.TagsType;

import io.opensphere.core.util.lang.NumberUtilities;
import io.opensphere.wms.state.model.WMSLayerState;

/** Translates v3 objects to v4 for saving. */
public final class SaveStateV3ToV4Translator
{
    /**
     * Converts a WFSLayerState to a LayerType.
     *
     * @param layerV3 the layer state
     * @return the layer type
     */
    public static LayerType toLayerType(WMSLayerState layerV3)
    {
        LayerType layer = new LayerType();
        layer.setAlpha(BigDecimal.valueOf(layerV3.getAlpha()));
        layer.setColorizeStyle(layerV3.getColorizeStyle());
        layer.setFixedHeight(Boolean.valueOf(layerV3.isFixedHeight()));
        layer.setFixedWidth(Boolean.valueOf(layerV3.isFixedWidth()));
        layer.setGetMapUrl(layerV3.getGetMapUrl());
        layer.setId(layerV3.getId());
        layer.setAnimate(Boolean.toString(layerV3.isAnimate()));
        layer.setLayerType(layerV3.getType());
        layer.setMaxDisplaySize(BigInteger.valueOf(layerV3.getMaxDisplaySize()));
        layer.setMinDisplaySize(BigInteger.valueOf(layerV3.getMinDisplaySize()));
        LayerParamsType params = new LayerParamsType();
        params.getAny().add(new JAXBElement<>(new QName("layers"), String.class, layerV3.getParameters().getLayerName()));
        layer.setParams(params);
        layer.setSplitLevels(String.valueOf(NumberUtilities.toInt(layerV3.getSplitLevels(), 18)));
        layer.setTags(toTags(layerV3.getTags()));
        layer.setTitle(layerV3.getTitle());
        layer.setType(layerV3.getLayerType());
        layer.setUrl(layerV3.getUrl());
        layer.setVisible(layerV3.isVisible());
        return layer;
    }

    /**
     * Converts tags to a TagsType.
     *
     * @param tagsV3 the tags
     * @return the TagsType
     */
    private static TagsType toTags(Collection<String> tagsV3)
    {
        TagsType tags = new TagsType();
        tags.getTag().addAll(tagsV3);
        return tags;
    }

    /** Disallow instantiation. */
    private SaveStateV3ToV4Translator()
    {
    }
}
