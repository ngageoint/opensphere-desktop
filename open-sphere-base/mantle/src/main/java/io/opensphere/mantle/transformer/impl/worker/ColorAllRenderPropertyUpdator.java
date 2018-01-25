package io.opensphere.mantle.transformer.impl.worker;

import java.awt.Color;

import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.LabelRenderProperties;
import io.opensphere.core.geometry.renderproperties.PointRenderProperties;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.util.MantleConstants;

/**
 * The Class ColorAllRenderPropertyUpdator.
 */
public class ColorAllRenderPropertyUpdator extends AbstractRenderPropertyUpdator
{
    /** The Color. */
    private final Color myColor;

    /** The Opacity change only. */
    private final boolean myOpacityChangeOnly;

    /**
     * Instantiates a new color render property updator.
     *
     * @param provider the provider
     * @param c the c
     * @param opacityChangeOnly the opacity change only
     */
    public ColorAllRenderPropertyUpdator(DataElementTransformerWorkerDataProvider provider, Color c, boolean opacityChangeOnly)
    {
        super(provider);
        myColor = c;
        myOpacityChangeOnly = opacityChangeOnly;
    }

    @Override
    public void adjust(RenderProperties rp)
    {
        if (rp instanceof PointRenderProperties)
        {
            PointRenderProperties dprp = (PointRenderProperties)rp;
            Color c = dprp.getColor();
            if (!Utilities.sameInstance(c, MantleConstants.SELECT_COLOR))
            {
                if (myOpacityChangeOnly)
                {
                    dprp.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), myColor.getAlpha()));
                }
                else
                {
                    dprp.setColor(myColor);
                }
            }
        }
        // If this is an opacity change only, it's coming from an opacity
        // slider, and we don't want opacity sliders to change label opacity. So
        // ignore the adjustment for label render properties.
        else if (rp instanceof ColorRenderProperties && !(rp instanceof LabelRenderProperties && myOpacityChangeOnly))
        {
            ColorRenderProperties dcrp = (ColorRenderProperties)rp;
            Color c = dcrp.getColor();
            if (!Utilities.sameInstance(c, MantleConstants.SELECT_COLOR))
            {
                if (myOpacityChangeOnly)
                {
                    dcrp.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), myColor.getAlpha()));
                }
                else
                {
                    dcrp.setColor(myColor);
                }
            }
        }
    }
}
