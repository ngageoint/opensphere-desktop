package io.opensphere.analysis.histogram.controller;

import java.util.concurrent.atomic.AtomicInteger;

import io.opensphere.analysis.base.controller.AbstractToolController;
import io.opensphere.analysis.base.model.BinDataModel;
import io.opensphere.analysis.base.model.CommonSettingsModel;
import io.opensphere.analysis.base.model.SettingsModel;
import io.opensphere.analysis.base.model.ToolModels;
import io.opensphere.core.Toolbox;

/** Histogram controller. */
public class HistogramController extends AbstractToolController
{
    /** The tool count. */
    private static final AtomicInteger TOOL_COUNT = new AtomicInteger();

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param commonModel The common settings model
     */
    public HistogramController(Toolbox toolbox, CommonSettingsModel commonModel)
    {
        super(toolbox, getModels(commonModel), "Histogram " + TOOL_COUNT.incrementAndGet());
    }

    /**
     * Creates the models class.
     *
     * @param commonModel The common settings model
     * @return the models class
     */
    private static ToolModels getModels(CommonSettingsModel commonModel)
    {
        return new ToolModels(new SettingsModel(commonModel), new BinDataModel());
    }

    @Override
    public void close()
    {
        super.close();
        TOOL_COUNT.decrementAndGet();
    }
}
