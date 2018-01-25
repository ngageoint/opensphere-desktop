package io.opensphere.controlpanels.layers.importdata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXCollapsiblePane.Direction;

import io.opensphere.controlpanels.layers.importdata.ImportDataController.ImportDataControllerListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.FileOrURLImporterMenuItem;
import io.opensphere.core.importer.ImportType;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.SplitButton;

/**
 * The Class ImportDataPanel.
 */
public class ImportDataPanel extends AbstractHUDPanel implements ImportDataControllerListener, ActionListener
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ImportDataPanel.class);

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The advanced panel. */
    private JXCollapsiblePane myAdvancedContentPanel;

    /** The controller. */
    private final ImportDataController myController;

    /** The enter url button. */
    private JMenuItem myEnterURLButton;

    /** The import file split button. */
    private SplitButton myImportFileButton;

    /** The specific url importer menu. */
    private JMenu mySpecificURLImporterMenu;

    /** The ImporterRegistry.  Duh. */
    private ImporterRegistry impReg;

    /**
     * Instantiates a new import data panel.
     *
     * @param tb the tb
     */
    public ImportDataPanel(Toolbox tb)
    {
        super(tb.getPreferencesRegistry());
        impReg = tb.getImporterRegistry();
        setLayout(new BorderLayout());
        myController = ImportDataController.getInstance(tb);

        setBackground(getBackgroundColor());

        GridBagPanel panel = new GridBagPanel();
        panel.add(getImportFileButton());
        add(panel, BorderLayout.CENTER);

        myController.addListener(this);

        buildImportMenuOptions();
        myController.addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() instanceof FileOrURLImporterMenuItem)
        {
            FileOrURLImporterMenuItem importerMI = (FileOrURLImporterMenuItem)e.getSource();
            myController.importSpecific(importerMI.getImporter(), importerMI.getImportType());
        }
    }

    @Override
    public void importersChanged()
    {
        buildImportMenuOptions();
    }

    /**
     * Method used internally to this class (or its inherited classes) to
     * enable/disable the panel.
     *
     * @param expanded the new panel state
     */
    protected void setPanelState(final boolean expanded)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                getAdvancedContentPanel().setCollapsed(!expanded);
                repaint();
            }
        });
    }

    /**
     * Adds the file initiated.
     */
    private void addFileInitiated()
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Add File Initiated");
        }
        myController.importFile();
    }

    /**
     * Builds the file import menu options.
     */
    private void buildImportMenuOptions()
    {
        getImportFileButton().removeAll();

        getImportFileButton().add(getEnterURLButton());
        getSpecificURLImporterMenu().removeAll();
        List<FileOrURLImporter> fileImporters = impReg.getUrlImporters();
        if (fileImporters.size() > 1)
        {
            for (FileOrURLImporter importer : fileImporters)
            {
                if (importer.importsURLs())
                {
                    getSpecificURLImporterMenu().add(makeFouimi(importer, ImportType.URL, this));
                }
            }
            getImportFileButton().add(getSpecificURLImporterMenu());
        }
        getImportFileButton().add(new JSeparator());

        for (FileOrURLImporter importer : impReg.getFileImporters())
        {
            if (importer.importsFiles())
            {
                getImportFileButton().addMenuItem(makeFouimi(importer, ImportType.FILE, this));
            }
            if (importer.importsFileGroups())
            {
                getImportFileButton().addMenuItem(makeFouimi(importer, ImportType.FILE_GROUP, this));
            }
        }
    }

    /**
     * Bla.
     * @param imp bla
     * @param t bla
     * @param ear bla
     * @return bla
     */
    private JMenuItem makeFouimi(FileOrURLImporter imp, ImportType t, ActionListener ear)
    {
        JMenuItem jmi = new FileOrURLImporterMenuItem(imp, t);
        if (ear != null)
        {
            jmi.addActionListener(ear);
        }
        return jmi;
    }

    /**
     * Gets the advanced content panel.
     *
     * @return the advanced content panel
     */
    private JXCollapsiblePane getAdvancedContentPanel()
    {
        if (myAdvancedContentPanel == null)
        {
            myAdvancedContentPanel = new JXCollapsiblePane(Direction.DOWN);
            myAdvancedContentPanel.setBorder(BorderFactory.createLineBorder(Color.green));
            myAdvancedContentPanel.setCollapsed(true);
            for (int i = 0; i < 20; i++)
            {
                myAdvancedContentPanel.add(new JLabel("Test"));
            }
        }
        return myAdvancedContentPanel;
    }

    /**
     * Gets the enter url button.
     *
     * @return the enter url button
     */
    private JMenuItem getEnterURLButton()
    {
        if (myEnterURLButton == null)
        {
            myEnterURLButton = new JMenuItem("Import URL...");
            myEnterURLButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    showEnterURLDialog();
                }
            });
        }
        return myEnterURLButton;
    }

    /**
     * Gets the adds the file split button.
     *
     * @return the adds the file split button
     */
    private SplitButton getImportFileButton()
    {
        if (myImportFileButton == null)
        {
            myImportFileButton = new SplitButton("Import File", null);
            IconUtil.setIcons(myImportFileButton, IconType.PLUS, Color.GREEN);
            myImportFileButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    addFileInitiated();
                }
            });
        }
        return myImportFileButton;
    }

    /**
     * Gets the specific URL import menu.
     *
     * @return the specific url importer menu
     */
    private JMenu getSpecificURLImporterMenu()
    {
        if (mySpecificURLImporterMenu == null)
        {
            mySpecificURLImporterMenu = new JMenu("Import Specific URL Type");
        }
        return mySpecificURLImporterMenu;
    }

    /**
     * Show enter url dialog.
     */
    private void showEnterURLDialog()
    {
        myController.importURL();
    }
}
