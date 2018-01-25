package io.opensphere.controlpanels.layers.activedata.controller;

import java.awt.Color;

import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.input.model.ColorModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;

/**
 * An opacity view model that adapts a ColorModel.
 */
public class OpacityColorAdapter extends IntegerModel implements Service
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Factor for opacity conversion. */
    private static final float OPACITY_FACTOR = 2.55f;

    /** The color model. */
    private final ColorModel myColorModel;

    /** Whether the adapter is open. */
    private boolean myIsOpen;

    /** The color model listener handle. */
    private final transient ObservableValueListenerHandle<Color> myColorModelHandle;

    /**
     * Constructor.
     *
     * @param colorModel The color model
     */
    public OpacityColorAdapter(ColorModel colorModel)
    {
        super(0, 100);
        myColorModel = colorModel;
        updateFromColorModel();
        myColorModelHandle = new ObservableValueListenerHandle<>(myColorModel, new ChangeListener<Color>()
        {
            @Override
            public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue)
            {
                updateFromColorModel();
            }
        });
    }

    @Override
    public void open()
    {
        myColorModelHandle.open();
        myIsOpen = true;
    }

    @Override
    public void close()
    {
        myColorModelHandle.close();
        myIsOpen = false;
    }

    @Override
    public boolean set(Integer value, boolean forceFire)
    {
        boolean changed = super.set(value, forceFire);
        if (changed && myIsOpen)
        {
            myColorModel.setAlpha(toAlpha(value.intValue()));
        }
        return changed;
    }

    /**
     * Updates the opacity from the color model.
     */
    private void updateFromColorModel()
    {
        set(Integer.valueOf(toOpacityPercent(myColorModel.get().getAlpha())));
    }

    /**
     * Converts an alpha to an opacity percent.
     *
     * @param alpha an alpha in the range 0-255
     * @return the opacity percent in the range 0-100
     */
    private static int toOpacityPercent(int alpha)
    {
        return Math.round(alpha / OPACITY_FACTOR);
    }

    /**
     * Converts an alpha to an opacity percent.
     *
     * @param opacityPercent an opacity percent in the range 0-100
     * @return the alpha in the range 0-255
     */
    private static int toAlpha(int opacityPercent)
    {
        return Math.round(opacityPercent * OPACITY_FACTOR);
    }
}
