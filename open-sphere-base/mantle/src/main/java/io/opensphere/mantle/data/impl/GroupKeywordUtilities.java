package io.opensphere.mantle.data.impl;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Data group keyword search utilities.
 */
public final class GroupKeywordUtilities
{
    /**
     * Searches for the pattern.
     *
     * @param pattern the pattern
     * @param dataType the data type
     * @return whether the group/type match the pattern
     */
    public static boolean search(Pattern pattern, DataTypeInfo dataType)
    {
        DataGroupInfo dataGroup = dataType.getParent();
        return search(pattern, dataGroup, dataType, getDescription(dataGroup, dataType));
    }

    /**
     * Searches for the pattern.
     *
     * @param pattern the pattern
     * @param dataGroup the data group
     * @param dataType the data type
     * @param description the description
     * @return whether the group/type match the pattern
     */
    public static boolean search(Pattern pattern, DataGroupInfo dataGroup, DataTypeInfo dataType, String description)
    {
        boolean matches = false;

        if (dataGroup != null)
        {
            matches = pattern.matcher(dataGroup.getDisplayName()).matches();
            if (!matches && dataGroup.getProviderType() != null)
            {
                matches = pattern.matcher(dataGroup.getProviderType()).matches();
            }
            if (!matches && dataType == null && dataGroup.hasMembers(false))
            {
                for (DataTypeInfo dti : dataGroup.getMembers(false))
                {
                    matches = matchesDTIDisplayNameOrTags(pattern, dti);
                    if (matches)
                    {
                        break;
                    }
                }
            }
        }
        if (!matches && dataType != null)
        {
            matches = matchesDTIDisplayNameOrTags(pattern, dataType);
        }
        if (!matches && description != null)
        {
            matches = pattern.matcher(description).matches();
        }
        return matches;
    }

    /**
     * Gets a search pattern for the keyword.
     *
     * @param keyword the keyword
     * @return the search pattern
     */
    public static Pattern getSearchPattern(String keyword)
    {
        String regex = "(?i)((.*?)(" + quoteWithWildCards(keyword) + ")(.*?))";
        return Pattern.compile(regex);
    }

    /**
     * Matches data type display name or tags.
     *
     * @param searchPattern the search pattern
     * @param dataType the data type
     * @return true, if successful
     */
    private static boolean matchesDTIDisplayNameOrTags(Pattern searchPattern, DataTypeInfo dataType)
    {
        boolean matches;
        matches = searchPattern.matcher(dataType.getDisplayName()).matches();
        if (!matches)
        {
            for (String tag : dataType.getTags())
            {
                matches = searchPattern.matcher(tag).matches();
                if (matches)
                {
                    break;
                }
            }
        }
        return matches;
    }

    /**
     * Gets the description.
     *
     * @param dataGroup the data group
     * @param dataType the data type
     * @return the description
     */
    private static String getDescription(DataGroupInfo dataGroup, DataTypeInfo dataType)
    {
        String description = null;
        if (dataType != null)
        {
            description = dataType.getDescription();
        }
        else if (dataGroup != null)
        {
            description = dataGroup.getSummaryDescription();
        }
        return description;
    }

    /**
     * Quote with wild cards.
     *
     * @param viewFilter the view filter
     * @return the string
     */
    private static String quoteWithWildCards(String viewFilter)
    {
        if (viewFilter.indexOf('*') != -1)
        {
            StringTokenizer st = new StringTokenizer(viewFilter, "*");
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                result.append(Pattern.quote(token));
                result.append(".*");
            }
            return result.toString();
        }
        else
        {
            return viewFilter;
        }
    }

    /** Disallow instantiation. */
    private GroupKeywordUtilities()
    {
    }
}
