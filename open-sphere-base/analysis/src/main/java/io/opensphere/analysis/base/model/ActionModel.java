package io.opensphere.analysis.base.model;

import java.util.function.Consumer;

/** Model for dealing with UI actions that don't affect settings or data. */
public class ActionModel
{
    /** The bin selection listener. */
    private volatile Consumer<UIBin> myBinSelectionListener;

    /**
     * Sets the binSelectionListener.
     *
     * @param binSelectionListener the binSelectionListener
     */
    public void setBinSelectionListener(Consumer<UIBin> binSelectionListener)
    {
        myBinSelectionListener = binSelectionListener;
    }

    /**
     * Notifies the model listener that a bin was selected.
     *
     * @param bin the bin
     */
    public void binSelected(UIBin bin)
    {
        Consumer<UIBin> binSelectionListener = myBinSelectionListener;
        if (binSelectionListener != null)
        {
            binSelectionListener.accept(bin);
        }
    }
}
