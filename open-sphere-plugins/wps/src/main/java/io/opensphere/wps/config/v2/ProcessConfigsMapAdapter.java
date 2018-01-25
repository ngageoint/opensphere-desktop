package io.opensphere.wps.config.v2;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import io.opensphere.core.util.collections.New;
import io.opensphere.wps.config.v2.ProcessConfigsMapAdapter.ProcessConfigs;

/** Process configs XML adapter. */
public class ProcessConfigsMapAdapter extends XmlAdapter<ProcessConfigs, Map<String, ProcessConfig>>
{
    @Override
    public Map<String, ProcessConfig> unmarshal(ProcessConfigs processes)
    {
        Map<String, ProcessConfig> map = New.map();
        for (ProcessConfig process : processes.getProcesses())
        {
            map.put(process.getIdentifier(), process);
        }
        return map;
    }

    @Override
    public ProcessConfigs marshal(Map<String, ProcessConfig> map)
    {
        return new ProcessConfigs(map.values());
    }

    /** A list of ProcessConfig objects. */
    @XmlAccessorType(XmlAccessType.NONE)
    public static class ProcessConfigs
    {
        /** The processes. */
        @XmlElement(name = "process")
        private final Collection<ProcessConfig> myProcesses;

        /**
         * Constructor.
         */
        public ProcessConfigs()
        {
            myProcesses = New.list();
        }

        /**
         * Constructor.
         *
         * @param processes the processes
         */
        public ProcessConfigs(Collection<ProcessConfig> processes)
        {
            myProcesses = processes;
        }

        /**
         * Gets the processes.
         *
         * @return the processes
         */
        public Collection<ProcessConfig> getProcesses()
        {
            return myProcesses;
        }
    }
}
