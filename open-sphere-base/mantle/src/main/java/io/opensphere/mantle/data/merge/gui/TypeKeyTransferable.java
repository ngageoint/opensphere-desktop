package io.opensphere.mantle.data.merge.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

/**
 * The Class TypeKeyTransferable.
 */
public class TypeKeyTransferable implements Transferable
{
    /** The Constant ourFlavorArray. */
    private static final DataFlavor[] ourFlavorArray;

    /** The Type key entry. */
    private final TypeKeyEntry myTypeKeyEntry;

    static
    {
        ourFlavorArray = new DataFlavor[1];
        ourFlavorArray[0] = TypeKeyEntry.ourDataFlavor;
    }

    /**
     * Instantiates a new type key transferable.
     *
     * @param entry the entry
     */
    public TypeKeyTransferable(TypeKeyEntry entry)
    {
        myTypeKeyEntry = entry;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        if (flavor == TypeKeyEntry.ourDataFlavor)
        {
            return myTypeKeyEntry;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return Arrays.copyOf(ourFlavorArray, 1);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor == TypeKeyEntry.ourDataFlavor;
    }
}
