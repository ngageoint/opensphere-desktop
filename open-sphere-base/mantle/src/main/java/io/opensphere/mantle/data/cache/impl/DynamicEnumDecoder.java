package io.opensphere.mantle.data.cache.impl;

import java.util.List;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationKey;
import io.opensphere.mantle.util.dynenum.DynamicEnumerationRegistry;

/**
 * The Class DynamicEnumDecoder.
 */
public final class DynamicEnumDecoder
{
    /**
     * Decodes a list by going through its elements, detecting any dynamic enum
     * values, and decoding them back to their original values from the.
     *
     * @param reg the reg
     * @param source the source list to be decoded.
     * @return the decoded list. {@link DynamicEnumerationRegistry}. If none of
     *         the values are dynamic enumeration types the original list is
     *         returned.
     */
    public static List<Object> decode(DynamicEnumerationRegistry reg, List<Object> source)
    {
        List<Object> result = null;
        if (source != null && !source.isEmpty())
        {
            int index = 0;
            boolean copyOver = false;
            Object toAdd = null;
            for (Object obj : source)
            {
                toAdd = obj;
                if (obj instanceof DynamicEnumerationKey)
                {
                    if (result == null)
                    {
                        result = New.list(source.size());
                        copyOver = true;
                        if (index > 0)
                        {
                            int copied = 0;
                            for (Object o2 : source)
                            {
                                result.add(o2);
                                copied++;
                                if (copied == index)
                                {
                                    break;
                                }
                            }
                        }
                    }
                    toAdd = reg.getEnumerationValue((DynamicEnumerationKey)obj);
                }
                if (copyOver)
                {
                    result.add(toAdd);
                }
                index++;
            }
        }
        return result == null ? source : result;
    }

    /**
     * Instantiates a new dynamic enum decoder list proxy.
     */
    private DynamicEnumDecoder()
    {
        // Don't allow instantiation.
    }
}
