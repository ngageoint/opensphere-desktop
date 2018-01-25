package io.opensphere.core.util.swing.input.controller;

import javax.swing.ImageIcon;

import io.opensphere.core.util.function.ConstantFunction;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;

/**
 * A function that always returns the same image icon.
 */
public class SingleIconProvider extends ConstantFunction<Object, ImageIcon>
{
    /**
     * Constructor.
     *
     * @param iconType The icon type.
     */
    public SingleIconProvider(IconType iconType)
    {
        super(IconUtil.getIcon(IconUtil.getResource(iconType)));
    }
}
