package io.opensphere.mantle.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * A dialog for viewing formatted text content.
 */
@SuppressWarnings("serial")
public class TextViewDialog extends JDialog
{
    /** The our logger. */
    private static final Logger LOGGER = Logger.getLogger(TextViewDialog.class);

    /** The Text area. */
    private final JEditorPane myTextArea;

    /**
     * Instantiates a new text view dialog.
     *
     * @param owner the owner
     * @param title the title
     * @param text the text
     */
    public TextViewDialog(Component owner, String title, String text)
    {
        this(owner, title, text, true, null);
    }

    /**
     * Instantiates a new text view dialog.
     *
     * @param owner the owner
     * @param title the title
     * @param text the text
     * @param editable the editable
     * @param prefRegistry The preferences registry.
     */
    public TextViewDialog(Component owner, String title, String text, boolean editable, final PreferencesRegistry prefRegistry)
    {
        super(SwingUtilities.getWindowAncestor(owner), title, ModalityType.MODELESS);
        setLocationRelativeTo(owner);
        setSize(800, 600);

        myTextArea = new JEditorPane("text/plain", text);
        Font f1 = myTextArea.getFont();
        myTextArea.setFont(new Font(Font.MONOSPACED, f1.getStyle(), f1.getSize()));
        myTextArea.setEditable(editable);

        JScrollPane jsp = new JScrollPane(myTextArea);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(jsp, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JMenuItem closeMI = new JMenuItem("Close");
        closeMI.addActionListener(e ->
            {
                TextViewDialog.this.setVisible(false);
                TextViewDialog.this.dispose();
            }
        );

        JMenuItem saveToFileMI = new JMenuItem("Save to File");
        saveToFileMI.addActionListener(e -> writeToFile(prefRegistry));

        fileMenu.add(saveToFileMI);
        fileMenu.addSeparator();
        fileMenu.add(closeMI);

        JEditorPaneSearchBar searchBar = new JEditorPaneSearchBar(myTextArea, Color.blue, Color.DARK_GRAY);
        mainPanel.add(searchBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        myTextArea.setCaretPosition(0);
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(final String text)
    {
        EventQueueUtilities.runOnEDT(() ->
            {
                myTextArea.setText(text);
                myTextArea.setCaretPosition(0);
            }
        );
    }

    /**
     * Displays the file chooser, and if the user approves, writes the contents of the dialog to file.
     *
     * @param pPreferencesRegistry the registry from which preferences are extracted, allowing the dialog to be configured.
     */
    protected void writeToFile(final PreferencesRegistry pPreferencesRegistry)
    {
        MnemonicFileChooser mfc = new MnemonicFileChooser(pPreferencesRegistry, "TextViewDialog");

        int option = mfc.showSaveDialog(TextViewDialog.this);
        if (option == JFileChooser.APPROVE_OPTION)
        {
            File aFile = mfc.getSelectedFile();
            try
            {
                PrintWriter pw = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), StringUtilities.DEFAULT_CHARSET)));
                try
                {
                    pw.print(myTextArea.getText());
                }
                finally
                {
                    pw.flush();
                    pw.close();
                }
            }
            catch (FileNotFoundException exc)
            {
                if (LOGGER.isTraceEnabled())
                {
                    LOGGER.trace(exc);
                }
            }
        }
    }
}
