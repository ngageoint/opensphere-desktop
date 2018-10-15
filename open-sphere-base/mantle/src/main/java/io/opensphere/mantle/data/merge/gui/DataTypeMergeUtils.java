package io.opensphere.mantle.data.merge.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;

/**
 * The Class DataTypeMergeUtils.
 */
public final class DataTypeMergeUtils
{
    /**
     * Creates the type key entries from meta data info.
     *
     * @param dtiNakeKeyPair the dti nake key pair
     * @param mdi the {@link MetaDataInfo}
     * @return the list of TypeKeyEntry
     */
    public static List<TypeKeyEntry> createTypeKeyEntriesFromMetaDataInfo(DTINameKeyPair dtiNakeKeyPair, MetaDataInfo mdi)
    {
        Utilities.checkNull(mdi, "mdi");
        Utilities.checkNull(dtiNakeKeyPair, "dtiKey");
        if (mdi.getKeyCount() == 0)
        {
            return Collections.<TypeKeyEntry>emptyList();
        }
        List<TypeKeyEntry> resultList = new ArrayList<>(mdi.getKeyCount());
        for (String key : mdi.getKeyNames())
        {
            Class<?> keyClass = mdi.getKeyClassTypeMap().get(key);
            SpecialKey sk = mdi.getSpecialTypeForKey(key);
            resultList.add(new TypeKeyEntry(dtiNakeKeyPair, key, keyClass, sk));
        }
        return resultList;
    }

    /**
     * Instantiates a new data type merge utils.
     */
    private DataTypeMergeUtils()
    {
    }
}
