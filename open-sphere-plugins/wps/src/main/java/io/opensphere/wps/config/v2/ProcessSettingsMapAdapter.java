package io.opensphere.wps.config.v2;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import io.opensphere.core.util.collections.New;
import io.opensphere.wps.config.v2.ProcessSettingsMapAdapter.ProcessSettings;

/** Process settings XML adapter. */
public class ProcessSettingsMapAdapter extends XmlAdapter<ProcessSettings, Map<String, ProcessSetting>>
{
    @Override
    public Map<String, ProcessSetting> unmarshal(ProcessSettings processes)
    {
        Map<String, ProcessSetting> map = New.map();
        for (ProcessSetting process : processes.getProcesses())
        {
            map.put(process.getIdentifier(), process);
        }
        return map;
    }

    @Override
    public ProcessSettings marshal(Map<String, ProcessSetting> map)
    {
        return new ProcessSettings(map.values());
    }

    /** A list of ProcessSetting objects. */
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ProcessSettings
    {
        /** The processes. */
        @XmlElement(name = "process")
        private final Collection<ProcessSetting> myProcesses;

        /**
         * Constructor.
         */
        public ProcessSettings()
        {
            myProcesses = New.list();
        }

        /**
         * Constructor.
         *
         * @param processes the processes
         */
        public ProcessSettings(Collection<ProcessSetting> processes)
        {
            myProcesses = processes;
        }

        /**
         * Gets the processes.
         *
         * @return the processes
         */
        public Collection<ProcessSetting> getProcesses()
        {
            return myProcesses;
        }
    }
}
