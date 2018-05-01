package io.opensphere.merge.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The model for the merge UI.
 */
@XmlType
@XmlAccessorType(XmlAccessType.NONE)
public class MergeModel
{
    /**
     * The layers we are merging.
     */
    private final List<DataTypeInfo> myLayers;

    /**
     * The type keys we are merging.
     */
    @XmlElement(name = "typeKey")
    private final List<String> myTypeKeys;

    /**
     * The new layer name.
     */
    @XmlAttribute(name = "layerName")
    @XmlJavaTypeAdapter(StringPropertyAdapter.class)
    private final StringProperty myNewLayerName = new SimpleStringProperty();

    /**
     * A message to show to the user.
     */
    private final StringProperty myUserMessage = new SimpleStringProperty();

    /**
     * JAXB Constructor.
     */
    public MergeModel()
    {
        myLayers = Collections.emptyList();
        myTypeKeys = Collections.emptyList();
    }

    /**
     * Constructs a new model.
     *
     * @param layers The layers that will be merged.
     */
    public MergeModel(Collection<DataTypeInfo> layers)
    {
        myLayers = New.unmodifiableList(layers);
        myTypeKeys = layers.stream().map(d -> d.getTypeKey()).collect(Collectors.toList());
    }

    /**
     * Gets the layers to merge.
     *
     * @return the layers to merge.
     */
    public List<DataTypeInfo> getLayers()
    {
        if (myLayers.isEmpty())
        {
            throw new RuntimeException("empty layers");
        }
        return myLayers;
    }

    /**
     * Gets the name of the new merged layer.
     *
     * @return the new layer name.
     */
    public StringProperty getNewLayerName()
    {
        return myNewLayerName;
    }

    /**
     * Gets a message to show to the user.
     *
     * @return A message to show to the user.
     */
    public StringProperty getUserMessage()
    {
        return myUserMessage;
    }

    /** XmlAdapter for StringProperty. */
    private static class StringPropertyAdapter extends XmlAdapter<String, StringProperty>
    {
        @Override
        public StringProperty unmarshal(String v)
        {
            return new SimpleStringProperty(v);
        }

        @Override
        public String marshal(StringProperty v)
        {
            return v.get();
        }
    }
}
