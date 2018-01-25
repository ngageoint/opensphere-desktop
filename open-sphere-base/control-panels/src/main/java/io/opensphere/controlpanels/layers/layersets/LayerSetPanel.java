package io.opensphere.controlpanels.layers.layersets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.opensphere.controlpanels.layers.layersets.LayerSetController.LayerSetControllerListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.preferences.PreferenceChangeEvent;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.HighlightedBorder;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.ToStringProxy;
import io.opensphere.mantle.data.ActiveGroupEntry;

/**
 * The panel used for managing layer sets.
 */
@SuppressWarnings("PMD.GodClass")
public class LayerSetPanel extends AbstractHUDPanel implements LayerSetControllerListener
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The activate button. */
    private JButton myActivateButton;

    /** The add button. */
    private IconButton myAddButton;

    /** The close button. */
    private JButton myCloseButton;

    /** The controller. */
    private final transient LayerSetController myController;

    /** The delete button. */
    private IconButton myDeleteButton;

    /** The frame. */
    private final transient LayerSetFrame myFrame;

    /** The layer list. */
    private JList<EntryProxy> myLayerList;

    /** The layer list scroll pane. */
    private JScrollPane myLayerListScrollPane;

    /** The layer panel. */
    private Box myLayerPanel;

    /** The layer set list. */
    private JList<String> myLayerSetList;

    /** The layer set panel. */
    private Box myLayerSetPanel;

    /** The layer set scroll pane. */
    private JScrollPane myLayerSetScrollPane;

    /** The remove layer buton. */
    private IconButton myRemoveLayerButon;

    /** The rename button. */
    private JButton myRenameButton;

    /** The toolbox. */
    private final transient Toolbox myToolbox;

    /**
     * Instantiates a new layer set panel.
     *
     * @param tb the {@link Toolbox}
     * @param frame the frame
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public LayerSetPanel(Toolbox tb, LayerSetFrame frame)
    {
        super();
        myToolbox = tb;
        myFrame = frame;
        myController = new LayerSetController(myToolbox);

        setBackground(new Color(myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, new JPanel().getBackground().getRGB()), true));

        myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .addPreferenceChangeListener(AbstractHUDPanel.ourHUDBackgroundColorKey, this);

        setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        myController.addListener(this);
        setLayout(new BorderLayout());

        Box topBox = Box.createHorizontalBox();
        topBox.add(getLayerSetPanel());
        topBox.add(Box.createHorizontalStrut(3));
        topBox.add(getLayerPanel());

        add(topBox, BorderLayout.CENTER);
        Box b = Box.createHorizontalBox();
        b.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        b.setMinimumSize(new Dimension(100, 40));
        b.add(Box.createHorizontalGlue());
        b.add(getCloseButton());
        b.add(Box.createHorizontalGlue());
        add(b, BorderLayout.SOUTH);

        layerSetsChanged();
        getRenameLayerSetButton().setEnabled(false);
        getActivateButton().setEnabled(false);
        getDeleteLayerSetButton().setEnabled(false);
        getRemoveLayerButton().setEnabled(false);
    }

    /**
     * Gets the activate button.
     *
     * @return the activate button
     */
    public JButton getActivateButton()
    {
        if (myActivateButton == null)
        {
            myActivateButton = new IconButton("Activate");
            myActivateButton.setToolTipText("Activate the layers in the selected layer set.");
            myActivateButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    List<String> values = getLayerSetList().getSelectedValuesList();
                    if (values.size() == 1)
                    {
                        myController.activateLayerSet(values.get(0), true);
                    }
                }
            });
        }
        return myActivateButton;
    }

    /**
     * Gets the adds the layer set button.
     *
     * @return the adds the layer set button
     */
    public JButton getAddLayerSetButton()
    {
        if (myAddButton == null)
        {
            myAddButton = new IconButton(IconType.PLUS);
            myAddButton.setToolTipText("Create a new layer set with the currently active layers.");
            myAddButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myController.saveCurrentSet();
                }
            });
        }
        return myAddButton;
    }

    /**
     * Gets the close button.
     *
     * @return the close button
     */
    public JButton getCloseButton()
    {
        if (myCloseButton == null)
        {
            myCloseButton = new JButton("Close");
            myCloseButton.setMargin(ButtonPanel.INSETS_MEDIUM);
            myCloseButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myFrame.setFrameVisible(false);
                }
            });
        }
        return myCloseButton;
    }

    /**
     * Gets the delete layer set button.
     *
     * @return the delete layer set button
     */
    public JButton getDeleteLayerSetButton()
    {
        if (myDeleteButton == null)
        {
            myDeleteButton = new IconButton();
            myDeleteButton.setToolTipText("Delete the selected layer set(s)");
            myDeleteButton.setIcon("/images/minus_big.png");
            myDeleteButton.setRolloverIcon("/images/minus_big_over.png");
            myDeleteButton.setPressedIcon("/images/minus_big_press.png");
            myDeleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    List<String> values = getLayerSetList().getSelectedValuesList();
                    if (!values.isEmpty())
                    {
                        for (String setName : values)
                        {
                            myController.deleteSet(setName);
                        }
                    }
                }
            });
        }
        return myDeleteButton;
    }

    /**
     * Gets the layer list.
     *
     * @return the layer list
     */
    public JList<EntryProxy> getLayerList()
    {
        if (myLayerList == null)
        {
            myLayerList = new JList<>();
            myLayerList.setBackground(new Color(0, 0, 0, 0));
            myLayerList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        int[] selectedIndexes = myLayerList.getSelectedIndices();
                        int count = selectedIndexes.length;
                        getRemoveLayerButton().setEnabled(count >= 1);
                    }
                }
            });
        }
        return myLayerList;
    }

    /**
     * Gets the layer list scroll pane.
     *
     * @return the layer list scroll pane
     */
    public JScrollPane getLayerListScrollPane()
    {
        if (myLayerListScrollPane == null)
        {
            myLayerListScrollPane = new JScrollPane(getLayerList());
            myLayerListScrollPane.setBackground(new Color(0, 0, 0, 0));
        }
        return myLayerListScrollPane;
    }

    /**
     * Gets the layer panel.
     *
     * @return the layer panel
     */
    public Box getLayerPanel()
    {
        if (myLayerPanel == null)
        {
            myLayerPanel = Box.createVerticalBox();
            HighlightedBorder hb = new HighlightedBorder(BorderFactory.createLineBorder(getBorderColor(), 1), "Layers",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, getTitleFont(),
                    new JLabel().getForeground(), getBorderHighlightColor());
            hb.setWidthOffset(3);
            hb.setXOffset(-1);
            myLayerPanel.setBorder(hb);

            myLayerPanel.setPreferredSize(new Dimension(0, 250));
            myLayerPanel.setMinimumSize(new Dimension(0, 250));
            myLayerPanel.add(Box.createVerticalStrut(3));
            myLayerPanel.add(getLayerListScrollPane());
            myLayerPanel.add(Box.createVerticalStrut(3));
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getRemoveLayerButton());
            buttonBox.add(Box.createHorizontalGlue());

            myLayerPanel.add(buttonBox);
        }
        return myLayerPanel;
    }

    /**
     * Gets the layer set list.
     *
     * @return the layer set list
     */
    public JList<String> getLayerSetList()
    {
        if (myLayerSetList == null)
        {
            myLayerSetList = new JList<>();
            myLayerSetList.setBackground(new Color(0, 0, 0, 0));
            myLayerSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        int[] selectedIndexes = myLayerSetList.getSelectedIndices();
                        refreshLayerList();
                        int count = selectedIndexes.length;
                        getRenameLayerSetButton().setEnabled(count == 1);
                        getActivateButton().setEnabled(count == 1);
                        getDeleteLayerSetButton().setEnabled(count >= 1);
                    }
                }
            });
            myLayerSetList.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
                    {
                        List<String> values = getLayerSetList().getSelectedValuesList();
                        if (values.size() == 1)
                        {
                            myController.activateLayerSet(values.get(0), true);
                        }
                    }
                }
            });
        }
        return myLayerSetList;
    }

    /**
     * Gets the layer set panel.
     *
     * @return the layer set panel
     */
    public Box getLayerSetPanel()
    {
        if (myLayerSetPanel == null)
        {
            myLayerSetPanel = Box.createVerticalBox();
            HighlightedBorder hb = new HighlightedBorder(BorderFactory.createLineBorder(getBorderColor(), 1), "Layer Sets",
                    TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, getTitleFont(),
                    new JLabel().getForeground(), getBorderHighlightColor());
            hb.setWidthOffset(3);
            hb.setXOffset(-1);
            myLayerSetPanel.setBorder(hb);

            myLayerSetPanel.setMinimumSize(new Dimension(0, 250));
            myLayerSetPanel.setPreferredSize(new Dimension(0, 250));
            myLayerSetPanel.add(Box.createVerticalStrut(3));
            myLayerSetPanel.add(getLayerSetScrollPane());
            myLayerSetPanel.add(Box.createVerticalStrut(3));
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getAddLayerSetButton());
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getDeleteLayerSetButton());
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getRenameLayerSetButton());
            buttonBox.add(Box.createHorizontalStrut(4));
            buttonBox.add(getActivateButton());
            buttonBox.add(Box.createHorizontalGlue());
            myLayerSetPanel.add(buttonBox);
        }
        return myLayerSetPanel;
    }

    /**
     * Gets the layer set scroll pane.
     *
     * @return the layer set scroll pane
     */
    public JScrollPane getLayerSetScrollPane()
    {
        if (myLayerSetScrollPane == null)
        {
            myLayerSetScrollPane = new JScrollPane(getLayerSetList());
            myLayerSetScrollPane.setBackground(new Color(0, 0, 0, 0));
        }
        return myLayerSetScrollPane;
    }

    /**
     * Gets the removes the layer button.
     *
     * @return the removes the layer button
     */
    public JButton getRemoveLayerButton()
    {
        if (myRemoveLayerButon == null)
        {
            myRemoveLayerButon = new IconButton();
            myRemoveLayerButon.setToolTipText("Remove the selected layers from the current layer set.");
            myRemoveLayerButon.setIcon("/images/minus_big.png");
            myRemoveLayerButon.setRolloverIcon("/images/minus_big_over.png");
            myRemoveLayerButon.setPressedIcon("/images/minus_big_press.png");
            myRemoveLayerButon.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    List<String> layerSets = getLayerSetList().getSelectedValuesList();
                    if (layerSets.size() == 1)
                    {
                        List<EntryProxy> layers = getLayerList().getSelectedValuesList();
                        if (!layers.isEmpty())
                        {
                            Set<ActiveGroupEntry> entrySet = New.set();
                            for (EntryProxy obj : layers)
                            {
                                entrySet.add(obj.getItem());
                            }
                            myController.deleteLayersFromSet(layerSets.get(0), entrySet);
                        }
                    }
                }
            });
        }
        return myRemoveLayerButon;
    }

    /**
     * My rename layer set button.
     *
     * @return the j button
     */
    public JButton getRenameLayerSetButton()
    {
        if (myRenameButton == null)
        {
            myRenameButton = new IconButton("Rename");
            myRenameButton.setToolTipText("Rename the selected layer set.");
            myRenameButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    List<String> values = getLayerSetList().getSelectedValuesList();
                    if (values.size() == 1)
                    {
                        myController.renameSet(values.get(0));
                    }
                }
            });
        }
        return myRenameButton;
    }

    @Override
    public void layerSetsChanged()
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                String lastSelectedLayerSet = null;
                List<String> values = getLayerSetList().getSelectedValuesList();
                if (values.size() == 1)
                {
                    lastSelectedLayerSet = values.get(0);
                }

                List<String> setNames = myController.getSavedSetNames();
                DefaultListModel<String> model = new DefaultListModel<>();
                if (setNames != null && !setNames.isEmpty())
                {
                    model.ensureCapacity(setNames.size());
                    for (String name : setNames)
                    {
                        model.addElement(name);
                    }
                }
                getLayerSetList().setModel(model);

                if (lastSelectedLayerSet != null)
                {
                    getLayerSetList().setSelectedValue(lastSelectedLayerSet, true);
                }
                else
                {
                    refreshLayerList();
                }
            }
        });
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt)
    {
        setBackground(new Color(myToolbox.getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, new JPanel().getBackground().getRGB()), true));
    }

    /**
     * Refresh layer list.
     */
    private void refreshLayerList()
    {
        List<String> values = getLayerSetList().getSelectedValuesList();
        DefaultListModel<EntryProxy> model = new DefaultListModel<>();
        if (values.size() == 1)
        {
            myController.getAvailableGroupIds();
            List<ActiveGroupEntry> layers = myController.getSavedSetLayers(values.get(0));

            if (layers != null && !layers.isEmpty())
            {
                model.ensureCapacity(layers.size());
                for (ActiveGroupEntry entry : layers)
                {
                    model.addElement(new EntryProxy(entry));
                }
            }
        }
        getLayerList().setModel(model);
        getRemoveLayerButton().setEnabled(false);
    }

    /**
     * The Class EntryProxy.
     */
    public static class EntryProxy extends ToStringProxy<ActiveGroupEntry>
    {
        /**
         * Instantiates a new entry proxy.
         *
         * @param entry the entry
         */
        public EntryProxy(ActiveGroupEntry entry)
        {
            super(entry);
        }

        @Override
        public String toString()
        {
            return getItem().getName();
        }
    }
}
