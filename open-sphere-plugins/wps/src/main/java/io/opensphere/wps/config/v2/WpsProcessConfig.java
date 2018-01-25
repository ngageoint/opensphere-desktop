package io.opensphere.wps.config.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/** Configuration bean for supported WPS processes. */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.NONE)
public class WpsProcessConfig
{
    /** The process configs. */
    @XmlElement(name = "processConfigs")
    @XmlJavaTypeAdapter(ProcessConfigsMapAdapter.class)
    private final Map<String, ProcessConfig> myProcessConfigs = new HashMap<>();

    /**
     * Adds a process config.
     *
     * @param process the process config
     */
    public void addProcessConfig(ProcessConfig process)
    {
        myProcessConfigs.put(process.getIdentifier(), process);
    }

    /**
     * Gets a process config.
     *
     * @param name the process name
     * @return the process config, or null
     */
    public ProcessConfig getProcessConfig(String name)
    {
        return myProcessConfigs.get(name);
    }

    /**
     * Gets the supported process names.
     *
     * @return the supported process names
     */
    public Collection<String> getSupportedProcesses()
    {
        return myProcessConfigs.values().stream().map(c -> c.getIdentifier()).collect(Collectors.toList());
    }
}
