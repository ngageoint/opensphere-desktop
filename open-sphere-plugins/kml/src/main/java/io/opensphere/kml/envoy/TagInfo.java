package io.opensphere.kml.envoy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.jcip.annotations.Immutable;

/**
 * Info about a KML tag.
 */
@Immutable
public class TagInfo
{
    /** The name. */
    private final String myName;

    /** The parent tag. */
    private final String myParent;

    /** The class type. */
    private final Class<?> myType;

    /** The known aliases. */
    private final Collection<String> myKnownAliases;

    /**
     * Constructor.
     *
     * @param name The name
     * @param parent The parent tag
     * @param type The class type
     * @param knownAliases The known aliases
     */
    public TagInfo(String name, String parent, Class<?> type, String... knownAliases)
    {
        super();
        myName = name;
        myParent = parent;
        myType = type;
        myKnownAliases = Collections.unmodifiableList(Arrays.asList(knownAliases));
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    public String getParent()
    {
        return myParent;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Class<?> getType()
    {
        return myType;
    }

    /**
     * Gets the known aliases.
     *
     * @return the known aliases
     */
    public Collection<String> getKnownAliases()
    {
        return myKnownAliases;
    }
}
