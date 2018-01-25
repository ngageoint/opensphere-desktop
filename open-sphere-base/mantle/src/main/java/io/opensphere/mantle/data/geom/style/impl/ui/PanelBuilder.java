package io.opensphere.mantle.data.geom.style.impl.ui;

import java.util.Map;

import io.opensphere.core.util.collections.New;

/**
 * The Class LabelBuilder.
 */
public class PanelBuilder
{
    /** The Height. */
    private final int myHeight;

    /** The Indent. */
    private final int myIndent;

    /** The Label. */
    private final String myLabel;

    /** The Other parameters. */
    private final Map<String, Object> myOtherParameters = New.map();

    /** The Trail padding. */
    private final int myTrailPadding;

    /** The Width. */
    private final int myWidth;

    /**
     * Static factory method to create a label builder with a label value only.
     *
     * @param label the label
     * @return the label builder
     */
    public static PanelBuilder get(String label)
    {
        return new PanelBuilder(label);
    }

    /**
     * Static factory method to create a label builder with a width.
     *
     * @param label the label
     * @param width the width
     * @param height the height
     * @return the label builder
     */
    public static PanelBuilder get(String label, int width, int height)
    {
        return new PanelBuilder(label, width, height);
    }

    /**
     * Static factory method to create a label builder with indent and width.
     *
     * @param label the label
     * @param indent the indent
     * @param width the width
     * @param height the height
     * @param trailPadding the trail padding
     * @return the label builder
     */
    public static PanelBuilder get(String label, int indent, int width, int height, int trailPadding)
    {
        return new PanelBuilder(label, indent, width, height, trailPadding);
    }

    /**
     * Instantiates a new label builder.
     *
     * @param label the label
     */
    public PanelBuilder(String label)
    {
        this(label, 5, 140, 30, 10);
    }

    /**
     * Instantiates a new label builder.
     *
     * @param label the label
     * @param width the width
     * @param height the height
     */
    public PanelBuilder(String label, int width, int height)
    {
        this(label, 5, width, height, 10);
    }

    /**
     * Instantiates a new label builder.
     *
     * @param label the label
     * @param indent the indent
     * @param width the width
     * @param height the height
     * @param trailPadding the trail padding
     */
    public PanelBuilder(String label, int indent, int width, int height, int trailPadding)
    {
        super();
        myLabel = label;
        myIndent = indent;
        myWidth = width;
        myHeight = height;
        myTrailPadding = trailPadding;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the indent.
     *
     * @return the indent
     */
    public int getIndent()
    {
        return myIndent;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel()
    {
        return myLabel;
    }

    /**
     * Gets the other parameter.
     *
     * @param param the param
     * @return the other parameter
     */
    public Object getOtherParameter(String param)
    {
        return myOtherParameters.get(param);
    }

    /**
     * Gets the other paramter.
     *
     * @param param the param
     * @param defaultValue the default value
     * @return the other paramter
     */
    public Object getOtherParameter(String param, Object defaultValue)
    {
        Object result = myOtherParameters.get(param);
        return result == null ? defaultValue : result;
    }

    /**
     * Gets the trail padding.
     *
     * @return the trail padding
     */
    public int getTrailPadding()
    {
        return myTrailPadding;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Sets the other parameter.
     *
     * @param param the param
     * @param value the value
     */
    public void setOtherParameter(String param, Object value)
    {
        myOtherParameters.put(param, value);
    }
}
