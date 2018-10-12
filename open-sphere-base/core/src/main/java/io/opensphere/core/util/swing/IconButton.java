package io.opensphere.core.util.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Timer;

import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A class that provides a common look and feel for an icon button.
 */
public class IconButton extends JButton implements CustomizableButton
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The helper. */
    private final transient IconButtonHelper myHelper;

    /** Whether the text is to be painted. */
    private boolean myTextPainted = true;

    /** Whether the icon is to be painted. */
    private boolean myIconPainted = true;

    /** Whether to auto-resize when text/icon is changed. */
    private boolean myAutoResize = true;

    /** The timer for firing events while holding the button. */
    private Timer myTimer;

    /** How many times the timer has been fired since starting. */
    private int myTimerCount;

    /** The hold listener. */
    private transient MouseAdapter myHoldListener;

    /**
     * Constructor.
     */
    public IconButton()
    {
        this(null, (Icon)null);
    }

    /**
     * Constructor.
     *
     * @param text The button text.
     * @param type The icon type.
     */
    public IconButton(String text, IconType type)
    {
        this(text, (Icon)null);
        IconUtil.setIcons(this, type);
    }

    /**
     * Constructor.
     *
     * @param text The button text.
     * @param type The icon type.
     * @param color The icon color.
     */
    public IconButton(String text, IconType type, Color color)
    {
        this(text, (Icon)null);
        IconUtil.setIcons(this, type, color);
    }

    /**
     * Constructor.
     *
     * @param icon the icon
     */
    public IconButton(Icon icon)
    {
        this(null, icon);
    }

    /**
     * Constructor.
     *
     * @param text the text
     */
    public IconButton(String text)
    {
        this(text, (Icon)null);
    }

    /**
     * Constructor.
     *
     * @param text the text
     * @param icon the icon
     */
    public IconButton(String text, Icon icon)
    {
        super(text, icon);
        myHelper = new IconButtonHelper(this);
    }

    /**
     * Constructor.
     *
     * @param type The icon type.
     */
    public IconButton(IconType type)
    {
        this(null, type);
    }

    /**
     * Constructor.
     *
     * @param type The icon type.
     * @param color The icon color.
     */
    public IconButton(IconType type, Color color)
    {
        this(null, (Icon)null);
        IconUtil.setIcons(this, type, color);
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        myHelper.handleAddNotify();
    }

    @Override
    public Icon getIcon()
    {
        return myIconPainted ? super.getIcon() : null;
    }

    @Override
    public String getText()
    {
        return myTextPainted || super.getIcon() == null ? super.getText() : null;
    }

    @Override
    public void setAutoResize(boolean autoResize)
    {
        myAutoResize = autoResize;
    }

    @Override
    public void setIcon(Icon icon)
    {
        super.setIcon(icon);
        if (myHelper != null && myAutoResize)
        {
            sizeToFit();
        }
    }

    @Override
    public void setIcon(String defaultIcon)
    {
        myHelper.setIcon(defaultIcon);
    }

    @Override
    public void setIconPainted(boolean iconPainted)
    {
        myIconPainted = iconPainted;
        sizeToFit();
        revalidate();
    }

    @Override
    public void setPressedIcon(String pressedIcon)
    {
        myHelper.setPressedIcon(pressedIcon);
    }

    @Override
    public void setRolloverIcon(String rolloverIcon)
    {
        myHelper.setRolloverIcon(rolloverIcon);
    }

    @Override
    public void setSelectedIcon(String selectedIcon)
    {
        myHelper.setSelectedIcon(selectedIcon);
    }

    @Override
    public void setText(String text)
    {
        super.setText(text);
        if (myHelper != null && myAutoResize)
        {
            sizeToFit();
        }
    }

    @Override
    public void setTextPainted(boolean textPainted)
    {
        myTextPainted = textPainted;
        sizeToFit();
        revalidate();
    }

    /**
     * Sets the delay between events while holding the button.
     *
     * @param delay milliseconds for the initial and between-event delay. A
     *            delay of 0 means no events will be fired while holding.
     */
    public void setHoldDelay(int delay)
    {
        if (delay > 0)
        {
            if (myTimer == null)
            {
                myTimer = new Timer(delay, e ->
                {
                    ++myTimerCount;
                    fireActionPerformed(e);
                });
            }
            if (myHoldListener == null)
            {
                myHoldListener = new MouseAdapter()
                {
                    @Override
                    public void mouseReleased(MouseEvent e)
                    {
                        myTimer.stop();
                    }

                    @Override
                    public void mouseExited(MouseEvent e)
                    {
                        myTimer.stop();
                    }

                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        myTimerCount = 0;
                        myTimer.start();
                    }
                };
            }

            myTimer.setDelay(delay);
            addMouseListener(myHoldListener);
        }
        else if (myHoldListener != null)
        {
            removeMouseListener(myHoldListener);
        }
    }

    @Override
    protected void fireActionPerformed(ActionEvent event)
    {
        if ((event.getModifiers() & InputEvent.BUTTON1_DOWN_MASK) == 0 || myTimerCount == 0)
        {
            super.fireActionPerformed(event);
        }
    }

    /**
     * Sizes the button to fit the text and/or icon.
     */
    protected void sizeToFit()
    {
        if (myHelper != null)
        {
            myHelper.sizeToFit();
        }
    }
}
