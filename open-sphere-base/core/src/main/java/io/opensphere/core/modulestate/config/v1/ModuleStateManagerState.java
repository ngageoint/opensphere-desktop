package io.opensphere.core.modulestate.config.v1;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * JAXB object used to store the state of the module state manager.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement
public class ModuleStateManagerState
{
    /** The collection of state data. */
    @XmlElement(name = "stateData")
    private Collection<ModuleStateData> myStateData = New.collection();

    /** Default constructor. */
    public ModuleStateManagerState()
    {
    }

    /**
     * Construct that takes a collection of state data.
     *
     * @param stateData The state data.
     */
    public ModuleStateManagerState(Collection<ModuleStateData> stateData)
    {
        myStateData = stateData;
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
        ModuleStateManagerState other = (ModuleStateManagerState)obj;
        if (myStateData == null)
        {
            if (other.myStateData != null)
            {
                return false;
            }
        }
        else if (myStateData.size() == other.myStateData.size() && myStateData.containsAll(other.myStateData))
        {
            return false;
        }
        return true;
    }

    /**
     * Get the state data.
     *
     * @return The state data.
     */
    public Collection<ModuleStateData> getStateData()
    {
        return myStateData;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myStateData == null ? 0 : myStateData.hashCode());
        return result;
    }

    /**
     * Set the state data.
     *
     * @param stateData The state data.
     */
    public void setStateData(Collection<ModuleStateData> stateData)
    {
        myStateData = stateData;
    }
}
