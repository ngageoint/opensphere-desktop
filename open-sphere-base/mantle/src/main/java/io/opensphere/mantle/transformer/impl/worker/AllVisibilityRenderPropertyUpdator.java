package io.opensphere.mantle.transformer.impl.worker;

import io.opensphere.core.geometry.renderproperties.BaseRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultMeshScalableRenderProperties;
import io.opensphere.core.geometry.renderproperties.DefaultPointRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderProperties;

/**
 * The Class AllVisibilityRenderPropertyUpdator.
 */
public class AllVisibilityRenderPropertyUpdator extends AbstractRenderPropertyUpdator
{
    /** The Show. */
    private final boolean myShow;

    /**
     * Instantiates a new visibility render property updator.
     *
     * @param provider the provider
     * @param show the show ( true to make visible, false to hide)
     */
    public AllVisibilityRenderPropertyUpdator(DataElementTransformerWorkerDataProvider provider, boolean show)
    {
        super(provider);
        myShow = show;
    }

    @Override
    public void adjust(RenderProperties rp)
    {
        if (rp instanceof DefaultMeshScalableRenderProperties)
        {
            ((DefaultMeshScalableRenderProperties)rp).setHidden(!myShow);
        }
        else if (rp instanceof DefaultPointRenderProperties)
        {
            ((DefaultPointRenderProperties)rp).setHidden(!myShow);
        }
        else if (rp instanceof DefaultColorRenderProperties)
        {
            ((DefaultColorRenderProperties)rp).setHidden(!myShow);
        }
        else if (rp instanceof BaseRenderProperties)
        {
            ((BaseRenderProperties)rp).setHidden(!myShow);
        }
    }
}
