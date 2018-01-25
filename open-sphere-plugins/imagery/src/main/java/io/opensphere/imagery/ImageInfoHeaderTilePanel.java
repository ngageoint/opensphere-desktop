package io.opensphere.imagery;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Class for the header of the import list.
 */
@SuppressWarnings("serial")
class ImageInfoHeaderTilePanel extends JPanel
{
    /** The Bands label. */
    private final JLabel myBandsLB = new JLabel("Bands");

    /** The Create ov label. */
    private final JLabel myCreateOvLB = new JLabel("Create");

    /** The Create ov l b2. */
    private final JLabel myCreateOvLB2 = new JLabel("Overview");

    /** The Datum label. */
    private final JLabel myDatumLB = new JLabel("Datum");

    /** The File name label. */
    private final JLabel myFileNameLB = new JLabel("File");

    /** The File name l b2. */
    private final JLabel myFileNameLB2 = new JLabel("Name");

    /** The Has overviews label. */
    private final JLabel myHasOverviewsLB = new JLabel("Has");

    /** The Has overviews l b2. */
    private final JLabel myHasOverviewsLB2 = new JLabel("Overviews");

    /** The Ig zero label. */
    private final JLabel myIgZeroLB = new JLabel("Ignore");

    /** The Ig zero l b2. */
    private final JLabel myIgZeroLB2 = new JLabel("Zeros");

    /** The Name label. */
    private final JLabel myNameLB = new JLabel("Name");

    /** The Projection label. */
    private final JLabel myProjectionLB = new JLabel("Projection");

    /**
     * Instantiates a new image info header tile panel.
     */
    public ImageInfoHeaderTilePanel()
    {
        super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(800, 30));
        setMaximumSize(new Dimension(5000, 30));
        setMinimumSize(new Dimension(800, 30));
        setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));

        add(Box.createHorizontalStrut(20));
        add(ImagerySourceWizardPanel.createSubPanel(myFileNameLB, myFileNameLB2, Constants.FILENAME_FIELD_WIDTH,
                Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myNameLB, null, Constants.NAME_FIELD_WIDTH, Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myProjectionLB, null, Constants.PROJECTION_FIELD_WIDTH, Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myDatumLB, null, Constants.DATUM_FIELD_WIDTH, Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myBandsLB, null, Constants.BANDS_FIELD_WIDTH, Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myIgZeroLB, myIgZeroLB2, Constants.IGZERO_FIELD_WIDTH, Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myHasOverviewsLB, myHasOverviewsLB2, Constants.HAS_OV_FIELD_WIDTH,
                Constants.LB_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myCreateOvLB, myCreateOvLB2, Constants.CREATE_OV_FIELD_WIDTH,
                Constants.LB_HEIGHT));
    }
}
