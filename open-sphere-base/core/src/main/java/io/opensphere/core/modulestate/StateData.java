package io.opensphere.core.modulestate;

import java.util.Collection;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Data that describes a saved state.
 */
public class StateData
{
    /** The description for the state. */
    private final String myDescription;

    /** The id for the state. */
    private final String myId;

    /** The modules that the state applies to. */
    private final Collection<? extends String> myModules;

    /** The tags associated with the state. */
    private final Collection<? extends String> myTags;

    /**
     * Constructor.
     *
     * @param id The id for the state.
     * @param description The description for the state.
     * @param tags The tags associated with the state.
     * @param modules The modules that the state applies to.
     */
    public StateData(String id, String description, Collection<? extends String> tags, Collection<? extends String> modules)
    {
        myId = Utilities.checkNull(id, "id");
        myDescription = Utilities.checkNull(description, "description");
        myTags = New.unmodifiableCollection(Utilities.checkNull(tags, "tags"));
        myModules = New.unmodifiableCollection(Utilities.checkNull(modules, "modules"));
    }

    /**
     * Get the description of the state.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Get the id for the state.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Get the modules that the state applies to.
     *
     * @return The modules.
     */
    public Collection<? extends String> getModules()
    {
        return myModules;
    }

    /**
     * Get the tags associated with the state.
     *
     * @return The tags.
     */
    public Collection<? extends String> getTags()
    {
        return myTags;
    }
}
