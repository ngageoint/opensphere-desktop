package io.opensphere.mantle.data.merge.gui;

import java.util.List;
import java.util.Set;

import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class SpecialMappedTypeKeyPanel.
 */
@SuppressWarnings("serial")
public class SpecialMappedTypeKeyPanel extends MappedTypeKeyPanel
{
    /** The Special key. */
    private final SpecialKey mySpecialKey;

    /**
     * Instantiates a new source type key panel.
     *
     * @param cdr the cdr
     * @param dtmp the dtmp
     * @param title the title
     * @param classType the class type
     * @param restrictByClassType the restrict by class type
     * @param specKey the spec key
     */
    public SpecialMappedTypeKeyPanel(DataTypeKeyMoveDNDCoordinator cdr, DataTypeMergePanel dtmp, String title, String classType,
            boolean restrictByClassType, SpecialKey specKey)
    {
        super(cdr, dtmp, title, classType, restrictByClassType);
        mySpecialKey = specKey;
    }

    @Override
    public boolean allowsTypeKeyEntry(TypeKeyEntry entry, List<String> errors)
    {
        boolean allows = super.allowsTypeKeyEntry(entry, errors);
        if (allows)
        {
            if (entry.getSpecialKeyType() == null || mySpecialKey != null && !mySpecialKey.equals(entry.getSpecialKeyType()))
            {
                Set<String> keynameSet = getKeyNameSet(true);
                if (getKeyName().equalsIgnoreCase(entry.getKeyName()) || keynameSet.contains(entry.getKeyName().toLowerCase()))
                {
                    Set<String> ctSet = getClassTypeSet();
                    if (!ctSet.contains(entry.getClassType()))
                    {
                        allows = false;
                    }
                }
                else
                {
                    allows = false;
                }
            }
            if (!allows && errors != null)
            {
                errors.add("This key only allows the special type \"" + mySpecialKey.getKeyName()
                        + "\" or\nkeys with the same name as an existing key and data type");
            }
        }
        return allows;
    }

    /**
     * Gets the special key.
     *
     * @return the special key
     */
    public SpecialKey getSpecialKey()
    {
        return mySpecialKey;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append("Type[").append(getKeyName()).append("] ClassType[").append(getClassType())
                .append("] SpecialType[").append(mySpecialKey == null ? "NULL" : mySpecialKey.getClass().getSimpleName())
                .append("]\n");
        for (TypeKeyEntry tke : getTypeEntryList())
        {
            sb.append("   ").append(tke.toString()).append('\n');
        }
        return sb.toString();
    }
}
