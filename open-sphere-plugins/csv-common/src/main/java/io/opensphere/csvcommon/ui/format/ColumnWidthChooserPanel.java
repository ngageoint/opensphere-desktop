package io.opensphere.csvcommon.ui.format;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * The Class ColumnWidthChooserPanel.
 */
public class ColumnWidthChooserPanel extends JPanel
{
    /** The border lead trail. */
    private static final int ourBorderLeadTrail = 5;

    /** The border top bottom. */
    private static final int ourBorderTopBottom = 10;

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The my action listener. */
    private ActionListener myActionListener;

    /** The my width text panel. */
    private final JWidthTextPanel myWidthTextPanel;

    /**
     * Instantiates a new column width chooser panel.
     *
     * @param text the text
     * @param listener the listener
     */
    public ColumnWidthChooserPanel(String text, ActionListener listener)
    {
        super();
        myActionListener = listener;
        setLayout(new BorderLayout());
        myWidthTextPanel = new JWidthTextPanel(text);
        add(myWidthTextPanel, BorderLayout.CENTER);
    }

    /**
     * Sets the actionListener.
     *
     * @param actionListener the actionListener
     */
    public void setActionListener(ActionListener actionListener)
    {
        myActionListener = actionListener;
    }

    /**
     * Gets the column breaks.
     *
     * @return the column breaks
     */
    public int[] getColumnBreaks()
    {
        return myWidthTextPanel.getColumnBreaks();
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText()
    {
        return myWidthTextPanel.getText();
    }

    @Override
    public void setBackground(Color c)
    {
        super.setBackground(c);
        if (myWidthTextPanel != null)
        {
            myWidthTextPanel.setBackground(c);
        }
    }

    /**
     * Sets the column breaks.
     *
     * @param widths the new column breaks
     */
    public void setColumnBreaks(int[] widths)
    {
        myWidthTextPanel.setColumnBreaks(widths);
    }

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public void setText(String text)
    {
        myWidthTextPanel.setText(text);
    }

    /**
     * The Class JWidthTextPanel.
     */
    @SuppressWarnings("serial")
    private class JWidthTextPanel extends JPanel implements MouseListener, MouseMotionListener
    {
        /** The my col widths. */
        private final TIntList myColWidths;

        /** The font width. */
        private final int myFontWidth;

        /** The marker in motion char. */
        private int myMarkerInMotionChar = -1;

        /** The my text area. */
        private final CustomTextArea myTextArea;

        /**
         * Instantiates a new j width text panel.
         *
         * @param text the text
         */
        public JWidthTextPanel(String text)
        {
            myColWidths = new TIntArrayList();
            myTextArea = new CustomTextArea(text);
            myTextArea.setEditable(false);
            myTextArea.setHighlighter(null);
            setLayout(new BorderLayout());
            myTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            myTextArea.setBorder(BorderFactory.createEmptyBorder(ourBorderTopBottom, ourBorderLeadTrail, ourBorderTopBottom,
                    ourBorderLeadTrail));
            add(myTextArea, BorderLayout.CENTER);

            FontMetrics fm = myTextArea.getFontMetrics(myTextArea.getFont());
            myFontWidth = fm.charWidth('-');

            myTextArea.addMouseListener(this);
            myTextArea.addMouseMotionListener(this);

            addMouseListener(this);
            addMouseMotionListener(this);
        }

        /**
         * Char index at x pos.
         *
         * @param x the x
         * @return the int
         */
        public int charIndexAtXPos(int x)
        {
            int t = x - ourBorderLeadTrail - 3;
            if (t > 0)
            {
                t = (int)Math.floor((double)t / (double)myFontWidth);
                return t + 1;
            }
            return 0;
        }

        /**
         * Fire action event.
         */
        public void fireActionEvent()
        {
            if (myActionListener != null)
            {
                myActionListener.actionPerformed(new ActionEvent(ColumnWidthChooserPanel.this, 0, "COLUMN_BREAKS_CHANGED"));
            }
        }

        /**
         * Gets the column breaks.
         *
         * @return the column breaks
         */
        public int[] getColumnBreaks()
        {
            if (myColWidths.isEmpty())
            {
                return null;
            }
            else
            {
                // Remove 0 because it's useless and just causes problems for
                // the caller
                TIntArrayList copy = new TIntArrayList(myColWidths);
                copy.remove(0);
                return myColWidths.toArray();
            }
        }

        /**
         * Gets the text.
         *
         * @return the text
         */
        public String getText()
        {
            return myTextArea.getText();
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            int charIndex = charIndexAtXPos((int)e.getPoint().getX());
            if (!myColWidths.contains(charIndex))
            {
                myColWidths.add(charIndex);
                myColWidths.sort();
                fireActionEvent();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            myMarkerInMotionChar = charIndexAtXPos((int)e.getPoint().getX());
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e)
        {
        }

        @Override
        public void mouseExited(MouseEvent e)
        {
        }

        @Override
        public void mouseMoved(MouseEvent e)
        {
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            myMarkerInMotionChar = charIndexAtXPos((int)e.getPoint().getX());

            myColWidths.remove(myMarkerInMotionChar);

            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            if (myMarkerInMotionChar != -1)
            {
                if (e.getPoint().getY() >= 0 && e.getPoint().getY() <= getHeight() && !myColWidths.contains(myMarkerInMotionChar))
                {
                    myColWidths.add(myMarkerInMotionChar);
                    myColWidths.sort();
                }
                myMarkerInMotionChar = -1;
                repaint();
                fireActionEvent();
            }
        }

        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            Graphics2D g2d = (Graphics2D)g;

            g2d.setColor(getForeground());

            for (int i = ourBorderLeadTrail; i < getWidth(); i += myFontWidth)
            {
                g2d.drawLine(i, 5, i, 9);
                g2d.drawLine(i, getHeight() - 5, i, getHeight() - 9);
            }

            for (int i = 0; i < myColWidths.size(); i++)
            {
                paintMarker(g2d, myColWidths.get(i), false, getForeground());
            }

            if (myMarkerInMotionChar > -1)
            {
                paintMarker(g2d, myMarkerInMotionChar, true, Color.RED);
            }
        }

        /**
         * Paint down triangle.
         *
         * @param g the g
         * @param x the x
         * @param y the y
         */
        public void paintDownTriangle(Graphics2D g, int x, int y)
        {
            g.drawLine(x, y, x, y - 5);
            g.drawLine(x + 1, y - 1, x + 1, y - 5);
            g.drawLine(x - 1, y - 1, x - 1, y - 5);
            g.drawLine(x + 2, y - 2, x + 2, y - 5);
            g.drawLine(x - 2, y - 2, x - 2, y - 5);
            g.drawLine(x + 3, y - 3, x + 3, y - 5);
            g.drawLine(x - 3, y - 3, x - 3, y - 5);
        }

        /**
         * Paint marker.
         *
         * @param g the g
         * @param charIndex the char index
         * @param drawLine the draw line
         * @param c the c
         */
        public void paintMarker(Graphics2D g, int charIndex, boolean drawLine, Color c)
        {
            g.setColor(c);
            int x = charIndex * myFontWidth + ourBorderLeadTrail;
            paintDownTriangle(g, x, 6);
            paintUpTriangle(g, x, getHeight() - 7);

            if (drawLine)
            {
                g.drawLine(x, 5, x, getHeight() - 5);
            }
        }

        /**
         * Paint up triangle.
         *
         * @param g the g
         * @param x the x
         * @param y the y
         */
        public void paintUpTriangle(Graphics2D g, int x, int y)
        {
            g.drawLine(x, y, x, y + 5);
            g.drawLine(x + 1, y + 1, x + 1, y + 5);
            g.drawLine(x - 1, y + 1, x - 1, y + 5);
            g.drawLine(x + 2, y + 2, x + 2, y + 5);
            g.drawLine(x - 2, y + 2, x - 2, y + 5);
            g.drawLine(x + 3, y + 3, x + 3, y + 5);
            g.drawLine(x - 3, y + 3, x - 3, y + 5);
        }

        @Override
        public void setBackground(Color c)
        {
            super.setBackground(c);
            if (myTextArea != null)
            {
                myTextArea.setBackground(c);
            }
        }

        /**
         * Sets the column breaks.
         *
         * @param widths the new column breaks
         */
        public void setColumnBreaks(int[] widths)
        {
            myColWidths.clear();
            if (widths != null)
            {
                for (int i = 0; i < widths.length; i++)
                {
                    if (!myColWidths.contains(widths[i]))
                    {
                        myColWidths.add(widths[i]);
                    }
                }
            }

            myColWidths.sort();
            fireActionEvent();
        }

        /**
         * Sets the text.
         *
         * @param text the new text
         */
        public void setText(String text)
        {
            myTextArea.setText(text);
        }

        /**
         * The Class CustomTextArea.
         */
        private class CustomTextArea extends JTextArea
        {
            /**
             * Instantiates a new custom text area.
             *
             * @param text the text
             */
            public CustomTextArea(String text)
            {
                super(text);
            }

            @Override
            public void paint(Graphics g)
            {
                super.paint(g);

                g.setColor(getForeground());
                for (int i = 0; i < myColWidths.size(); i++)
                {
                    int x = myColWidths.get(i) * myFontWidth + ourBorderLeadTrail;
                    g.drawLine(x, 0 + ourBorderTopBottom, x, getHeight() - ourBorderTopBottom);
                }
            }
        }
    }
}
