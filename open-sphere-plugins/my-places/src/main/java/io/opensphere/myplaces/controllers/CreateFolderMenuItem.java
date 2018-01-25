package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.util.GroupUtils;

/**
 * Create folder menu item.
 */
public class CreateFolderMenuItem extends JMenuItem
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param dataGroup the selected data group
     */
    public CreateFolderMenuItem(final Toolbox toolbox, final MyPlacesDataGroupInfo dataGroup)
    {
        super("Create Folder...");
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                Component parent = toolbox.getUIRegistry().getMainFrameProvider().get();
                String folderName = JOptionPane.showInputDialog(parent, "Name", "New Folder", JOptionPane.QUESTION_MESSAGE);
                if (StringUtils.isNotEmpty(folderName))
                {
                    GroupUtils.createAndAddGroup(folderName, dataGroup, toolbox, CreateFolderMenuItem.this);
                }
            }
        });
    }
}
