package io.opensphere.core.util.swing;

import javax.swing.Icon;
import javax.swing.JToggleButton;

/**
 * A class that provides a common look and feel for an icon toggle button.
 */
public class IconToggleButton extends JToggleButton implements CustomizableButton
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The helper. */
    private final IconButtonHelper myHelper;

    /** Whether the text is to be painted. */
    private boolean myTextPainted = true;

    /** Whether the icon is to be painted. */
    private boolean myIconPainted = true;

    /** Whether to auto-resize when text/icon is changed. */
    private boolean myAutoResize = true;

    /**
     * Constructor.
     */
    public IconToggleButton()
    {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param icon the icon
     */
    public IconToggleButton(Icon icon)
    {
        this(null, icon);
    }

    /**
     * Constructor.
     *
     * @param text the text
     */
    public IconToggleButton(String text)
    {
        this(text, null);
    }

    /**
     * Constructor.
     *
     * @param text the text
     * @param icon the icon
     */
    public IconToggleButton(String text, Icon icon)
    {
        super(text, icon);
        myHelper = new IconButtonHelper(this);
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
        return myTextPainted ? super.getText() : null;
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
