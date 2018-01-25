package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

import com.bric.swing.ColorPicker;

import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * Set properties for the CSV file.
 */
public class PropertiesPanel extends GridBagPanel implements ActionListener, MouseListener, DocumentListener
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The color choose bt. */
    private final JButton myColorChooseBT;

    /** The color preview panel. */
    private final JPanel myColorPreviewPanel;

    /** The data file name tf. */
    private final JTextField myDataFileNameTF;

    /** The data set name tf. */
    private final JTextField myDataSetNameTF;

    /** The disable focus lost. */
    private boolean myDisableFocusLost;

    /** The data set name error label. */
    private final JLabel myErrorLabel = new JLabel("Error");

    /** The import config. */
    private final ShapeFileSource myImportConfig;

    /** The existing name set. */
    private final Set<String> myExistingNameSet;

    /** The next bt. */
    private final JButton myNextBT;

    /**
     * Instantiates a new properties panel.
     *
     * @param importConfig the import config
     * @param existingNameSet the set of existing names
     * @param nextBT the next button
     */
    public PropertiesPanel(ShapeFileSource importConfig, Set<String> existingNameSet, JButton nextBT)
    {
        super();
        anchorWest();
        fillHorizontal();
        setInsets(5, 5, 5, 5);

        myImportConfig = importConfig;
        myExistingNameSet = existingNameSet;
        myNextBT = nextBT;

        JTextArea ta = new JTextArea(
                "Please set name used to identify the dataset, and the color for\n" + "the data once loaded.");
        ta.setEditable(false);
        ta.setBackground(getBackground());
        ta.setBorder(BorderFactory.createEmptyBorder());
        Font itemFont = ta.getFont().deriveFont(Font.BOLD, ta.getFont().getSize() + 2);
        ta.setFont(itemFont);

        myDataFileNameTF = new JTextField(myImportConfig.getShapeFileAbsolutePath());
        myDataFileNameTF.setEditable(false);
        JPanel fileChoicePanel = new JPanel(new BorderLayout());
        fileChoicePanel.setBorder(BorderFactory.createTitledBorder("File To Be Loaded"));
        fileChoicePanel.add(myDataFileNameTF, BorderLayout.CENTER);

        myDataSetNameTF = new JTextField(myImportConfig.getName());
        JPanel dataNamePanel = new JPanel(new BorderLayout());
        dataNamePanel.setBorder(BorderFactory.createTitledBorder("Data Set Name"));
        dataNamePanel.add(myDataSetNameTF, BorderLayout.CENTER);

        if (myImportConfig.getDateColumn() == -1 && myImportConfig.getTimeColumn() == -1)
        {
            myImportConfig.setLoadsTo(LoadsTo.STATIC);
        }
        else
        {
            myImportConfig.setLoadsTo(LoadsTo.TIMELINE);
        }

        myColorPreviewPanel = new JPanel();
        myColorPreviewPanel.setBackground(myImportConfig.getShapeColor());
        myColorPreviewPanel.setPreferredSize(new Dimension(36, 22));

        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.add(myColorPreviewPanel, BorderLayout.CENTER);

        myColorChooseBT = new JButton("Choose");
        myColorChooseBT.addActionListener(this);

        JPanel btPanel = new JPanel(new BorderLayout());
        btPanel.add(myColorChooseBT, BorderLayout.CENTER);

        JPanel colorChoicePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorChoicePanel.setBorder(BorderFactory.createTitledBorder("Data Color"));
        colorChoicePanel.add(tPanel);
        colorChoicePanel.add(btPanel);

        Box wp = Box.createVerticalBox();
        wp.add(fileChoicePanel);
        wp.add(Box.createVerticalStrut(5));
        wp.add(dataNamePanel);
        wp.add(Box.createVerticalStrut(5));
        wp.add(colorChoicePanel);
        wp.add(Box.createVerticalStrut(5));

        myErrorLabel.setForeground(Color.red);
        myErrorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dataNamePanel.add(myErrorLabel, BorderLayout.SOUTH);
        Box errorBox = Box.createHorizontalBox();
        errorBox.add(Box.createHorizontalGlue());
        errorBox.add(myErrorLabel);
        errorBox.add(Box.createHorizontalGlue());
        wp.add(errorBox);

        addRow(ta);
        addRow(wp);
        fillVerticalSpace();

        myDataSetNameTF.getDocument().addDocumentListener(this);
        validateSettings();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myColorChooseBT)
        {
            Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor((Component)e.getSource()),
                    myImportConfig.getShapeColor(), true);

            if (c != null)
            {
                myImportConfig.setShapeColor(c);
                myColorPreviewPanel.setBackground(c);
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        validateSettings();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        validateSettings();
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        if (!myDisableFocusLost)
        {
            validateSettings();
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        validateSettings();
    }

    /**
     * Validate data name.
     */
    private void validateSettings()
    {
        myNextBT.setVisible(false);
        myDisableFocusLost = true;
        boolean valid = true;

        if (valid)
        {
            String newName = myDataSetNameTF.getText();

            if (StringUtils.isEmpty(newName))
            {
                myErrorLabel.setText("ERROR: Data Set Name cannot be blank");
                valid = false;
            }
            else if (myExistingNameSet != null && myExistingNameSet.contains(newName))
            {
                myErrorLabel.setText("ERROR: Data Set Name already in use!");
                valid = false;
            }
            else
            {
                myErrorLabel.setText("VALID");
                myImportConfig.setName(newName);
            }
        }
        myDisableFocusLost = false;

        myErrorLabel.setForeground(valid ? Color.green : Color.red);

        myNextBT.setVisible(valid);
    }
}
