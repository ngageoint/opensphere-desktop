package io.opensphere.imagery;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.opensphere.mantle.util.MyButtons;

/**
 * Class for the individual tiles of the import list.
 */
@SuppressWarnings("serial")
class ImageInfoTilePanel extends JPanel implements ActionListener
{
    /** The Bands tf. */
    private JTextField myBandsTF;

    /** The Create overviews check box. */
    private JCheckBox myCreateOverviewsCheckBox;

    /** The Datum tf. */
    private final JTextField myDatumTF;

    /** The File name tf. */
    private final JTextField myFileNameTF;

    /** The Has overviews check box. */
    private final JCheckBox myHasOverviewsCheckBox;

    /** The Ignore zeros check box. */
    private final JCheckBox myIgnoreZerosCheckBox;

    /** The Info bt. */
    private JButton myInfoBT;

    /** The Listener. */
    private final ActionListener myListener;

    /** The Minus bt. */
    private JButton myMinusBT;

    /** The Name panel. */
    private JPanel myNamePanel;

    /** The Name tf. */
    private final JTextField myNameTF;

    /** The Projection tf. */
    private final JTextField myProjectionTF;

    /** The Read only dir. */
    private final boolean myReadOnlyDir;

    /** The Source. */
    private final ImageryFileSource mySource;

    /** The Wizard. */
    private final ImagerySourceWizardPanel myWizard;

    /**
     * Instantiates a new image info tile panel.
     *
     * @param source the source
     * @param wiz the wiz
     * @param listener the listener
     * @param showRemoveBt the show remove bt
     * @param readOnlyParentDir the read only parent dir
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ImageInfoTilePanel(ImageryFileSource source, ImagerySourceWizardPanel wiz, ActionListener listener,
            boolean showRemoveBt, boolean readOnlyParentDir)
    {
        super();
        myWizard = wiz;
        mySource = source;
        myListener = listener;
        myReadOnlyDir = readOnlyParentDir;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setPreferredSize(new Dimension(800, 30));
        setMaximumSize(new Dimension(5000, 30));
        setMinimumSize(new Dimension(800, 30));
        setBorder(BorderFactory.createRaisedBevelBorder());

        File aFile = new File(mySource.getFilePath());
        myFileNameTF = new JTextField(aFile.getName());
        myFileNameTF.setEditable(false);

        myNameTF = new JTextField(mySource.getName());
        myNameTF.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent documentevent)
            {
            }

            @Override
            public void insertUpdate(DocumentEvent documentevent)
            {
                mySource.setName(myNameTF.getText());
                requestRevalidation();
            }

            @Override
            public void removeUpdate(DocumentEvent documentevent)
            {
                mySource.setName(myNameTF.getText());
                requestRevalidation();
            }
        });
        myProjectionTF = new JTextField(mySource.getProjection());
        myProjectionTF.setEditable(false);
        myDatumTF = new JTextField(mySource.getDatum());
        myDatumTF.setEditable(false);
        myIgnoreZerosCheckBox = new JCheckBox("", mySource.ignoreZeros());
        myIgnoreZerosCheckBox.addActionListener(this);
        myHasOverviewsCheckBox = new JCheckBox("", mySource.hasOverviews());
        myHasOverviewsCheckBox.setEnabled(false);
        buildPart();

        if (showRemoveBt)
        {
            add(ImagerySourceWizardPanel.createSubPanel(myMinusBT, null, 20, Constants.ROW_HEIGHT - 6,
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        }

        refresh();
    }

    @Override
    public void actionPerformed(ActionEvent actionevent)
    {
        saveState();
    }

    /**
     * Validates the state of this tile.
     *
     * @return the string
     */
    public String areSettingsValid()
    {
        String errorText = null;
        if (myNameTF != null && myNamePanel != null)
        {
            boolean nameInUse = myWizard.isFileNameInUse(myNameTF.getText(), mySource);
            if (nameInUse)
            {
                myNamePanel.setBorder(BorderFactory.createLineBorder(Color.RED));
                errorText = "ERROR: The name \"" + myNameTF.getText() + "\" is already in use.  Names must be unique.";
            }
            else
            {
                myNamePanel.setBorder(BorderFactory.createEmptyBorder());
            }
        }
        return errorText;
    }

    /**
     * Gets the descriptive text for the source used by this tile.
     *
     * @return the descriptive text
     */
    public String getDescriptiveText()
    {
        return "FILE: " + mySource.getFilePath() + "\n" + mySource.getDescription();
    }

    /**
     * Gets the source used by this tile.
     *
     * @return the source
     */
    public ImageryFileSource getSource()
    {
        return mySource;
    }

    /**
     * Refreshes the information displayed in this tile from the source.
     */
    public final void refresh()
    {
        File aFile = new File(mySource.getFilePath());
        myFileNameTF.setText(aFile.getName());
        myNameTF.setText(mySource.getName());
        myProjectionTF.setText(mySource.getProjection());
        myDatumTF.setText(mySource.getDatum());
        myIgnoreZerosCheckBox.setSelected(mySource.ignoreZeros());
        myHasOverviewsCheckBox.setSelected(mySource.hasOverviews());
        myHasOverviewsCheckBox.setVisible(mySource.hasOverviews());
        myCreateOverviewsCheckBox.setSelected(mySource.isCreateOverviews());
        myCreateOverviewsCheckBox.setVisible(!mySource.hasOverviews() && !myReadOnlyDir);
        myBandsTF.setText(Integer.toString(mySource.getBands()));
        requestRevalidation();
    }

    /**
     * Requests that the parent of this tile revalidate it with other sources in
     * the import list.
     */
    public final void requestRevalidation()
    {
        myListener.actionPerformed(new ActionEvent(this, 0, Constants.REVALIDATE_TILE_CONTENT));
    }

    /**
     * Saves the state of the tile's editor back into the source.
     */
    public void saveState()
    {
        mySource.setName(myNameTF.getText());
        mySource.setIgnoreZeros(myIgnoreZerosCheckBox.isSelected());
        mySource.setCreateOverviews(myCreateOverviewsCheckBox.isSelected());
    }

    /**
     * Sets the ignore zeros.
     *
     * @param ignore the new ignore zeros
     */
    public void setIgnoreZeros(boolean ignore)
    {
        myIgnoreZerosCheckBox.setSelected(ignore);
    }

    /**
     * Builds the part.
     */
    private void buildPart()
    {
        myCreateOverviewsCheckBox = new JCheckBox("", mySource.isCreateOverviews());
        myCreateOverviewsCheckBox.setVisible(mySource.hasOverviews() && !myReadOnlyDir);
        myCreateOverviewsCheckBox.addActionListener(this);
        myBandsTF = new JTextField(mySource.getBands());
        myBandsTF.setEditable(false);

        myInfoBT = MyButtons.createIButton();
        myInfoBT.setToolTipText("Show File Info");
        myInfoBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myListener.actionPerformed(new ActionEvent(getDescriptiveText(), 0, Constants.REFRESH_FILE_INFO_AREA));
            }
        });

        myMinusBT = MyButtons.createMinusButton();
        myMinusBT.setToolTipText("Remove Import File");
        myMinusBT.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myListener.actionPerformed(new ActionEvent(ImageInfoTilePanel.this, 0, Constants.REMOVE_SELECTED_IMPORT_FILE));
            }
        });

        add(ImagerySourceWizardPanel.createSubPanel(myInfoBT, null, 20, Constants.ROW_HEIGHT - 6,
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(ImagerySourceWizardPanel.createSubPanel(myFileNameTF, null, Constants.FILENAME_FIELD_WIDTH, Constants.ROW_HEIGHT));
        myNamePanel = ImagerySourceWizardPanel.createSubPanel(myNameTF, null, Constants.NAME_FIELD_WIDTH, Constants.ROW_HEIGHT);
        add(myNamePanel);
        add(ImagerySourceWizardPanel.createSubPanel(myProjectionTF, null, Constants.PROJECTION_FIELD_WIDTH,
                Constants.ROW_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myDatumTF, null, Constants.DATUM_FIELD_WIDTH, Constants.ROW_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myBandsTF, null, Constants.BANDS_FIELD_WIDTH, Constants.ROW_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myIgnoreZerosCheckBox, null, Constants.IGZERO_FIELD_WIDTH,
                Constants.ROW_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myHasOverviewsCheckBox, null, Constants.HAS_OV_FIELD_WIDTH,
                Constants.ROW_HEIGHT));
        add(ImagerySourceWizardPanel.createSubPanel(myCreateOverviewsCheckBox, null, Constants.CREATE_OV_FIELD_WIDTH,
                Constants.ROW_HEIGHT));
    }
}
