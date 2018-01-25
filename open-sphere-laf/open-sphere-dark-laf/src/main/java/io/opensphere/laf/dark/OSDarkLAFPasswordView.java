package io.opensphere.laf.dark;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.JPasswordField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.Position;

public class OSDarkLAFPasswordView extends PasswordView
{
    protected static int wide = 9;

    protected static int gap = 3;

    public OSDarkLAFPasswordView(Element el)
    {
        super(el);
    }

    public int viewToModel(float fx, float fy, Shape aShape, Position.Bias[] bias)
    {
        bias[0] = Position.Bias.Forward;
        int n = 0;
        Container container = getContainer();

        if (container instanceof JPasswordField)
        {
            JPasswordField passField = (JPasswordField)container;
            if (!passField.echoCharIsSet())
            {
                return super.viewToModel(fx, fy, aShape, bias);
            }

            char echoChar = passField.getEchoChar();
            int width = passField.getFontMetrics(passField.getFont()).charWidth(echoChar);
            width = (width < wide ? wide : width) + gap;

            aShape = adjustAllocation(aShape);
            Rectangle allocationArea = (aShape instanceof Rectangle) ? (Rectangle)aShape : aShape.getBounds();
            n = ((int)fx - allocationArea.x) / width;
            if (n < 0)
            {
                n = 0;
            }
            else if (n > (getStartOffset() + getDocument().getLength()))
            {
                n = getDocument().getLength() - getStartOffset();
            }
        }

        return getStartOffset() + n;
    }

    public Shape modelToView(int position, Shape aShape, Position.Bias bias) throws BadLocationException
    {
        Container container = getContainer();
        if (container instanceof JPasswordField)
        {
            JPasswordField passwordField = (JPasswordField)container;
            if (!passwordField.echoCharIsSet())
            {
                return super.modelToView(position, aShape, bias);
            }

            char echoChar = passwordField.getEchoChar();
            int width = passwordField.getFontMetrics(passwordField.getFont()).charWidth(echoChar);
            width = (width < wide ? wide : width) + gap;

            Rectangle allocationArea = adjustAllocation(aShape).getBounds();
            int dx = (position - getStartOffset()) * width;
            allocationArea.x += dx - 2;
            if (allocationArea.x <= 5)
            {
                allocationArea.x = 6;
            }
            allocationArea.width = 1;

            return allocationArea;
        }

        return null;
    }

    protected int drawEchoCharacter(Graphics graph, int x, int y, char ch)
    {
        int charWidth = getFontMetrics().charWidth(ch);
        charWidth = (charWidth < wide ? wide : charWidth);
        int height = (getContainer().getHeight() - wide) / 2;

        Graphics2D graph2D = (Graphics2D)graph;
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph2D.fillOval(x, height + 1, charWidth, charWidth);
        graph2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

        return x + charWidth + gap;
    }
}
