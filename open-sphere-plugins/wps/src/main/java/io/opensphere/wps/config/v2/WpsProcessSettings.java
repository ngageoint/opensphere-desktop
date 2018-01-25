package io.opensphere.wps.config.v2;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/** Configuration bean for supported WPS processes. */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.NONE)
public class WpsProcessSettings
{
    /** The process settings. */
    @XmlElement(name = "processSettings")
    @XmlJavaTypeAdapter(ProcessSettingsMapAdapter.class)
    private final Map<String, ProcessSetting> myProcessSettings = new HashMap<>();

    /**
     * Adds a process setting.
     *
     * @param process the process setting
     */
    public void addProcessSetting(ProcessSetting process)
    {
        myProcessSettings.put(process.getIdentifier(), process);
    }

    /**
     * Gets a process setting.
     *
     * @param name the process name
     * @return the process setting, or null
     */
    public ProcessSetting getProcessSetting(String name)
    {
        return myProcessSettings.get(name);
    }

    /**
     * Gets a process setting, creating it if necessary.
     *
     * @param name the process name
     * @return the process setting, or null
     */
    public ProcessSetting getOrCreateProcessSetting(String name)
    {
        ProcessSetting process = getProcessSetting(name);
        if (process == null)
        {
            process = new ProcessSetting();
            process.setIdentifier(name);
            addProcessSetting(process);
        }
        return process;
    }
}
