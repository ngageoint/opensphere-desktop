package io.opensphere.overlay;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.control.DiscreteEventAdapter;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Manager for a popup that displays the latest cursor position. */
public class CursorPositionPopupManager
{
    /** The Alt text. */
    private String myAltText;

    /** Supplier for the dialog parent. */
    private final Supplier<? extends JFrame> myDialogParentSupplier;

    /** The Lat text. */
    private String myLatText;

    /** The Lon text. */
    private String myLonText;

    /** The MGRS text. */
    private String myMGRSText;

    /** The key listener that displays a popup with the cursor position. */
    private final DiscreteEventAdapter myPopupListener = new DiscreteEventAdapter("Cursor Position", "Display Cursor Position",
            "Show a popup with the current mouse cursor position")
    {
        /** Counter for mouse position popups. */
        private int myCounter;

        @Override
        public void eventOccurred(InputEvent event)
        {
            if (!StringUtils.isBlank(myMGRSText))
            {
                EventQueueUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        JDialog dialog = new JDialog(myDialogParentSupplier.get());
                        dialog.setTitle("Mouse Position " + ++myCounter);
                        JPanel detailsPanel = new JPanel(new BorderLayout());
                        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        StringBuilder text = new StringBuilder();
                        text.append(myMGRSText).append("  ");
                        text.append(myLatText).append("  ");
                        text.append(myLonText).append("  ");
                        if (StringUtils.isNotEmpty(myAltText))
                        {
                            text.append(myAltText);
                        }
                        JTextArea detailsText = new JTextArea(text.toString());
                        detailsText.setBackground(detailsPanel.getBackground());
                        detailsText.setBorder(BorderFactory.createEmptyBorder());
                        detailsText.setEditable(false);
                        detailsPanel.add(detailsText);
                        dialog.getContentPane().add(detailsPanel, BorderLayout.CENTER);
                        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                        dialog.setLocationRelativeTo(dialog.getParent());
                        dialog.pack();
                        dialog.setVisible(true);
                    }
                });
            }
        }
    };

    /**
     * Constructor.
     *
     * @param dialogParentSupplier The dialog parent provider.
     */
    public CursorPositionPopupManager(Supplier<? extends JFrame> dialogParentSupplier)
    {
        myDialogParentSupplier = dialogParentSupplier;
    }

    /**
     * Get the listener for mouse events.
     *
     * @return The listener.
     */
    public DiscreteEventAdapter getListener()
    {
        return myPopupListener;
    }

    /**
     * Set the latest cursor position.
     *
     * @param lat The lat.
     * @param lon The lon.
     * @param alt The alt.
     * @param mgrs The mgrs.
     */
    public void setLabels(String lat, String lon, String alt, String mgrs)
    {
        myLatText = lat;
        myLonText = lon;
        myAltText = alt;
        myMGRSText = mgrs;
    }
}
