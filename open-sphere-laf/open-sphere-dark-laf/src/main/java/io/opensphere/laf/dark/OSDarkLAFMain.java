package io.opensphere.laf.dark;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class OSDarkLAFMain extends JFrame
{
    public static OpenSphereDarkLookAndFeel nf;

    public static OSDarkLAFTheme nt;

    private JPanel bP1;

    private JPanel bP2;

    private JPanel bP3;

    private JPanel bS1;

    private JPanel bS2;

    private JPanel bS3;

    private JPanel bB;

    private JPanel bW;

    private JPanel bSelection;

    private JPanel bBackground;

    private JButton bOpen;

    private JButton bSave;

    private JButton bPrueba;

    private JTextField tNomFich;

    private JSlider sMenuOpacidad;

    private JSlider sFrameOpacidad;

    private JProgressBar pb3;

    private JProgressBar pb4;

    private JSpinner sp;

    private JToolBar toolBar;

    private JDesktopPane desktop;

    private JPanel pConfig;

    private JPanel pView1;

    private JPanel pView2;

    private JPanel pView3;

    private JPanel pView4;

    private JPanel pView5;

    private final JMenuBar menuBar;

    private final JTabbedPane tabPan;

    private static int pos;

    OSDarkLAFMain()
    {
        super("OpenSphere Dark Look&Feel Sample");

        menuBar = new JMenuBar();
        hazMenuBar();
        setJMenuBar(menuBar);

        hazConfig();
        hazPreview1();
        hazPreview2();
        hazPreview3();
        hazPreview4();
        hazPreview5();
        hazToolBar();

        tabPan = new JTabbedPane();
        tabPan.add("Config", pConfig);
        tabPan.add("Preview 1", pView1);
        tabPan.add("Preview 2", pView2);
        tabPan.add("Preview 3", pView3);
        tabPan.add("Preview 4", pView4);
        tabPan.add("Preview 5", pView5);

        getContentPane().add(toolBar, BorderLayout.PAGE_START);
        getContentPane().add(tabPan, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(450, 490);
        setVisible(true);
    }

    private void hazToolBar()
    {
        toolBar = new JToolBar("ToolBar, you know...");

        final JButton b1 = new JButton(UIManager.getIcon("FileView.floppyDriveIcon"));
        b1.setToolTipText("Floopy Drive");
        final JButton b2 = new JButton(UIManager.getIcon("Tree.closedIcon"));
        b2.setToolTipText("Close");
        final JToggleButton b3 = new JToggleButton(UIManager.getIcon("Tree.openIcon"));
        b3.setToolTipText("Open");

        toolBar.add(b1);
        toolBar.add(b2);
        toolBar.addSeparator();
        toolBar.add(b3);
    }

    private void hazMenuBar()
    {
        final JMenu menuTabs = new JMenu("Tabs");
        menuBar.add(menuTabs);

        JMenuItem menuItem = new JMenuItem("Top");
        menuItem.addActionListener(new MiTL());
        menuTabs.add(menuItem);

        menuItem = new JMenuItem("Bottom");
        menuItem.addActionListener(new MiTL());
        menuTabs.add(menuItem);

        menuItem = new JMenuItem("Left");
        menuItem.addActionListener(new MiTL());
        menuTabs.add(menuItem);

        menuItem = new JMenuItem("Right");
        menuItem.addActionListener(new MiTL());
        menuTabs.add(menuItem);

        final JMenu menuOtro = new JMenu("Menu");
        menuBar.add(menuOtro);

        menuItem = new JMenuItem("One item");
        menuItem.setMnemonic(KeyEvent.VK_O);
        menuOtro.add(menuItem);

        menuItem = new JMenuItem("Not enabled");
        menuItem.setMnemonic(KeyEvent.VK_E);
        menuItem.setEnabled(false);
        menuOtro.add(menuItem);

        menuItem = new JMenuItem("Other item");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
        menuOtro.add(menuItem);

        menuItem = new JMenuItem("Other Not enabled");
        menuItem.setEnabled(false);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
        menuOtro.add(menuItem);

        final ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem rbMi = new JRadioButtonMenuItem("Cats", true);
        rbMi.setMnemonic(KeyEvent.VK_G);
        group.add(rbMi);
        menuOtro.add(rbMi);

        rbMi = new JRadioButtonMenuItem("Dogs");
        rbMi.setMnemonic(KeyEvent.VK_P);
        rbMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        group.add(rbMi);
        menuOtro.add(rbMi);

        rbMi = new JRadioButtonMenuItem("Birds", true);
        rbMi.setEnabled(false);
        rbMi.setMnemonic(KeyEvent.VK_E);
        group.add(rbMi);
        menuOtro.add(rbMi);

        rbMi = new JRadioButtonMenuItem("Elephants");
        rbMi.setEnabled(false);
        rbMi.setMnemonic(KeyEvent.VK_V);
        rbMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        group.add(rbMi);
        menuOtro.add(rbMi);

        menuOtro.addSeparator();

        JCheckBoxMenuItem cbMi = new JCheckBoxMenuItem("Eat", true);
        cbMi.setMnemonic(KeyEvent.VK_C);
        menuOtro.add(cbMi);

        cbMi = new JCheckBoxMenuItem("Drink", false);
        cbMi.setMnemonic(KeyEvent.VK_B);
        cbMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuOtro.add(cbMi);

        cbMi = new JCheckBoxMenuItem("Walk", true);
        cbMi.setEnabled(false);
        cbMi.setMnemonic(KeyEvent.VK_R);
        menuOtro.add(cbMi);

        cbMi = new JCheckBoxMenuItem("Look", false);
        cbMi.setEnabled(false);
        cbMi.setMnemonic(KeyEvent.VK_M);
        cbMi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        menuOtro.add(cbMi);

        menuOtro.addSeparator();

        final JMenu submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);

        menuItem = new JMenuItem("Not enabled");
        menuItem.setEnabled(false);
        submenu.add(menuItem);

        menuItem = new JMenuItem("Not enabled, too");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
        menuItem.setEnabled(false);
        submenu.add(menuItem);

        menuOtro.add(submenu);

        final JMenu masMenus = new JMenu("Not enabled");
        masMenus.setEnabled(false);

        menuBar.add(masMenus);
    }

    private void hazConfig()
    {
        // Para abrir y guardar
        tNomFich = new JTextField(20);
        tNomFich.setEditable(false);

        bOpen = new JButton("Open");
        bOpen.addActionListener(new MiAL());
        bOpen.setToolTipText("Open theme files");
        bSave = new JButton("Save");
        bSave.setToolTipText("Save theme files");
        bSave.addActionListener(new MiAL());

        final JPanel pAlto = new JPanel(new FlowLayout());
        pAlto.add(tNomFich);
        pAlto.add(bOpen);
        pAlto.add(bSave);

        final MiML ml = new MiML();
        // Para los colores de seleccion
        bSelection = hazPanel(ml);
        bP1 = hazPanel(ml);
        bP2 = hazPanel(ml);
        bP3 = hazPanel(ml);

        final JPanel pSel = new JPanel(new GridLayout(1, 5, 3, 3));
        pSel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Selection"));
        pSel.add(bSelection);
        pSel.add(new JLabel());
        pSel.add(bP1);
        pSel.add(bP2);
        pSel.add(bP3);

        // Para los colores de fondo
        bBackground = hazPanel(ml);
        bS1 = hazPanel(ml);
        bS2 = hazPanel(ml);
        bS3 = hazPanel(ml);

        final JPanel pFon = new JPanel(new GridLayout(1, 5, 3, 3));
        pFon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Background"));
        pFon.add(bBackground);
        pFon.add(new JLabel());
        pFon.add(bS1);
        pFon.add(bS2);
        pFon.add(bS3);

        bB = hazPanel(ml);
        bW = hazPanel(ml);

        final JPanel pBW = new JPanel(new GridLayout(1, 2, 3, 3));
        pBW.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Black & White"));
        pBW.add(bB);
        pBW.add(bW);

        bPrueba = new JButton("Test");
        bPrueba.setToolTipText("Test current selection");
        bPrueba.addActionListener(new MiAL());

        final JPanel pMenuOp = new JPanel();
        pMenuOp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Menu Opacity"));
        sMenuOpacidad = new JSlider(SwingConstants.HORIZONTAL, 0, 255, nt.getMenuOpacity());
        pMenuOp.add(sMenuOpacidad);

        final JPanel pFrameOp = new JPanel();
        pFrameOp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Internal Frame Opacity"));
        sFrameOpacidad = new JSlider(SwingConstants.HORIZONTAL, 0, 255, nt.getFrameOpacity());
        pFrameOp.add(sFrameOpacidad);

        hazPaleta();

        pConfig = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        pConfig.add(pAlto, c);

        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 1;
        pConfig.add(pSel, c);
        c.gridx = 1;
        c.gridy = 1;
        pConfig.add(pBW, c);

        c.gridx = 0;
        c.gridy = 2;
        pConfig.add(pFon, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 2;
        pConfig.add(bPrueba, c);

        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 3;
        pConfig.add(pMenuOp, c);

        c.gridy = 4;
        pConfig.add(pFrameOp, c);
    }

    private JPanel hazPanel(MouseAdapter ml)
    {
        final JPanel pan = new JPanel();

        pan.setPreferredSize(new Dimension(40, 40));
        pan.addMouseListener(ml);
        pan.setBorder(BorderFactory.createEtchedBorder());

        return pan;
    }

    private void hazPaleta()
    {
        bSelection.setBackground(nt.getPrimary3());
        bP1.setBackground(nt.getPrimary1());
        bP2.setBackground(nt.getPrimary2());
        bP3.setBackground(nt.getPrimary3());

        bBackground.setBackground(nt.getSecondary3());
        bS1.setBackground(nt.getSecondary1());
        bS2.setBackground(nt.getSecondary2());
        bS3.setBackground(nt.getSecondary3());

        bB.setBackground(nt.getBlack());
        bW.setBackground(nt.getWhite());

        sMenuOpacidad.setValue(nt.getMenuOpacity());
        sFrameOpacidad.setValue(nt.getFrameOpacity());
    }

    private void hazPreview1()
    {
        final JPanel fondo = new JPanel();
        fondo.setLayout(new BoxLayout(fondo, BoxLayout.X_AXIS));

        final JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        p1.add(new JCheckBox("Checkbox 1"));
        final JCheckBox cbi = new JCheckBox("Checkbox 2");
        cbi.setEnabled(false);
        p1.add(cbi);

        p1.add(new JRadioButton("Radio 1"));
        final JRadioButton rbi = new JRadioButton("Radio 2");
        rbi.setEnabled(false);
        p1.add(rbi);

        p1.add(new JButton("Button 1"));
        p1.add(new JToggleButton("ToggleButton"));
        final JButton bi = new JButton("Inactive");
        bi.setEnabled(false);
        p1.add(bi);

        p1.add(Box.createVerticalGlue());

        final Vector<String> v = new Vector<>();
        for (int i = 0; i < 30; i++)
        {
            v.add("Option " + i);
        }

        final JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

        final JComboBox cb = new JComboBox(v);
        cb.setMaximumSize(new Dimension(400, cb.getPreferredSize().height));

        p2.add(cb);
        p2.add(Box.createRigidArea(new Dimension(10, 10)));

        final JComboBox cb2 = new JComboBox(v);
        cb2.setMaximumSize(new Dimension(400, cb.getPreferredSize().height));
        cb2.setEditable(true);

        p2.add(cb2);
        p2.add(Box.createRigidArea(new Dimension(10, 10)));

        final JComboBox cb3 = new JComboBox();
        cb3.addItem("Option 1");
        cb3.addItem("Option 2");
        cb3.addItem("Option 3");
        cb3.addItem("Option 4");
        cb3.setMaximumRowCount(7);
        cb3.setMaximumSize(new Dimension(400, cb.getPreferredSize().height));
        cb3.setEditable(true);
        cb3.setEnabled(false);

        p2.add(cb3);
        p2.add(Box.createRigidArea(new Dimension(10, 10)));

        final JList list = new JList(v);
        final JScrollPane scrPan = new JScrollPane(list);
        scrPan.setPreferredSize(new Dimension(200, 80));
        p2.add(scrPan);

        fondo.add(p1);
        fondo.add(p2);

        final JPanel bots = new JPanel(new FlowLayout());

        JButton b = new JButton(UIManager.getIcon("OptionPane.errorIcon"));
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                JOptionPane.showMessageDialog(OSDarkLAFMain.this, "Eggs aren't supposed to be green.", "Inane warning",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        bots.add(b);

        b = new JButton(UIManager.getIcon("OptionPane.informationIcon"));
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                JOptionPane.showMessageDialog(OSDarkLAFMain.this, "Eggs aren't supposed to be green.", "Inane warning",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        bots.add(b);

        b = new JButton(UIManager.getIcon("OptionPane.warningIcon"));
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                JOptionPane.showMessageDialog(OSDarkLAFMain.this, "Eggs aren't supposed to be green.", "Inane warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        bots.add(b);

        b = new JButton(UIManager.getIcon("OptionPane.questionIcon"));
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                JOptionPane.showMessageDialog(OSDarkLAFMain.this, "Eggs aren't supposed to be green.", "Inane warning",
                        JOptionPane.QUESTION_MESSAGE);
            }
        });
        bots.add(b);

        pView1 = new JPanel(new BorderLayout());
        pView1.add(fondo, BorderLayout.CENTER);
        pView1.add(bots, BorderLayout.SOUTH);
    }

    private void hazPreview2()
    {
        pView2 = new JPanel(new BorderLayout(10, 10));

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Raiz");
        root.add(new DefaultMutableTreeNode("Alfa"));
        root.add(new DefaultMutableTreeNode("Beta"));
        root.add(new DefaultMutableTreeNode("Gamma"));

        DefaultMutableTreeNode next = new DefaultMutableTreeNode("Iota");
        root.add(next);

        next.add(new DefaultMutableTreeNode("Iota-Alfa"));
        next.add(new DefaultMutableTreeNode("Iota-Beta"));
        next.add(new DefaultMutableTreeNode("Iota-Gamma"));

        next = new DefaultMutableTreeNode("Kappa");
        root.add(next);

        next.add(new DefaultMutableTreeNode("Kappa-Alfa"));
        next.add(new DefaultMutableTreeNode("Kappa-Beta"));
        next.add(new DefaultMutableTreeNode("Kappa-Gamma"));

        next = new DefaultMutableTreeNode("Xi");
        root.add(next);

        next.add(new DefaultMutableTreeNode("Xi-Alfa"));
        next.add(new DefaultMutableTreeNode("Xi-Beta"));
        next.add(new DefaultMutableTreeNode("Xi-Gamma"));

        final DefaultMutableTreeNode renext = new DefaultMutableTreeNode("Rho");
        next.add(renext);

        renext.add(new DefaultMutableTreeNode("Rho-Alfa"));
        renext.add(new DefaultMutableTreeNode("Rho-Beta"));
        renext.add(new DefaultMutableTreeNode("Rho-Gamma"));

        final JTree arb = new JTree(root);
        final JScrollPane treeView = new JScrollPane(arb);

        final DefaultTableModel dtm = new DefaultTableModel(25, 5)
        {
            @Override
            public Class getColumnClass(int c)
            {
                if (c == 4)
                {
                    return Boolean.class;
                }
                else
                {
                    return String.class;
                }
            }
        };

        final JTable tabla = new JTable(dtm)
        {
            private static final long serialVersionUID = 1L;

            private final String[] vDa = { "Option 1", "Option 2", "Option 3" };

            @Override
            public javax.swing.table.TableCellEditor getCellEditor(int row, int column)
            {
                if (column == 1)
                {
                    return new DefaultCellEditor(new JComboBox(vDa));
                }
                else if (column == 2)
                {
                    final JComboBox cb = new JComboBox(vDa);
                    cb.setEditable(true);
                    return new DefaultCellEditor(cb);
                }
                else if (column == 3)
                {
                    return new DefaultCellEditor(new JTextField());
                }
                return super.getCellEditor(row, column);
            }
        };
        final JScrollPane scrPan = new JScrollPane(tabla);

        final JSlider sl = new JSlider(SwingConstants.VERTICAL, 10, 40, 20);
        sl.addChangeListener(new MiCL(tabla));

        pView2.add(treeView, BorderLayout.WEST);
        pView2.add(scrPan, BorderLayout.CENTER);
        pView2.add(sl, BorderLayout.EAST);
    }

    private class MiCL implements ChangeListener
    {
        JTable tabla;

        MiCL(JTable tabla)
        {
            this.tabla = tabla;
        }

        @Override
        public void stateChanged(ChangeEvent e)
        {
            final int val = ((JSlider)e.getSource()).getValue();
            tabla.setRowHeight(val);
        }
    }

    private void hazPreview3()
    {
        pView3 = new JPanel(new BorderLayout(0, 10));

        final JPanel p1 = new JPanel(new GridLayout(2, 3, 5, 1));

        p1.add(new JLabel("JTextField"));
        p1.add(new JLabel("JTextField disabled"));
        p1.add(new JLabel("JPasswordField"));

        final JTextField tf1 = new JTextField("I am not opaque", 15);
        tf1.setOpaque(false);
        p1.add(tf1);

        final JTextField tf2 = new JTextField("Disabled");
        tf2.setEnabled(false);
        p1.add(tf2);

        final JPasswordField tp1 = new JPasswordField(15);
        p1.add(tp1);

        final JTextArea text = new JTextArea("e\nasd\nasd\nads\nasd\nasdaaaaaaaaaaaaaaaaaa\n\n\n\n\n\n\n"
                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaae\n"
                + "asd\nasd\nads\nasd\nasdaaaaa\naaaaaaa\naaaaaaaaa\naaaaaaa\naaaaa\n"
                + "aaaaaa\naaaaaaa\naaaaaaaaaaaaaaa\naaaaaaaaaaaa\naaaaa");
        // text.setLineWrap( true);
        text.setWrapStyleWord(true);
        final JScrollPane scrPan = new JScrollPane(text);
        scrPan.setPreferredSize(new Dimension(300, 320));

        pView3.add(p1, BorderLayout.NORTH);
        pView3.add(scrPan, BorderLayout.CENTER);
    }

    private void hazPreview4()
    {
        pView4 = new JPanel(new BorderLayout(3, 3));

        desktop = new JDesktopPane();
        desktop.setBorder(BorderFactory.createEtchedBorder());

        final JButton bAdd = new JButton("Add InternalFrame");
        bAdd.addActionListener(new MiFL());
        pView4.add(bAdd, BorderLayout.NORTH);
        pView4.add(desktop, BorderLayout.CENTER);

        final JPanel pp = new JPanel(new BorderLayout());
        pp.add(new JLabel("If you like the icons, here they are..."), BorderLayout.NORTH);
        pp.add(new JTextField("http://www.kde-look.org/content/show.php/Alien+OSX?content=53829"), BorderLayout.CENTER);

        pView4.add(pp, BorderLayout.SOUTH);
    }

    private void hazPreview5()
    {
        final JProgressBar pb1 = new JProgressBar(0, 100);
        pb1.setValue(0);
        pb1.setString("Doing something...");
        pb1.setStringPainted(true);
        pb1.setIndeterminate(true);

        final JProgressBar pb2 = new JProgressBar(0, 100);
        pb2.setValue(0);
        pb2.setString("Doing something too...");
        pb2.setStringPainted(true);
        pb2.setIndeterminate(true);
        pb2.setOrientation(JProgressBar.VERTICAL);

        pb3 = new JProgressBar(0, 100);
        pb3.setValue(50);
        pb3.setStringPainted(true);

        pb4 = new JProgressBar(0, 100);
        pb4.setValue(50);
        pb4.setStringPainted(true);
        pb4.setOrientation(JProgressBar.VERTICAL);

        final SpinnerModel spModel = new SpinnerNumberModel(50, 0, 100, 1);
        sp = new JSpinner(spModel);
        sp.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                pb4.setValue(((Integer)sp.getValue()).intValue());
                pb3.setValue(((Integer)sp.getValue()).intValue());
            }
        });

        final JPanel pp = new JPanel();
        pp.add(sp);

        pView5 = new JPanel(new BorderLayout());
        pView5.add(pp, BorderLayout.CENTER);
        pView5.add(pb1, BorderLayout.SOUTH);
        pView5.add(pb2, BorderLayout.EAST);
        pView5.add(pb3, BorderLayout.NORTH);
        pView5.add(pb4, BorderLayout.WEST);
    }

    class MiTL implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent ev)
        {
            if (ev.getActionCommand().equals("Top"))
            {
                tabPan.setTabPlacement(JTabbedPane.TOP);
            }
            else if (ev.getActionCommand().equals("Bottom"))
            {
                tabPan.setTabPlacement(JTabbedPane.BOTTOM);
            }
            else if (ev.getActionCommand().equals("Right"))
            {
                tabPan.setTabPlacement(JTabbedPane.RIGHT);
            }
            else if (ev.getActionCommand().equals("Left"))
            {
                tabPan.setTabPlacement(JTabbedPane.LEFT);
            }
        }
    }

    int index;

    ImageIcon[] lIcon = { OSDarkLAFUtils.loadImageResource("/icons/tools.png"),
        OSDarkLAFUtils.loadImageResource("/icons/folder_picture.png"),
        OSDarkLAFUtils.loadImageResource("/icons/network_local.png"), OSDarkLAFUtils.loadImageResource("/icons/news.png"), null };

    private Icon nextIcon()
    {
        if (index >= lIcon.length)
        {
            index = 0;
        }

        return lIcon[index++];
    }

    class MiFL implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent ev)
        {
            final JInternalFrame jif = new JInternalFrame(
                    "The Long Titled Internal Frame, n� (this is to test \"strange caracters\" titles)" + pos / 5, true, true,
                    true, true);

            jif.setVisible(true);
            jif.setFrameIcon(nextIcon());

            jif.setIconifiable(true);
            jif.setMaximizable(true);
            jif.setClosable(true);

            final JButton but = new JButton("Change Icon");
            but.addActionListener(new dummy(jif));
            jif.getContentPane().add(new JLabel("Another InternalFrame"));
            jif.getContentPane().add(but, BorderLayout.SOUTH);

            jif.setLocation(new Point(pos, pos));

            pos += 5;
            jif.pack();
            desktop.add(jif);

            try
            {
                jif.setSelected(true);
            }
            catch (final PropertyVetoException e)
            {
            }
        }
    }

    class dummy implements ActionListener
    {
        JInternalFrame jif;

        public dummy(JInternalFrame jif)
        {
            this.jif = jif;
        }

        @Override
        public void actionPerformed(ActionEvent ev)
        {
            jif.setFrameIcon(nextIcon());
        }
    }

    class MiAL implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent ev)
        {
            if (ev.getActionCommand().equals("Open"))
            {
                final JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File("."));
                if (fc.showOpenDialog(OSDarkLAFMain.this) != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                final String nomFich = fc.getSelectedFile().getPath();
                tNomFich.setText(nomFich);

                final Properties props = new Properties();
                try
                {
                    props.load(new FileInputStream(nomFich));// this.getClass().getResourceAsStream(
                                                             // nomFich));
                }
                catch (final Exception ex)
                {
                    ex.printStackTrace();
                    return;
                }

                nt = new OSDarkLAFTheme();

                cambiaTema();
            }
            else if (ev.getActionCommand().equals("Save"))
            {
                final JFileChooser fc = new JFileChooser();

                String nomFich = tNomFich.getText();
                if (nomFich.isEmpty())
                {
                    fc.setCurrentDirectory(new File("."));
                    fc.setSelectedFile(new File("OSDarkLAFThemeFile.theme"));
                }
                else
                {
                    final File fich = new File(nomFich);
                    fc.setSelectedFile(fich);
                }

                if (fc.showSaveDialog(OSDarkLAFMain.this) != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                nomFich = fc.getSelectedFile().getPath();
                tNomFich.setText(nomFich);

                try
                {
                    final FileOutputStream f = new FileOutputStream(nomFich);
                    f.write(nt.toString().getBytes());
                    f.close();
                }
                catch (final IOException ex)
                {
                    JOptionPane.showMessageDialog(OSDarkLAFMain.this, "No se puede guardar el fichero");
                }
            }
            else if (ev.getActionCommand().equals("Test"))
            {
                nt = new OSDarkLAFTheme();

                nt.setBlack(bB.getBackground());
                nt.setWhite(bW.getBackground());
                nt.setPrimary1(bP1.getBackground());
                nt.setPrimary2(bP2.getBackground());
                nt.setPrimary3(bP3.getBackground());
                nt.setSecondary1(bS1.getBackground());
                nt.setSecondary2(bS2.getBackground());
                nt.setSecondary3(bS3.getBackground());
                nt.setMenuOpacity(sMenuOpacidad.getValue());
                nt.setFrameOpacity(sFrameOpacidad.getValue());

                cambiaTema();
            }
        }

        protected void cambiaTema()
        {
            OpenSphereDarkLookAndFeel.setCurrentTheme(nt);
            try
            {
                UIManager.setLookAndFeel(nf);
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
            }

            SwingUtilities.updateComponentTreeUI(OSDarkLAFMain.this);

            hazPaleta();
        }
    }

    class MiML extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent ev)
        {
            if (ev.getComponent() == bP1)
            {
                bP1.setBackground(getColor(bP1.getBackground(), "Primary 1"));
            }
            else if (ev.getComponent() == bP2)
            {
                bP2.setBackground(getColor(bP2.getBackground(), "Primary 2"));
            }
            else if (ev.getComponent() == bP3)
            {
                bP3.setBackground(getColor(bP3.getBackground(), "Primary 3"));
            }
            else if (ev.getComponent() == bSelection)
            {
                final Color col = getColor(bSelection.getBackground(), "Selection");

                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                bP1.setBackground(new Color(r > 20 ? r - 20 : 0, g > 20 ? g - 20 : 0, b > 20 ? b - 20 : 0));
                bP2.setBackground(new Color(r > 10 ? r - 10 : 0, g > 10 ? g - 10 : 0, b > 10 ? b - 10 : 0));
                bP3.setBackground(col);
                bSelection.setBackground(col);
            }
            else if (ev.getComponent() == bS1)
            {
                bS1.setBackground(getColor(bS1.getBackground(), "Secondary 1"));
            }
            else if (ev.getComponent() == bS2)
            {
                bS2.setBackground(getColor(bS2.getBackground(), "Secondary 2"));
            }
            else if (ev.getComponent() == bS3)
            {
                bS3.setBackground(getColor(bS3.getBackground(), "Secondary 3"));
            }
            else if (ev.getComponent() == bBackground)
            {
                final Color col = getColor(bBackground.getBackground(), "Background");

                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                bS1.setBackground(new Color(r > 20 ? r - 20 : 0, g > 20 ? g - 20 : 0, b > 20 ? b - 20 : 0));
                bS2.setBackground(new Color(r > 10 ? r - 10 : 0, g > 10 ? g - 10 : 0, b > 10 ? b - 10 : 0));
                bS3.setBackground(col);
                bBackground.setBackground(col);
            }
            else if (ev.getComponent() == bW)
            {
                bW.setBackground(getColor(bW.getBackground(), "White"));
            }
            else if (ev.getComponent() == bB)
            {
                bB.setBackground(getColor(bB.getBackground(), "Black"));
            }
        }

        protected Color getColor(Color col, String cad)
        {
            Color ret = JColorChooser.showDialog(OSDarkLAFMain.this, cad, col);
            if (ret == null)
            {
                ret = col;
            }

            return ret;
        }
    }

    public static void main(String[] args)
    {
        try
        {
            nf = new OpenSphereDarkLookAndFeel();
            nt = new OSDarkLAFTheme();
            OpenSphereDarkLookAndFeel.setCurrentTheme(nt);

            UIManager.setLookAndFeel(nf);
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (final Exception ex)
        {
            System.out.println(ex);
            ex.printStackTrace();
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        new OSDarkLAFMain();
    }
}
