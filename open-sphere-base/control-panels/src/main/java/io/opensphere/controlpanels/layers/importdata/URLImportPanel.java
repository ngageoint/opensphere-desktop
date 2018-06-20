package io.opensphere.controlpanels.layers.importdata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;

/**
 * The Class URLImportPanel.
 */
@SuppressWarnings("serial")
public class URLImportPanel extends JPanel
{
    /** The url text field. */
    private final JTextField myURLTextField;

    /** The validate message label. */
    private final JLabel myValidateMessageLabel;

    /**
     * Instantiates a new uRL import panel.
     *
     * @param url the url
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public URLImportPanel(String url)
    {
        myURLTextField = new JTextField();
        if (url != null)
        {
            myURLTextField.setText(url);
        }
        myURLTextField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                validateURL(myURLTextField.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                myValidateMessageLabel.setVisible(true);
                validateURL(myURLTextField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                myValidateMessageLabel.setVisible(true);
                validateURL(myURLTextField.getText());
            }
        });

        myValidateMessageLabel = new JLabel("VALID");
        myValidateMessageLabel.setForeground(Color.green);
        myValidateMessageLabel.setVisible(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        add(new JLabel("Enter URL "), BorderLayout.WEST);

        add(myURLTextField, BorderLayout.CENTER);

        Box messageBox = Box.createHorizontalBox();
        messageBox.setMinimumSize(new Dimension(10, 30));
        messageBox.setPreferredSize(new Dimension(10, 30));
        messageBox.add(Box.createHorizontalStrut(70));
        messageBox.add(myValidateMessageLabel);
        add(messageBox, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(800, 80));
        setPreferredSize(new Dimension(800, 80));

        validateURL(myURLTextField.getText());
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     */
    public URL getURL()
    {
        URL url;
        try
        {
            url = new URL(myURLTextField.getText());
        }
        catch (MalformedURLException e)
        {
            url = null;
        }
        return url;
    }

    /**
     * Gets the uRL text.
     *
     * @return the uRL text
     */
    public String getURLText()
    {
        return myURLTextField.getText();
    }

    /**
     * Checks if is uRL valid.
     *
     * @return true, if is uRL valid
     */
    public boolean isURLValid()
    {
        return validateURL(myURLTextField.getText());
    }

    /**
     * Sets the validation message.
     *
     * @param valid the valid
     * @param message the message
     */
    public void setValidationMessage(boolean valid, String message)
    {
        if (valid)
        {
            myValidateMessageLabel.setForeground(Color.green);
        }
        else
        {
            myValidateMessageLabel.setForeground(Color.red);
        }
        myValidateMessageLabel.setText(message);
    }

    /**
     * Validate url.
     *
     * @param url the url
     * @return true, if successful
     */
    @SuppressWarnings("unused")
    public boolean validateURL(String url)
    {
        boolean valid = !StringUtils.isBlank(url);
        if (!valid)
        {
            setValidationMessage(valid, "URL can not be empty.");
        }
        else
        {
            try
            {
                new URL(url);
                setValidationMessage(valid, "VALID");
            }
            catch (MalformedURLException e)
            {
                valid = false;
                setValidationMessage(valid, "NOT VALID: " + e.getMessage());
            }
        }
        return valid;
    }
}
