package io.opensphere.mantle.data.geom.style;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.mantle.data.VisualizationSupport;

/**
 * The Class VisualizationStyleDatatypeChangeEvent.
 */
public class VisualizationStyleDatatypeChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The DTI key. */
    private final String myDTIKey;

    /** The MGS class. */
    private final Class<? extends VisualizationSupport> myMGSClass;

    /** The New is default style. */
    private final boolean myNewIsDefaultStyle;

    /** The New style. */
    private final VisualizationStyle myNewStyle;

    /** The Old style. */
    private final VisualizationStyle myOldStyle;

    /** The Source. */
    private final Object mySource;

    /**
     * Instantiates a new visualization style datatype change event.
     *
     * @param dtiKey the dti key
     * @param mgsClass the mgs class
     * @param oldStyle the old style
     * @param newStyle the new style
     * @param newIsDefault the new is default
     * @param source the source
     */
    public VisualizationStyleDatatypeChangeEvent(String dtiKey, Class<? extends VisualizationSupport> mgsClass,
            VisualizationStyle oldStyle, VisualizationStyle newStyle, boolean newIsDefault, Object source)
    {
        myDTIKey = dtiKey;
        myMGSClass = mgsClass;
        myOldStyle = oldStyle;
        myNewStyle = newStyle;
        myNewIsDefaultStyle = newIsDefault;
        mySource = source;
    }

    @Override
    public String getDescription()
    {
        return getClass().getSimpleName();
    }

    /**
     * Gets the dTI key.
     *
     * @return the dTI key
     */
    public String getDTIKey()
    {
        return myDTIKey;
    }

    /**
     * Gets the mgs class.
     *
     * @return the MGS class
     */
    public Class<? extends VisualizationSupport> getMGSClass()
    {
        return myMGSClass;
    }

    /**
     * Gets the new style.
     *
     * @return the new style
     */
    public VisualizationStyle getNewStyle()
    {
        return myNewStyle;
    }

    /**
     * Gets the old style.
     *
     * @return the old style
     */
    public VisualizationStyle getOldStyle()
    {
        return myOldStyle;
    }

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Checks if is new is default style.
     *
     * @return true, if is new is default style
     */
    public boolean isNewIsDefaultStyle()
    {
        return myNewIsDefaultStyle;
    }

    /**
     * Checks if is type specific change.
     *
     * @return true, if is type specific change
     */
    public boolean isTypeSpecificChange()
    {
        return myDTIKey != null;
    }
}
