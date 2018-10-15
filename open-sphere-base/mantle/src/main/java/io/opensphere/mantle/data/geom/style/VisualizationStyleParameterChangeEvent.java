package io.opensphere.mantle.data.geom.style;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.opensphere.core.event.AbstractSingleStateEvent;
import io.opensphere.core.event.SourceableEvent;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * The Class VisualizationStyleParameterChangeEvent.
 */
public class VisualizationStyleParameterChangeEvent extends AbstractSingleStateEvent implements SourceableEvent
{
    /** The Changed parameter set. */
    private final Set<VisualizationStyleParameter> myChangedParameterSet;

    /** The DTI key. */
    private final String myDTIKey;

    /** The Requires geometry rebuild. */
    private boolean myRequiresGeometryRebuild;

    /** The Requires meta data. */
    private boolean myRequiresMetaData;

    /** The Source. */
    private final Object mySource;

    /** The Style. */
    private final VisualizationStyle myStyle;

    /**
     * Instantiates a new visualization style parameter change event.
     *
     * @param dtiKey the dti key
     * @param style the style
     * @param changedSet the changed set
     * @param source the source
     */
    public VisualizationStyleParameterChangeEvent(String dtiKey, VisualizationStyle style,
            Set<VisualizationStyleParameter> changedSet, Object source)
    {
        Utilities.checkNull(style, "style");
        Utilities.checkNull(changedSet, "changedSet");

        myDTIKey = dtiKey;
        myStyle = style;
        mySource = source;
        myChangedParameterSet = Collections.unmodifiableSet(New.set(changedSet));

        for (VisualizationStyleParameter p : myChangedParameterSet)
        {
            if (p.getHint() != null)
            {
                if (!p.getHint().isRenderPropertyChangeOnly())
                {
                    myRequiresGeometryRebuild = true;
                }
                if (p.getHint().isRequiresMetaData())
                {
                    myRequiresMetaData = true;
                }
            }
        }
    }

    /**
     * Gets the changed parameter key to parameter map.
     *
     * @return the changed parameter key to parameter map
     */
    public Map<String, VisualizationStyleParameter> getChangedParameterKeyToParameterMap()
    {
        return myChangedParameterSet.stream().collect(Collectors.toUnmodifiableMap(v -> v.getKey(), v -> v));
    }

    /**
     * Gets the changed parameter set.
     *
     * @return the changed parameter set
     */
    public Set<VisualizationStyleParameter> getChangedParameterSet()
    {
        return myChangedParameterSet;
    }

    @Override
    public String getDescription()
    {
        return this.getClass().getSimpleName();
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

    @Override
    public Object getSource()
    {
        return mySource;
    }

    /**
     * Gets the style.
     *
     * @return the style
     */
    public VisualizationStyle getStyle()
    {
        return myStyle;
    }

    /**
     * Checks if requires geometry rebuild.
     *
     * @return true, if is requires geometry rebuild
     */
    public boolean requiresGeometryRebuild()
    {
        return myRequiresGeometryRebuild;
    }

    /**
     * Requires meta data.
     *
     * @return true, if successful
     */
    public boolean requiresMetaData()
    {
        return myRequiresMetaData;
    }
}
