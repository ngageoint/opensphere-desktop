package io.opensphere.kml.envoy;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.concurrent.Immutable;

import io.opensphere.core.util.collections.New;

/**
 * A store for tag information.
 */
@Immutable
public class Tags
{
    /** The name to tag info map. */
    private final Map<String, TagInfo> myNameToInfoMap;

    /** The alias to tag info map. */
    private final Map<String, TagInfo> myAliasToInfoMap;

    /**
     * Constructor.
     */
    public Tags()
    {
        Collection<TagInfo> tags = New.list();

        // Tags with aliases
        tags.add(new TagInfo("scale", "LabelStyle", Float.class, "labelScale"));
        tags.add(new TagInfo("color", "LabelStyle", String.class, "labelColor"));

        // Booleans
        tags.add(new TagInfo("extrude", null, Boolean.class));
        tags.add(new TagInfo("fill", null, Boolean.class));
        tags.add(new TagInfo("flyToView", null, Boolean.class));
        tags.add(new TagInfo("open", null, Boolean.class));
        tags.add(new TagInfo("outline", null, Boolean.class));
        tags.add(new TagInfo("refreshVisibility", null, Boolean.class));
        tags.add(new TagInfo("tessellate", null, Boolean.class));
        tags.add(new TagInfo("visibility", null, Boolean.class));

        // Integers
        tags.add(new TagInfo("drawOrder", null, Integer.class));

        // Floats
        tags.add(new TagInfo("heading", null, Float.class));
        tags.add(new TagInfo("refreshInterval", null, Float.class));
        tags.add(new TagInfo("rotation", null, Float.class));
        tags.add(new TagInfo("viewBoundScale", null, Float.class));
        tags.add(new TagInfo("viewRefreshTime", null, Float.class));
        tags.add(new TagInfo("width", null, Float.class));

        // Doubles
        tags.add(new TagInfo("altitude", null, Double.class));
        tags.add(new TagInfo("range", null, Double.class));
        tags.add(new TagInfo("x", null, Double.class));
        tags.add(new TagInfo("y", null, Double.class));
        tags.add(new TagInfo("z", null, Double.class));

        // Enums
        tags.add(new TagInfo("altitudeMode", null, Enum.class));
        tags.add(new TagInfo("colorMode", null, Enum.class));
        tags.add(new TagInfo("displayMode", null, Enum.class));
        tags.add(new TagInfo("key", null, Enum.class));
        tags.add(new TagInfo("refreshMode", null, Enum.class));
        tags.add(new TagInfo("shape", null, Enum.class));
        tags.add(new TagInfo("viewRefreshMode", null, Enum.class));

        Map<String, TagInfo> nameToInfoMap = New.map();
        Map<String, TagInfo> aliasToInfoMap = New.map();
        for (TagInfo tag : tags)
        {
            nameToInfoMap.put(tag.getName(), tag);
            for (String alias : tag.getKnownAliases())
            {
                aliasToInfoMap.put(alias, tag);
            }
        }

        myNameToInfoMap = Collections.unmodifiableMap(nameToInfoMap);
        myAliasToInfoMap = Collections.unmodifiableMap(aliasToInfoMap);
    }

    /**
     * Gets the tag info for the given name.
     *
     * @param name the tag name
     * @return the tag info, or null
     */
    public TagInfo getTagInfoForName(String name)
    {
        return myNameToInfoMap.get(name);
    }

    /**
     * Gets the tag info for the given alias.
     *
     * @param alias the tag alias
     * @return the tag info, or null
     */
    public TagInfo getTagInfoForAlias(String alias)
    {
        return myAliasToInfoMap.get(alias);
    }
}
