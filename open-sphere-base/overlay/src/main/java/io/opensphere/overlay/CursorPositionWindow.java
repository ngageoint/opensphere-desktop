package io.opensphere.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.layout.GridLayout;
import io.opensphere.core.hud.framework.layout.GridLayoutConstraints;
import io.opensphere.core.hud.widget.TextLabel;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/**
 * The Class CursorPositionWindow.
 */
class CursorPositionWindow extends InfoOverlayWindow
{
    /** The MGRS label. */
    private TextLabel myMGRSTextLabel;

    /** The Lat label. */
    private TextLabel myLatTextLabel;

    /** The Lon label. */
    private TextLabel myLonTextLabel;

    /** The Alt label. */
    private TextLabel myAltTextLabel;

    /**
     * Instantiates a new cursor position window.
     *
     * @param helper the helper
     * @param bbox the bbox
     * @param loc the loc
     * @param zOrder the z order
     */
    public CursorPositionWindow(TransformerHelper helper, ScreenBoundingBox bbox, ToolLocation loc, int zOrder)
    {
        super(helper, bbox, loc, ResizeOption.RESIZE_KEEP_FIXED_SIZE, zOrder);
    }

    @Override
    public void addLabels()
    {
        TextLabel.Builder builder = new TextLabel.Builder();
        builder.setText("hi");
        builder.setColor(Color.GREEN);
        builder.setFont(Font.SANS_SERIF + " PLAIN 12");
        GridLayoutConstraints constr = null;

        myMGRSTextLabel = new TextLabel(this, builder);
        myMGRSTextLabel.setVerticalAlignment(1f);
        myMGRSTextLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 0)));
        add(myMGRSTextLabel, constr);

        myLatTextLabel = new TextLabel(this, builder);
        myLatTextLabel.setVerticalAlignment(1f);
        myLatTextLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 1), new ScreenPosition(0, 1)));
        add(myLatTextLabel, constr);

        myLonTextLabel = new TextLabel(this, builder);
        myLonTextLabel.setVerticalAlignment(1f);
        myLonTextLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 2), new ScreenPosition(0, 2)));
        add(myLonTextLabel, constr);

        myAltTextLabel = new TextLabel(this, builder);
        myAltTextLabel.setVerticalAlignment(1f);
        myAltTextLabel.setHorizontalAlignment(.5f);
        constr = new GridLayoutConstraints(new ScreenBoundingBox(new ScreenPosition(0, 3), new ScreenPosition(0, 3)));
        add(myAltTextLabel, constr);
    }

    @Override
    public void addRenderable()
    {
        GridLayoutConstraints constr = new GridLayoutConstraints(
                new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(0, 4)));
        add(getInfoOverLayRenderable(), constr);
    }

    /**
     * Gets the alt label.
     *
     * @return the alt label
     */
    public TextLabel getAltLabel()
    {
        return myAltTextLabel;
    }

    @Override
    public Rectangle getBounds()
    {
        return getFrameLocation().asRectangle();
    }

    /**
     * Gets the lat label.
     *
     * @return the lat label
     */
    public TextLabel getLatLabel()
    {
        return myLatTextLabel;
    }

    /**
     * Gets the lon label.
     *
     * @return the lon label
     */
    public TextLabel getLonLabel()
    {
        return myLonTextLabel;
    }

    /**
     * Gets the mGRS label.
     *
     * @return the mGRS label
     */
    public TextLabel getMGRSLabel()
    {
        return myMGRSTextLabel;
    }

    @Override
    public void init()
    {
        initBorder();
        // set the layout
        setLayout(new GridLayout(1, 4, this));
        addLabels();
        addRenderable();
        getLayout().complete();
    }

    @Override
    public void repositionForInsets()
    {
    }
}
