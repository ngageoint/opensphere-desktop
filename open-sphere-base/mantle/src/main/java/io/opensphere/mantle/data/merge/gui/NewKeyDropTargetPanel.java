package io.opensphere.mantle.data.merge.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Stroke;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.merge.gui.DataTypeKeyMoveDNDCoordinator.KeyMoveListener;

/**
 * The Class NewKeyDropTargetPanel.
 */
@SuppressWarnings("serial")
public class NewKeyDropTargetPanel extends JPanel implements KeyMoveListener
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(NewKeyDropTargetPanel.class);

    /** The Constant dash. */
    private static final float[] ourDash = { 5.0f };

    /** The Allow only with special keys. */
    private final boolean myAllowOnlyWithSpecialKeys;

    /** The Data type merge panel. */
    private final DataTypeMergePanel myDataTypeMergePanel;

    /** The Line color. */
    private Color myLineColor = Color.white;

    /** The Message parts. */
    private final String[] myMessageParts;

    /**
     * Instantiates a new new key drop target panel.
     *
     * @param cdr the cdr
     * @param dtmp the dtmp
     * @param lm the lm
     * @param msg the msg
     * @param specialKeysOnly the special keys only
     */
    public NewKeyDropTargetPanel(DataTypeKeyMoveDNDCoordinator cdr, DataTypeMergePanel dtmp, LayoutManager lm, String msg,
            boolean specialKeysOnly)
    {
        super(lm);
        cdr.addKeyMoveListener(this);
        myDataTypeMergePanel = dtmp;
        myAllowOnlyWithSpecialKeys = specialKeysOnly;
        myMessageParts = msg.split("\n");
        setTransferHandler(new TransferHandler()
        {
            @SuppressWarnings("PMD.SimplifiedTernary")
            @Override
            public boolean canImport(TransferHandler.TransferSupport info)
            {
                if (!info.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
                {
                    return false;
                }

                Transferable tf = info.getTransferable();
                try
                {
                    TypeKeyEntry tke = (TypeKeyEntry)tf.getTransferData(TypeKeyEntry.ourDataFlavor);
                    return myAllowOnlyWithSpecialKeys ? tke.getSpecialKeyType() != null : true;
                }
                catch (UnsupportedFlavorException | IOException e)
                {
                    LOGGER.warn(e);
                }
                return false;
            }

            @Override
            public int getSourceActions(JComponent c)
            {
                return MOVE;
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport info)
            {
                if (!info.isDrop())
                {
                    return false;
                }

                // Check for String flavor
                if (!info.isDataFlavorSupported(TypeKeyEntry.ourDataFlavor))
                {
                    EventQueueUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            JOptionPane.showMessageDialog(null, "Doesn't accept a drop of this type.");
                        }
                    });
                    return false;
                }

                TypeKeyEntry data;
                try
                {
                    data = (TypeKeyEntry)info.getTransferable().getTransferData(TypeKeyEntry.ourDataFlavor);
                }
                catch (UnsupportedFlavorException | IOException e)
                {
                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace(e, e);
                    }
                    return false;
                }

                if (data.getOwner() != null)
                {
                    data.setOwner(null);
                    ((TypeKeyPanel)data.getOwner()).acceptedTransferOfEntry(data);
                }

                final TypeKeyEntry toAdd = data;
                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        myDataTypeMergePanel.createNewMappedTypeFromEntry(toAdd, myAllowOnlyWithSpecialKeys);
                    }
                });

                return true;
            }
        });
    }

    @Override
    public void keyMoveCompleted(TypeKeyEntry entry, TypeKeyPanel origPanel)
    {
        setLineColor(Color.white);
    }

    @SuppressWarnings("PMD.SimplifiedTernary")
    @Override
    public void keyMoveInitiated(TypeKeyEntry entry, TypeKeyPanel sourcePanel, Object source)
    {
        boolean acceptable = myAllowOnlyWithSpecialKeys ? entry.getSpecialKeyType() != null : true;
        setLineColor(acceptable ? Color.green : Color.red);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D)g;
        Stroke origStroke = g2D.getStroke();
        BasicStroke bs = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, ourDash, 0f);
        g2D.setStroke(bs);
        g2D.setColor(myLineColor);
        g2D.drawRoundRect(0, 2, getWidth() - 1, getHeight() - 3, 20, 20);
        FontMetrics fm = g2D.getFontMetrics();

        g2D.setColor(Color.white);
        g2D.setStroke(origStroke);
        int totalheight = 0;
        for (String part : myMessageParts)
        {
            LineMetrics lm = fm.getLineMetrics(part, g);
            totalheight += lm.getHeight();
        }

        int startY = (int)(0.5 * (getHeight() - totalheight)) + fm.getHeight() - 3;
        for (String part : myMessageParts)
        {
            Rectangle2D r = fm.getStringBounds(part, g);
            int x = (int)(getWidth() / 2.0 - r.getWidth() / 2.0);
            g2D.drawString(part, x, startY);
            startY += r.getHeight();
        }
    }

    /**
     * Sets the line color.
     *
     * @param c the new line color
     */
    private void setLineColor(final Color c)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                myLineColor = c;
                repaint();
            }
        });
    }
}
