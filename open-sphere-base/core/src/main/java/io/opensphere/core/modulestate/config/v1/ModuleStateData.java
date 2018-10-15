package io.opensphere.core.modulestate.config.v1;

import java.util.Collection;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

import org.w3c.dom.Element;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Data that describes a saved state.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ModuleStateData
{
    /** The active state of the state. */
    @XmlElement(name = "active")
    private boolean myActive;

    /** The description of the state. */
    @XmlElement(name = "description")
    private String myDescription = StringUtilities.EMPTY;

    /** The node containing the state information. */
    @XmlAnyElement
    private Element myElement;

    /** The id of the state. */
    @XmlElement(name = "id")
    private String myId;

    /** The modules that the state applies to. */
    @XmlElement(name = "module")
    private Collection<? extends String> myModules;

    /** The tags associated with the state. */
    @XmlElement(name = "tags")
    @XmlList
    private Collection<? extends String> myTags = New.collection(0);

    /** The state object. */
    @XmlElement(name = "state")
    private StateType myState;

    /**
     * Constructor.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags associated with the state.
     * @param active If the state is active.
     * @param modules The modules that the state applies to.
     * @param element The element containing the state information.
     */
    public ModuleStateData(String id, String description, Collection<? extends String> tags, boolean active,
            Collection<? extends String> modules, Element element)
    {
        this(id, description, tags, active, modules, Utilities.checkNull(element, "element"), null);
    }

    /**
     * Constructor.
     *
     * @param id The id of the state.
     * @param description The description of the state.
     * @param tags The tags associated with the state.
     * @param active If the state is active.
     * @param modules The modules that the state applies to.
     * @param element The element containing the state information.
     * @param state The state object
     */
    public ModuleStateData(String id, String description, Collection<? extends String> tags, boolean active,
            Collection<? extends String> modules, Element element, StateType state)
    {
        myId = Utilities.checkNull(id, "id");
        myDescription = Utilities.checkNull(description, "description");
        myTags = New.unmodifiableCollection(Utilities.checkNull(tags, "tags"));
        myActive = active;
        myModules = New.unmodifiableCollection(Utilities.checkNull(modules, "modules"));
        myElement = element;
        myState = state;
    }

    /**
     * Constructor for JAXB.
     */
    protected ModuleStateData()
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ModuleStateData other = (ModuleStateData)obj;
        return myActive == other.myActive && Objects.equals(myElement, other.myElement) && Objects.equals(myId, other.myId)
                && Objects.equals(myModules, other.myModules) && Objects.equals(myState, other.myState);
    }

    /**
     * Get the state description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Get the element containing the state information.
     *
     * @return The DOM element.
     */
    public Element getElement()
    {
        return myElement;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public StateType getState()
    {
        return myState;
    }

    /**
     * Get the state id.
     *
     * @return The state id.
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myActive ? 1231 : 1237);
        result = prime * result + (myElement == null ? 0 : myElement.hashCode());
        result = prime * result + (myId == null ? 0 : myId.hashCode());
        result = prime * result + (myModules == null ? 0 : myModules.hashCode());
        result = prime * result + (myState == null ? 0 : myState.hashCode());
        return result;
    }

    /**
     * Get if the state should be active.
     *
     * @return The active state.
     */
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Set if the state is active.
     *
     * @param active If the state is active.
     */
    public void setActive(boolean active)
    {
        myActive = active;
    }

    /**
     * Set the element comprising the state.
     *
     * @param element The element.
     */
    public void setElement(Element element)
    {
        myElement = element;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Set the modules.
     *
     * @param modules The modules.
     */
    public void setModules(Collection<? extends String> modules)
    {
        myModules = New.unmodifiableCollection(modules);
    }
}
