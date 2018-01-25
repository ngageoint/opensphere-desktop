package io.opensphere.csv.ui;

import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.controller.DetectedParameters;
import io.opensphere.importer.config.LayerSettings;

/**
 * The Class CSVWizardPanel.
 */
public abstract class CSVWizardPanel extends GridBagPanel implements Validatable
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /**
     * Updates the CSV wizard panel model.
     *
     * @param parse the parse
     * @param layerSettings the layer settings
     * @param detected the detected parameters
     * @param cellSampler the cell sampler
     */
    public abstract void updateModel(CSVParseParameters parse, LayerSettings layerSettings, DetectedParameters detected,
            CellSampler cellSampler);
}
