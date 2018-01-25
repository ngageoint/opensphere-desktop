package io.opensphere.core.appl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * A list of thread pool configs.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ThreadPoolConfigs
{
    /** List of configs. */
    @XmlElement(name = "threadPoolConfig")
    private final List<ThreadPoolConfig> myConfigs = New.list();

    /**
     * Get the configs.
     *
     * @return The configs.
     */
    public List<ThreadPoolConfig> getConfigs()
    {
        return myConfigs;
    }
}
