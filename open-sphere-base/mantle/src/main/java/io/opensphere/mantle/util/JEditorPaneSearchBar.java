package io.opensphere.mantle.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Search Bar for a plain text JEditor pane. Does not work with HTML editor
 * panes.
 */
@SuppressWarnings("serial")
public class JEditorPaneSearchBar extends JPanel implements KeyListener, ActionListener
{
    /** The our logger. */
    private static final Logger LOGGER = Logger.getLogger(JEditorPaneSearchBar.class);

    /** The Clear search button. */
    private final JButton myClearSearchBT;

    /** The Editor. */
    private final JEditorPane myEditor;

    /** The Found color. */
    private final Color myFoundColor;

    /** The Highlight all check box. */
    private final JCheckBox myHilightAllCheckBox;

    /** The Highlight all color. */
    private final Color myHilightAllColor;

    /** The Last found index. */
    private int myLastFoundIndex = -1;

    /** The Last search term. */
    private String myLastSearchTerm = "";

    /** The last used search term. */
    private String myLastUsedSearchTerm = "";

    /** The Match case check box. */
    private final JCheckBox myMatchCaseCheckBox;

    /** The Next button. */
    private final JButton myNextBT;

    /** The original search text area background color. */
    private final Color myOrigSearchTextAreaBackgroundColor;

    /** The preview button. */
    private final JButton myPrevBT;

    /** The Search text field. */
    private final JTextField mySearchTF;

    /** The Search timer. */
    private final Timer mySearchTimer;

    /** The Search type. */
    private SearchType mySearchType = SearchType.FORWARD;

    /**
     * Instantiates a new j editor pane search bar.
     *
     * @param editor the editor
     */
    public JEditorPaneSearchBar(JEditorPane editor)
    {
        this(editor, new Color(0, 255, 255), Color.LIGHT_GRAY);
    }

    /**
     * Instantiates a new j editor pane search bar.
     *
     * @param editor the editor
     * @param foundColor the found color
     * @param hilightAllColor the hilight all color
     */
    public JEditorPaneSearchBar(JEditorPane editor, Color foundColor, Color hilightAllColor)
    {
        myEditor = editor;
        myFoundColor = foundColor;
        myHilightAllColor = hilightAllColor;

        mySearchTF = new JTextField();
        mySearchTF.addKeyListener(this);
        mySearchTF.addActionListener(this);
        myOrigSearchTextAreaBackgroundColor = mySearchTF.getBackground();
        myClearSearchBT = new JButton("X");
        myClearSearchBT.setFont(myClearSearchBT.getFont().deriveFont(Font.BOLD));
        myClearSearchBT.setFocusable(false);
        myClearSearchBT.setMargin(new Insets(2, 5, 2, 4));
        myClearSearchBT.addActionListener(this);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        myNextBT = new JButton("Next");
        myNextBT.addActionListener(this);
        myPrevBT = new JButton("Prev");
        myPrevBT.addActionListener(this);
        myMatchCaseCheckBox = new JCheckBox("Match Case", false);
        myMatchCaseCheckBox.addActionListener(this);
        myHilightAllCheckBox = new JCheckBox("Highlight all", false);
        myHilightAllCheckBox.addActionListener(this);

        add(Box.createHorizontalStrut(3));
        add(myClearSearchBT);
        add(Box.createHorizontalStrut(6));
        add(new JLabel("Search"));
        add(Box.createHorizontalStrut(3));
        add(mySearchTF);
        add(Box.createHorizontalStrut(3));
        add(myNextBT);
        add(Box.createHorizontalStrut(3));
        add(myPrevBT);
        add(Box.createHorizontalStrut(3));
        add(myHilightAllCheckBox);
        add(Box.createHorizontalStrut(3));
        add(myMatchCaseCheckBox);
        add(Box.createHorizontalStrut(3));

        setMinimumSize(new Dimension(300, 30));
        setPreferredSize(new Dimension(300, 30));
        setMaximumSize(new Dimension(300, 30));

        mySearchTimer = new Timer(300, this);
        mySearchTimer.setRepeats(false);
        mySearchTimer.setCoalesce(true);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == myClearSearchBT)
        {
            mySearchTF.setText("");
            String term = mySearchTF.getText();
            if (!term.equals(myLastSearchTerm))
            {
                myLastFoundIndex = myEditor.getCaretPosition();
                myLastSearchTerm = term;
            }
            doSearch();
        }
        // else if (e.getSource() == mySearchTF)
        // {
        // // DO nothing.
        // }
        else if (e.getSource() == myNextBT)
        {
            doSearch();
        }
        else if (e.getSource() == myPrevBT)
        {
            mySearchType = SearchType.BACKWARD;
            doSearch();
        }
        else if (e.getSource() == myHilightAllCheckBox)
        {
            mySearchType = SearchType.NOMOVEMENT;
            doSearch();
        }
        else if (e.getSource() == myMatchCaseCheckBox)
        {
            doSearch();
        }
        else if (e.getSource() == mySearchTimer)
        {
            doSearch();
        }
    }

    /**
     * Do search.
     */
    public synchronized void doSearch()
    {
        EventQueueUtilities.runOnEDT(new SearchWorker());
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        String term = mySearchTF.getText();
        if (!term.equals(myLastSearchTerm))
        {
            myLastFoundIndex = myEditor.getCaretPosition();
            myLastSearchTerm = term;
        }
        mySearchTimer.restart();
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * The Enum SearchType.
     */
    private enum SearchType
    {
        /** The BACKWARD. */
        BACKWARD,

        /** The FORWARD. */
        FORWARD,

        /** The NOMOVEMENT. */
        NOMOVEMENT
    }

    /**
     * The Class SearchWorker.
     */
    private class SearchWorker implements Runnable
    {
        @Override
        public void run()
        {
            // Reset the last found index depending on a few
            // special conditions, such as if it is -1 it means
            // we didn't find anything last time we search, so start
            // at the current caret position
            if (myLastFoundIndex < 0)
            {
                myLastFoundIndex = myEditor.getCaretPosition();
            }

            // If we're not searching backwards and we've reached
            // the end of the document, reset to search from the
            // head of the document
            if (mySearchType != SearchType.BACKWARD && myLastFoundIndex == myEditor.getText().length())
            {
                myLastFoundIndex = 0;
            }

            // Clear all old highlights
            myEditor.getHighlighter().removeAllHighlights();

            // Reset the search bar background to it's normal color
            // indicating no problem.
            mySearchTF.setBackground(myOrigSearchTextAreaBackgroundColor);

            // Take care of the case sensitive part of the search
            // by lower casing both the term and search text if the match
            // case is not on
            String textToSearch = determineTextToSearch();
            String termToSearch = determineTermToSearch();

            // Determine if our term has changed
            boolean searchTermChanged = !myLastUsedSearchTerm.equals(termToSearch);
            myLastUsedSearchTerm = termToSearch;

            int index = myLastFoundIndex;
            if (termToSearch != null && !termToSearch.isEmpty())
            {
                switch (mySearchType)
                {
                    case FORWARD:
                        index = searchForward(index, searchTermChanged, textToSearch, termToSearch);
                        break;
                    case BACKWARD:
                        index = searchBackward(index, searchTermChanged, textToSearch, termToSearch);
                        break;
                    case NOMOVEMENT:
                        break;
                    default:
                        break;
                }

                if (index != -1)
                {
                    if (mySearchType != SearchType.NOMOVEMENT)
                    {
                        myEditor.setCaretPosition(index);
                    }
                    // Highlight our current found
                    try
                    {
                        myEditor.getHighlighter().addHighlight(index, index + termToSearch.length(),
                                new DefaultHighlighter.DefaultHighlightPainter(myFoundColor));
                    }
                    catch (BadLocationException e)
                    {
                        // NOPMD:GuardLogStatement
                        LOGGER.trace(e);
                    }
                }
                else
                {
                    // If we're here we didn't find the term while searching
                    // anywhere in the document
                    // mark the background of the search bar to a light red
                    // to indicate not found
                    mySearchTF.setBackground(new Color(255, 133, 144));
                }
                myLastFoundIndex = index;

                // Now handle the highlight all case where we need to find
                // all occurrences and mark them.
                if (myHilightAllCheckBox.isSelected())
                {
                    index = textToSearch.indexOf(termToSearch, 0);
                    while (index != -1)
                    {
                        try
                        {
                            myEditor.getHighlighter().addHighlight(index, index + termToSearch.length(),
                                    new DefaultHighlighter.DefaultHighlightPainter(myHilightAllColor));
                        }
                        catch (BadLocationException e)
                        {
                            // NOPMD:GuardLogStatement
                            LOGGER.trace(e);
                        }
                        index = textToSearch.indexOf(termToSearch, index + termToSearch.length());
                    }
                }
            }

            // Reset search to forward
            mySearchType = SearchType.FORWARD;
        }

        /**
         * Determine term to search.
         *
         * @return the string
         */
        private String determineTermToSearch()
        {
            return myMatchCaseCheckBox.isSelected() ? myLastSearchTerm : myLastSearchTerm.toLowerCase();
        }

        /**
         * Determine text to search.
         *
         * @return the string
         */
        private String determineTextToSearch()
        {
            return myMatchCaseCheckBox.isSelected() ? myEditor.getText() : myEditor.getText().toLowerCase();
        }

        /**
         * Search backward.
         *
         * @param index the index
         * @param searchTermChanged the search term changed
         * @param textToSearch the text to search
         * @param termToSearch the term to search
         * @return the int
         */
        private int searchBackward(int index, boolean searchTermChanged, String textToSearch, String termToSearch)
        {
            int length = textToSearch.length();
            int newIndex = index;
            // Adjust the end index for the search depending on
            // if the search term changed.
            int endIdx = searchTermChanged ? myLastFoundIndex : myLastFoundIndex - termToSearch.length();
            newIndex = textToSearch.lastIndexOf(termToSearch, endIdx);

            // If we didn't find it searching from the last
            // position
            // reset to the end of the document and try again.
            if (newIndex == -1)
            {
                newIndex = textToSearch.lastIndexOf(termToSearch, length);
            }
            return newIndex;
        }

        /**
         * Search forward.
         *
         * @param index the index
         * @param searchTermChanged the search term changed
         * @param textToSearch the text to search
         * @param termToSearch the term to search
         * @return the next index
         */
        private int searchForward(int index, boolean searchTermChanged, String textToSearch, String termToSearch)
        {
            int newIndex = index;
            // Adjust the search start index depending on
            // whether this is a new search term or not
            // make sure to increment forward if the term didn't
            // change so we don't continually re-find the
            // same result
            int startIdx = myLastFoundIndex == 0 ? myLastFoundIndex
                    : myLastFoundIndex + (searchTermChanged ? 0 : termToSearch.length());
            newIndex = textToSearch.indexOf(termToSearch, startIdx);

            // If we didn't find it searching forward from the
            // last position
            // reset and search from the top again.
            if (index == -1)
            {
                newIndex = textToSearch.indexOf(termToSearch, 0);
            }
            return newIndex;
        }
    }
}
