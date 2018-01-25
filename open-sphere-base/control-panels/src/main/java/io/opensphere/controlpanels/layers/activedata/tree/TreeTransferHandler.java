package io.opensphere.controlpanels.layers.activedata.tree;

import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.JComponent;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.tree.DirectionalTransferHandler;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.util.swing.tree.OrderTreeEventController;

/**
 * Allows the active data tree to have multiple transfer handlers and routes the
 * calls to multiple transfer handlers.
 */
public class TreeTransferHandler extends DirectionalTransferHandler
{
    /**
     * The serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The active handler.
     */
    private DirectionalTransferHandler myActiveHandler;

    /**
     * The default transfer handler.
     */
    private final DirectionalTransferHandler myDefaultHandler;

    /**
     * Tree handlers.
     */
    private final List<DirectionalTransferHandler> myHandlers = New.list();

    /**
     * Constructs a tree transfer handler.
     *
     * @param defaultHandler The default handler.
     */
    public TreeTransferHandler(DirectionalTransferHandler defaultHandler)
    {
        myDefaultHandler = defaultHandler;
        myActiveHandler = myDefaultHandler;
        myHandlers.add(myDefaultHandler);
    }

    /**
     * Adds a tree handler.
     *
     * @param handler The handler to add.
     */
    public void addTransferHandler(DirectionalTransferHandler handler)
    {
        myHandlers.add(handler);
    }

    @Override
    public boolean canImport(TransferSupport support)
    {
        return getActiveHandler(support).canImport(support);
    }

    @Override
    public boolean couldImport()
    {
        return myActiveHandler.couldImport();
    }

    @Override
    public Transferable createTransferable(ListCheckBoxTree c)
    {
        return myActiveHandler.createTransferable(c);
    }

    @Override
    public OrderTreeEventController getController()
    {
        return myActiveHandler.getController();
    }

    @Override
    public int getSourceActions(JComponent c)
    {
        return myActiveHandler.getSourceActions(c);
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        return myActiveHandler.importData(support);
    }

    @Override
    public boolean isUp()
    {
        return myActiveHandler.isUp();
    }

    /**
     * Removes a tree handler.
     *
     * @param handler The handler to remove.
     */
    public void removeTransferHandler(DirectionalTransferHandler handler)
    {
        myHandlers.remove(handler);
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
        Transferable transferable = null;
        if (c instanceof ListCheckBoxTree)
        {
            transferable = createTransferable((ListCheckBoxTree)c);
        }

        return transferable;
    }

    /**
     * Gets the active handler.
     *
     * @param support The transfer support.
     * @return The active handler.
     */
    private DirectionalTransferHandler getActiveHandler(TransferSupport support)
    {
        myActiveHandler = myDefaultHandler;
        for (DirectionalTransferHandler handler : myHandlers)
        {
            if (handler.getController() != null && handler.getController().isAllowDrag() && handler.canImport(support))
            {
                myActiveHandler = handler;
                break;
            }
        }

        return myActiveHandler;
    }
}
