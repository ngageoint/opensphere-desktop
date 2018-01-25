package io.opensphere.core.util.swing;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * An InfoPopupLabel which limits the size of the text on the label and sets the
 * info text to be the full text of the label.
 */
public class LimitedPopupLabel extends InfoPopupLabel
{
    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The maximum number of characters in the label text. */
    private final int myMaxLabelSize;

    /** The number of characters per line for the info text. */
    private final int myPopupLineLength;

    /**
     * Constructor.
     *
     * @param maxLabelSize The maximum number of characters in the label text.
     * @param popupLineLength The number of characters per line for the info
     *            text.
     */
    public LimitedPopupLabel(int maxLabelSize, int popupLineLength)
    {
        super();
        myMaxLabelSize = maxLabelSize;
        myPopupLineLength = popupLineLength;
    }

    @Override
    public void setText(String text)
    {
        if (text == null || text.length() <= myMaxLabelSize)
        {
            disableInfoPopup();
            super.setText(text);
            return;
        }

        enableInfoPopup();
        StringBuilder divided = new StringBuilder("<html>");
        divided.append(StringUtilities.addHTMLLineBreaks(text, myPopupLineLength));
        divided.append("</html>");
        setInfoText(divided.toString());
        super.setText(text.substring(0, myMaxLabelSize - 3) + "...");
    }
}
