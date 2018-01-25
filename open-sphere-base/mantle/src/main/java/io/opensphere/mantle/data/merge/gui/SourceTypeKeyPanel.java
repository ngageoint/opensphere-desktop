package io.opensphere.mantle.data.merge.gui;

import java.awt.Color;
import java.util.List;

import io.opensphere.mantle.data.merge.gui.DataTypeKeyMoveDNDCoordinator.KeyMoveListener;

/**
 * The Class SourceTypeKeyPanel.
 */
@SuppressWarnings("serial")
public class SourceTypeKeyPanel extends TypeKeyPanel implements KeyMoveListener
{
    /** The Type. */
    private final String myTypeKey;

    /**
     * Instantiates a new source type key panel.
     *
     * @param title the title
     * @param typeKey the type
     * @param cdr the cdr
     */
    public SourceTypeKeyPanel(String title, String typeKey, DataTypeKeyMoveDNDCoordinator cdr)
    {
        super(title, cdr);
        myTypeKey = typeKey;
        cdr.addKeyMoveListener(this);
    }

    @Override
    public boolean addTypeKeyEntry(TypeKeyEntry entry)
    {
        if (allowsTypeKeyEntry(entry, null))
        {
            entry.setOwner(this);
            getListModel().addElement(entry);
            sortListEntries();
            return true;
        }
        return false;
    }

    @Override
    public boolean allowsTypeKeyEntry(TypeKeyEntry entry, List<String> errors)
    {
        return entry.getDataTypeKey().equals(myTypeKey);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    @Override
    public void keyMoveCompleted(TypeKeyEntry entry, TypeKeyPanel origPanel)
    {
        resetInnerPanelBorder();
    }

    @Override
    public void keyMoveInitiated(TypeKeyEntry entry, TypeKeyPanel sourcePanel, Object source)
    {
        if (allowsTypeKeyEntry(entry, null))
        {
            setInnerPanelBorderColor(Color.green);
        }
        else
        {
            setInnerPanelBorderColor(Color.red);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Type: ").append(myTypeKey).append('\n');
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            sb.append("   ").append(tke.toString()).append('\n');
        }
        return sb.toString();
    }
}
