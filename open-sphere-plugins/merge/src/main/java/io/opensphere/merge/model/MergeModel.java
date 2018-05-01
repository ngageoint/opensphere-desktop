package io.opensphere.merge.model;

import java.util.Collection;
import java.util.List;

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
import io.opensphere.mantle.MantleToolbox;
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
    private final List<String> myTypeKeys = New.list();

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
        myLayers = New.list();
    }

    /**
     * Constructs a new model.
     *
     * @param layers The layers that will be merged.
     */
    public MergeModel(Collection<DataTypeInfo> layers)
    {
        myLayers = New.unmodifiableList(layers);
        layers.stream().map(d -> d.getTypeKey()).forEach(myTypeKeys::add);
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
            throw new IllegalStateException("Empty layers - please call getLayers() with the mantle toolbox instead");
        }
        return myLayers;
    }

    /**
     * Gets the layers to merge.
     *
     * @param mantleToolbox the mantle toolbox, to look up layers
     * @return the layers to merge.
     */
    public List<DataTypeInfo> getLayers(MantleToolbox mantleToolbox)
    {
        if (myLayers.isEmpty())
        {
            myTypeKeys.stream().map(mantleToolbox::getDataTypeInfoFromKey).forEach(myLayers::add);
        }
        return myLayers;
    }

    /**
     * Gets the layer count.
     *
     * @return the layer count
     */
    public int getLayerCount()
    {
        return !myLayers.isEmpty() ? myLayers.size() : myTypeKeys.size();
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
