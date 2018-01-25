package io.opensphere.controlpanels.layers.importdata;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.FileOrURLImporterMenuItem;
import io.opensphere.core.importer.ImportType;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.util.collections.New;

/**
 * The Class ImportButtonMenuProvider.
 */
public class ImportButtonMenuProvider implements ActionListener
{
    /** The add file group menu item. */
    private transient JMenuItem myAddFileGroupMenuItem;

    /** The add file menu item. */
    private transient JMenuItem myAddFileMenuItem;

    /** The add url menu item. */
    private transient JMenuItem myAddURLMenuItem;

    /** The controller. */
    private final transient ImportDataController myController;

    /** The Delete context menu provider. */
    private final ContextMenuProvider<Void> myImportContextMenuProvider = new ContextMenuProvider<Void>()
    {
        @Override
        public List<Component> getMenuItems(String contextId, Void key)
        {
            return buildImportMenuOptions();
        }

        @Override
        public int getPriority()
        {
            return 0;
        }
    };

    /** The toolbox. */
    private final Toolbox myToolbox;
    /** The ImportRegistry. */
    private ImporterRegistry impReg;

    /**
     * Instantiates a new import button panel.
     *
     * @param tb the {@link Toolbox}
     */
    public ImportButtonMenuProvider(Toolbox tb)
    {
        super();
        myToolbox = tb;
        impReg = myToolbox.getImporterRegistry();
        myController = ImportDataController.getInstance(tb);
        tb.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.IMPORT_CONTEXT,
                Void.class, myImportContextMenuProvider);
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

    /**
     * Closes the provider.
     */
    public void close()
    {
        myToolbox.getUIRegistry().getContextActionManager().deregisterContextMenuItemProvider(ContextIdentifiers.IMPORT_CONTEXT,
                Void.class, myImportContextMenuProvider);
    }

    /**
     * Builds the file import menu options.
     *
     * @return the list
     */
    private List<Component> buildImportMenuOptions()
    {
        List<Component> menuList = New.list();

        JMenu specificFileTypeMenu = new JMenu("Import Specific File Type");
        JMenu specificFileGroupTypeMenu = new JMenu("Import Specific File Group Type");
        JMenu specificURLTypeMenu = new JMenu("Import Specific URL Type");

        boolean hasMultiFileImporter = false;
        for (FileOrURLImporter importer : impReg.getFileImporters())
        {
            if (importer.importsFiles())
            {
                FileOrURLImporterMenuItem menuItem = new FileOrURLImporterMenuItem(importer, ImportType.FILE);
                menuItem.addActionListener(this);
                specificFileTypeMenu.add(menuItem);
            }
            if (importer.importsFileGroups())
            {
                FileOrURLImporterMenuItem menuItem = new FileOrURLImporterMenuItem(importer, ImportType.FILE_GROUP);
                menuItem.addActionListener(this);
                specificFileGroupTypeMenu.add(menuItem);
                hasMultiFileImporter = true;
            }
        }

        boolean hasSpecificURLTypeImporter = false;
        for (FileOrURLImporter importer : impReg.getUrlImporters())
        {
            FileOrURLImporterMenuItem menuItem = new FileOrURLImporterMenuItem(importer, ImportType.URL);
            menuItem.addActionListener(this);
            specificURLTypeMenu.add(menuItem);
            hasSpecificURLTypeImporter = true;
        }

        menuList.add(getAddFileMenuItem());
        if (hasMultiFileImporter)
        {
            menuList.add(getAddFileGroupMenuItem());
        }
        menuList.add(getAddURLMenuItem());

        menuList.add(new JSeparator());
        menuList.add(specificFileTypeMenu);
        if (hasMultiFileImporter)
        {
            menuList.add(specificFileGroupTypeMenu);
        }
        if (hasSpecificURLTypeImporter)
        {
            menuList.add(specificURLTypeMenu);
        }
        return menuList;
    }

    /**
     * Gets the adds the file group menu item.
     *
     * @return the adds the file group menu item
     */
    private JMenuItem getAddFileGroupMenuItem()
    {
        if (myAddFileGroupMenuItem == null)
        {
            myAddFileGroupMenuItem = new JMenuItem("Import File Group...");
            myAddFileGroupMenuItem.addActionListener(e -> myController.importFileGroup());
        }
        return myAddFileGroupMenuItem;
    }

    /**
     * Gets the adds the file menu item.
     *
     * @return the adds the file menu item
     */
    private JMenuItem getAddFileMenuItem()
    {
        if (myAddFileMenuItem == null)
        {
            myAddFileMenuItem = new JMenuItem("Import File...");
            myAddFileMenuItem.addActionListener(e -> myController.importFile());
        }
        return myAddFileMenuItem;
    }

    /**
     * Gets the adds the url menu item.
     *
     * @return the adds the url menu item
     */
    private JMenuItem getAddURLMenuItem()
    {
        if (myAddURLMenuItem == null)
        {
            myAddURLMenuItem = new JMenuItem("Import URL...");
            myAddURLMenuItem.addActionListener(e -> myController.importURL());
        }
        return myAddURLMenuItem;
    }
}
