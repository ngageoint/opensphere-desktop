package io.opensphere.core.dialog.alertviewer.toast;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * The UI for a toast message.
 *
 */
class ToastView extends JDialog
{
    /**
     * The current opacity value between 0-1.
     */
    private float myOpacity;

    /**
     * The serial version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The model for the view.
     */
    private final ToastModel myModel;

    /**
     * The main window to display in.
     */
    private final Frame myFrame;

    /**
     * Constructs a new toast view.
     *
     * @param frame The parent frame
     * @param model The model for the view.
     */
    public ToastView(Frame frame, ToastModel model)
    {
        super(frame, false);
        myModel = model;
        myFrame = frame;
        initComponents();
    }

    /**
     * Gets the opacity of the view.
     *
     * @return The opacity value between 0 and 1.
     */
    public float getTheOpacity()
    {
        return myOpacity;
    }

    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, myOpacity));

        super.paint(g2);
    }

    /**
     * Sets the opacity of the view.
     *
     * @param opacity The opacity value between 0 and 1.
     */
    public void setTheOpacity(float opacity)
    {
        myOpacity = opacity;
        repaint();
    }

    /**
     * Initializes the components.
     */
    private void initComponents()
    {
        myOpacity = 0f;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setUndecorated(true);
        Point loc = myFrame.getLocation();
        getContentPane().setBackground(myModel.getColor());

        JLabel label = new JLabel();
        if (myModel.getColor().equals(Color.yellow) || myModel.getColor().equals(Color.white))
        {
            label.setForeground(Color.black);
        }

        String message = myModel.getMessage();
        String multilineMessage = "<html>" + message + "</html>";
        multilineMessage = multilineMessage.replace("\n", "<br>");
        label.setText(multilineMessage);

        add(label);

        pack();

        if (getWidth() > myFrame.getWidth())
        {
            this.setSize(myFrame.getWidth() - 20, getHeight());
        }

        if (getHeight() > myFrame.getHeight() / 4)
        {
            this.setSize(getWidth(), myFrame.getHeight() / 4);
        }

        setLocation(loc.x + myFrame.getWidth() / 2 - this.getSize().width / 2,
                loc.y + myFrame.getHeight() - this.getSize().height - 55);
    }
}
