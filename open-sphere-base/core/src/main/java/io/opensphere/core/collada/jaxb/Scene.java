package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA scene.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Scene
{
    /** The mesh. */
    @XmlElement(name = "instance_visual_scene")
    private InstanceVisualScene myVisualScene;

    /**
     * Get the visual scene.
     *
     * @return The visual scene.
     */
    public InstanceVisualScene getVisualScene()
    {
        return myVisualScene;
    }

    /**
     * Set the visual scene.
     *
     * @param visualScene The visual scene.
     */
    public void setVisualScene(InstanceVisualScene visualScene)
    {
        myVisualScene = visualScene;
    }
}
