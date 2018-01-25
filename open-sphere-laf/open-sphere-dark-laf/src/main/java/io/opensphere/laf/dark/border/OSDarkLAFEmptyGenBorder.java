package io.opensphere.laf.dark.border;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.plaf.UIResource;

@SuppressWarnings("serial")
public class OSDarkLAFEmptyGenBorder extends OSDarkLAFGenBorder implements UIResource
{
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        // Intentionally empty
    }
}
