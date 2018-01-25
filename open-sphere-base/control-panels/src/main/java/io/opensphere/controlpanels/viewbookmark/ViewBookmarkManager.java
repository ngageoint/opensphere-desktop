package io.opensphere.controlpanels.viewbookmark;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.opensphere.controlpanels.viewbookmark.controller.ViewBookmarkController;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.ToStringProxy;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.ViewBookmarkRegistryListener;

/**
 * The Class ViewBookmarkManagerPanel. The panel that will manage user views.
 */
final class ViewBookmarkManager extends AbstractInternalFrame implements ViewBookmarkRegistryListener
{
    /** Serial. */
    private static final long serialVersionUID = 1L;

    /** The title of the window. */
    private static final String TITLE = "Delete Saved Look Angles";

    /** The cancel button. */
    private JButton myCancelButton;

    /** The container panel. */
    private AbstractHUDPanel myContainerPanel;

    /** The Controller. */
    private final ViewBookmarkController myController;

    /** The save button. */
    private JButton myRemoveButton;

    /** The Selected views. */
    private final List<ViewBookmarkProxy> mySelectedViews = New.list();

    /** The View bookmark panel. */
    private JPanel myViewBookmarkPanel;

    /** The View list. */
    private JList<ViewBookmarkProxy> myViewList;

    /** The Available roi list model. */
    private DefaultListModel<ViewBookmarkProxy> myViewListModel;

    /** The View pane. */
    private JScrollPane myViewPane;

    /**
     * Instantiates a new view bookmark manager panel.
     *
     * @param controller the toolbox
     */
    public ViewBookmarkManager(ViewBookmarkController controller)
    {
        super();
        setPopable(false);
        setSize(250, 350);
        setPreferredSize(getSize());
        setMinimumSize(getSize());
        setTitle(TITLE);
        setOpaque(false);
        setIconifiable(false);
        setClosable(true);
        setResizable(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        myController = controller;
        myController.addListener(this);
        populateViewList();
        setContentPane(getContainerPanel());
    }

    /**
     * Gets the cancel button.
     *
     * @return the cancel button
     */
    public JButton getCancelButton()
    {
        if (myCancelButton == null)
        {
            myCancelButton = new JButton("Close");
            myCancelButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myCancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ViewBookmarkManager.this.setVisible(false);
                }
            });
        }
        return myCancelButton;
    }

    /**
     * Gets the removes the button.
     *
     * @return the removes the button
     */
    public JButton getRemoveButton()
    {
        if (myRemoveButton == null)
        {
            myRemoveButton = new JButton("Delete Selected");
            myRemoveButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myRemoveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (!mySelectedViews.isEmpty())
                    {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Are you sure you want to delete\nthe following saved look angles?\n\n");
                        for (ViewBookmarkProxy view : mySelectedViews)
                        {
                            sb.append(view.getItem().getViewName());
                            sb.append('\n');
                        }

                        int choice = JOptionPane.showConfirmDialog(ViewBookmarkManager.this, sb.toString(), "Delete Look Angles",
                                JOptionPane.ERROR_MESSAGE);

                        if (choice == JOptionPane.OK_OPTION)
                        {
                            List<ViewBookmarkProxy> selectedViews = New.list(mySelectedViews);
                            for (ViewBookmarkProxy view : selectedViews)
                            {
                                myController.removeViewerPosition(myController.getBookmarkByName(view.getItem().getViewName()));
                            }
                            populateViewList();
                        }
                        else
                        {
                            mySelectedViews.clear();
                            getViewList().clearSelection();
                        }
                    }
                }
            });
        }
        return myRemoveButton;
    }

    /**
     * Gets the view list.
     *
     * @return the view list
     */
    public JList<ViewBookmarkProxy> getViewList()
    {
        if (myViewList == null)
        {
            myViewList = new JList<>(getViewListModel());
            myViewList.setOpaque(false);
            myViewList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            myViewList.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent evt)
                {
                    if (!evt.getValueIsAdjusting())
                    {
                        mySelectedViews.clear();
                        for (ViewBookmarkProxy view : myViewList.getSelectedValuesList())
                        {
                            mySelectedViews.add(view);
                        }
                    }
                }
            });
        }
        return myViewList;
    }

    /**
     * Gets the view list model.
     *
     * @return the view list model
     */
    public DefaultListModel<ViewBookmarkProxy> getViewListModel()
    {
        if (myViewListModel == null)
        {
            myViewListModel = new DefaultListModel<>();
        }
        return myViewListModel;
    }

    @Override
    public void viewBookmarkAdded(ViewBookmark view, Object source)
    {
        populateViewList();
    }

    @Override
    public void viewBookmarkRemoved(ViewBookmark view, Object source)
    {
        populateViewList();
    }

    /**
     * Gets the container panel.
     *
     * @return the container panel
     */
    private AbstractHUDPanel getContainerPanel()
    {
        if (myContainerPanel == null)
        {
            myContainerPanel = new AbstractHUDPanel(myController.getToolbox().getPreferencesRegistry());
            myContainerPanel.setLayout(new GridBagLayout());
            myContainerPanel.setBackground(myContainerPanel.getBackgroundColor());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            myContainerPanel.add(getViewBookmarkPanel(), gbc);

            gbc.fill = GridBagConstraints.NONE;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.insets = new Insets(10, 20, 10, 0);
            gbc.weightx = 0;
            gbc.weighty = 0;
            myContainerPanel.add(getRemoveButton(), gbc);

            gbc.gridx = 1;
            gbc.insets = new Insets(10, 10, 10, 0);
            myContainerPanel.add(getCancelButton(), gbc);
        }
        return myContainerPanel;
    }

    /**
     * Gets the view bookmark panel.
     *
     * @return the view bookmark panel
     */
    private JPanel getViewBookmarkPanel()
    {
        if (myViewBookmarkPanel == null)
        {
            myViewBookmarkPanel = new JPanel(new BorderLayout());
            myViewBookmarkPanel.setOpaque(false);
            myViewBookmarkPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
            myViewBookmarkPanel.setSize(250, 200);
            myViewBookmarkPanel.setMinimumSize(myViewBookmarkPanel.getSize());
            myViewBookmarkPanel.setPreferredSize(myViewBookmarkPanel.getSize());
            myViewBookmarkPanel.add(getViewPane(), BorderLayout.CENTER);
        }
        return myViewBookmarkPanel;
    }

    /**
     * Gets the view pane.
     *
     * @return the view pane
     */
    private JScrollPane getViewPane()
    {
        if (myViewPane == null)
        {
            myViewPane = AbstractHUDPanel.getJScrollPane(getViewList());
            myViewPane.setBorder(BorderFactory.createLineBorder(getContainerPanel().getBorderColor(), 1));
        }
        return myViewPane;
    }

    /**
     * Populate view list.
     */
    private void populateViewList()
    {
        getViewListModel().removeAllElements();
        for (ViewBookmark vbm : myController.getBookmarks())
        {
            getViewListModel().addElement(new ViewBookmarkProxy(vbm));
        }
    }

    /**
     * The Class ViewBookmarkProxy.
     */
    private static class ViewBookmarkProxy extends ToStringProxy<ViewBookmark>
    {
        /**
         * Instantiates a new view bookmark proxy.
         *
         * @param itemToProxy the item to proxy
         */
        public ViewBookmarkProxy(ViewBookmark itemToProxy)
        {
            super(itemToProxy);
        }

        @Override
        public String toString()
        {
            return getItem() == null ? "NULL" : (getItem().is3D() ? "3D: " : "2D: ") + getItem().getViewName();
        }
    }
}
