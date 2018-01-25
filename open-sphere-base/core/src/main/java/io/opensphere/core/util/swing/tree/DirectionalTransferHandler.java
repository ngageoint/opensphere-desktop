package io.opensphere.core.util.swing.tree;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler;

/**
 * A TransferHandler that indicates direction.
 */
public abstract class DirectionalTransferHandler extends TransferHandler
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Returns the last calculated value from canImport().
     *
     * @return The last value of can import
     */
    public abstract boolean couldImport();

    /**
     * Creates the transfereable object from the tree.
     *
     * @param c The tree.
     * @return The created transferable.
     */
    public abstract Transferable createTransferable(ListCheckBoxTree c);

    /**
     * Gets the order tree event controller.
     *
     * @return the controller
     */
    public abstract OrderTreeEventController getController();

    /**
     * Returns the direction of the drag.
     *
     * @return True for up, false for down
     */
    public abstract boolean isUp();
}
