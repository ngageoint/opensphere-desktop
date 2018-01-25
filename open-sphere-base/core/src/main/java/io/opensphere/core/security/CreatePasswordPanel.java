package io.opensphere.core.security;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.Validatable;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.ValidatorSupport;
import io.opensphere.core.util.swing.GridBagPanel;

/**
 * A panel used to enter a new password that requires retyping the password and
 * may enforce password requirements.
 */
public class CreatePasswordPanel extends JPanel implements Validatable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The minimum character categories that must be present in the password.
     */
    private final int myMinPasswordCharacterCategories;

    /** The minimum password length. */
    private final int myMinPasswordLength;

    /** The first password field. */
    private JPasswordField myNewPasswordField;

    /** The retype password field. */
    private JPasswordField myRetypePasswordField;

    /** Support for validation. */
    private final DefaultValidatorSupport myValidatorSupport = new DefaultValidatorSupport(this);

    /**
     * Construct the password panel.
     *
     * @param minPasswordLength The minimum password length.
     * @param minPasswordCharacterCategories The minimum character categories
     *            that must be present in the password.
     */
    public CreatePasswordPanel(int minPasswordLength, int minPasswordCharacterCategories)
    {
        super(new BorderLayout());
        myMinPasswordLength = minPasswordLength;
        myMinPasswordCharacterCategories = minPasswordCharacterCategories;
        initialize();
    }

    /**
     * Get the password from the password field. For better security the array
     * should be cleared after the password is used.
     *
     * @return The password.
     */
    public char[] getPassword()
    {
        return myNewPasswordField.getPassword();
    }

    @Override
    public ValidatorSupport getValidatorSupport()
    {
        return myValidatorSupport;
    }

    /**
     * Get the number of distinct character categories in a character array.
     *
     * @param newPasswordArray The array.
     * @return The number of distinct character categories.
     */
    private int getCharacterCategoryCount(char[] newPasswordArray)
    {
        int lowerCase = 0;
        int upperCase = 0;
        int symbol = 0;
        int digit = 0;

        for (int index = 0; index < newPasswordArray.length; ++index)
        {
            if (Character.isDigit(newPasswordArray[index]))
            {
                digit = 1;
            }
            else if (Character.isLowerCase(newPasswordArray[index]))
            {
                lowerCase = 1;
            }
            else if (Character.isUpperCase(newPasswordArray[index]))
            {
                upperCase = 1;
            }
            else
            {
                symbol = 1;
            }
        }
        return lowerCase + upperCase + symbol + digit;
    }

    /**
     * Initialize the display.
     */
    private void initialize()
    {
        GridBagPanel passwordPanel = new GridBagPanel();
        add(passwordPanel);

        // @formatter:off
        StringBuffer strBuf = new StringBuffer(278);
        strBuf
                .append("Your password must be a minimum of ")
                .append(myMinPasswordLength)
                .append(" characters and meet any ")
                .append(myMinPasswordCharacterCategories)
                .append(" of the following 4 requirements:\n"
                        + "      -Contain at least one symbol\n"
                        + "      -Contain at least one number [0-9]\n"
                        + "      -Contain at least one lower case letter [a-z]\n"
                        + "      -Contain at least one upper case letter [A-Z]");
        // @formatter:on

        JTextArea description = new JTextArea(0, 30);
        description.setBorder(new EmptyBorder(10, 10, 20, 10));
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setOpaque(false);
        description.setWrapStyleWord(true);
        description.setText(strBuf.toString());

        passwordPanel.fillHorizontal().setGridwidth(2);
        passwordPanel.addRow(description);

        passwordPanel.style("label");
        passwordPanel.style("input").fillHorizontal();
        myNewPasswordField = new JPasswordField(15);
        passwordPanel.style("label", "input").addRow(new JLabel("Master Password:"), myNewPasswordField);
        myRetypePasswordField = new JPasswordField(15);
        passwordPanel.style("label", "input").addRow(new JLabel("Retype Password:"), myRetypePasswordField);

        KeyAdapter keyListener = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                verifyPassword();
            }
        };
        myNewPasswordField.addKeyListener(keyListener);
        myRetypePasswordField.addKeyListener(keyListener);
    }

    /**
     * Helper method to verify passwords.
     */
    private void verifyPassword()
    {
        boolean valid;

        char[] newPasswordArray = myNewPasswordField.getPassword();
        try
        {
            String message;
            if (newPasswordArray.length < myMinPasswordLength)
            {
                myNewPasswordField.setForeground(Color.RED);
                myRetypePasswordField.setForeground(Color.RED);
                message = "Password must be at least " + myMinPasswordLength + " characters";
                valid = false;
            }
            else if (getCharacterCategoryCount(newPasswordArray) < myMinPasswordCharacterCategories)
            {
                myNewPasswordField.setForeground(Color.RED);
                myRetypePasswordField.setForeground(Color.RED);
                message = "Password must have at least " + myMinPasswordCharacterCategories + " character categories";
                valid = false;
            }
            else
            {
                myNewPasswordField.setForeground(Color.GREEN);
                boolean match;
                char[] reEnterPasswordArray = myRetypePasswordField.getPassword();
                try
                {
                    match = Arrays.equals(newPasswordArray, reEnterPasswordArray);
                }
                finally
                {
                    Arrays.fill(reEnterPasswordArray, '\0');
                }

                if (match)
                {
                    myRetypePasswordField.setForeground(Color.GREEN);
                    message = "Passwords are valid";
                    valid = true;
                }
                else
                {
                    myRetypePasswordField.setForeground(Color.RED);
                    message = "Passwords do not match";
                    valid = false;
                }
            }

            ValidationStatus status = ValidationStatus.VALID;

            if (!valid)
            {
                status = ValidationStatus.ERROR;
            }

            myValidatorSupport.setValidationResult(status, message);
        }
        finally
        {
            Arrays.fill(newPasswordArray, '\0');
        }
    }
}
