package io.opensphere.osh.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultTimeExtents;

/** OpenSensorHub data type. */
public class OSHDataTypeInfo extends DefaultDataTypeInfo
{
    /** The offering. */
    private final Offering myOffering;

    /** The result template. */
    private final Map<String, Output> myResultTemplates = Collections.synchronizedMap(New.map());

    /** Whether this data type is for real video. */
    private volatile boolean myIsVideo;

    /**
     * The list of outputs this data type represents.
     */
    private final List<Output> myOutputs = Collections.synchronizedList(New.list());

    /** The animation plan, if any, created for this layer. */
    private volatile AnimationPlan myPlan;

    /**
     * Constructor.
     *
     * @param tb the tool box
     * @param sourcePrefix the source prefix
     * @param url the URL
     * @param offering the offering
     * @param outputs the output
     */
    public OSHDataTypeInfo(Toolbox tb, String sourcePrefix, String url, Offering offering, List<Output> outputs)
    {
        super(tb, sourcePrefix, getTypeKey(url, offering, outputs.get(0)), "OshKosh B'gosh", getName(offering, outputs.get(0)),
                true);
        myOffering = offering;
        myOutputs.addAll(outputs);
        setTimeExtents(new DefaultTimeExtents(offering.getSpan()), this);
        setUrl(url);
        setDescription(offering.getDescription());
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        // Don't need other fields because the type key should be unique
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        // Don't need other fields because the type key should be unique
        return this == obj || super.equals(obj) && getClass() == obj.getClass();
    }

    /**
     * Gets the offering.
     *
     * @return the offering
     */
    public Offering getOffering()
    {
        return myOffering;
    }

    /**
     * Gets the first output.
     *
     * @return the output
     */
    public Output getOutput()
    {
        return myOutputs.get(0);
    }

    /**
     * Gets all the outputs that make up this data type.
     *
     * @return The outputs this data type represents.
     */
    public List<Output> getOutputs()
    {
        return myOutputs;
    }

    /**
     * Gets the resultTemplate.
     *
     * @param output The output to get the result template for.
     * @return the resultTemplate
     */
    public Output getResultTemplate(Output output)
    {
        return myResultTemplates.get(output.getName());
    }

    /**
     * Sets the resultTemplate.
     *
     * @param output The output the result template is for.
     * @param resultTemplate the resultTemplate
     */
    public void setResultTemplate(Output output, Output resultTemplate)
    {
        myResultTemplates.put(output.getName(), resultTemplate);
    }

    /**
     * Gets the isVideo.
     *
     * @return the isVideo
     */
    public boolean isVideo()
    {
        return myIsVideo;
    }

    /**
     * Sets the isVideo.
     *
     * @param isVideo the isVideo
     */
    public void setVideo(boolean isVideo)
    {
        myIsVideo = isVideo;
    }

    /**
     * Gets the plan.
     *
     * @return the plan
     */
    public AnimationPlan getPlan()
    {
        return myPlan;
    }

    /**
     * Sets the plan.
     *
     * @param plan the plan
     */
    public void setPlan(AnimationPlan plan)
    {
        myPlan = plan;
    }

    /**
     * Gets whether this type is a near real-time streaming layer.
     *
     * @return whether it's NRT streaming
     */
    public boolean isNrtStreaming()
    {
        return myOffering.getSpan().isUnboundedEnd();
    }

    /**
     * Gets the type key.
     *
     * @param url the URL
     * @param offering the offering
     * @param output the output
     * @return the type key
     */
    private static String getTypeKey(String url, Offering offering, Output output)
    {
        return StringUtilities.concat(url, "/", offering.getId(), "/", output.getName());
    }

    /**
     * Gets the layer name.
     *
     * @param offering the offering
     * @param output the output
     * @return the type key
     */
    private static String getName(Offering offering, Output output)
    {
        return StringUtilities.concat(offering.getName(), " ", output.getName());
    }
}
