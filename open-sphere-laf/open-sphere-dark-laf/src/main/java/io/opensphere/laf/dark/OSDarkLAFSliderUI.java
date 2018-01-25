package io.opensphere.laf.dark;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalSliderUI;

public class OSDarkLAFSliderUI extends MetalSliderUI
{
    /**
     * Utility method used to create the ComponentUI.
     *
     * @param pComponent the component for which the UI will be created.
     * @return a new ComponentUI created for the supplied component.
     */
    public static ComponentUI createUI(JComponent pComponent)
    {
        return new OSDarkLAFSliderUI();
    }

    public OSDarkLAFSliderUI()
    {
        super();
    }

    @Override
    protected int getThumbOverhang()
    {
        return super.getThumbOverhang() + (slider.getOrientation() == JSlider.HORIZONTAL ? 0 : 2);
    }
}
