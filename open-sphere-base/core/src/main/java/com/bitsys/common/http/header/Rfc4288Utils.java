package com.bitsys.common.http.header;

/**
 * This class provides helper methods relating to RFC 4288.
 */
public final class Rfc4288Utils
{
   /**
    * Constructs a <code>Rfc4288Utils</code>. This constructor is private to
    * prevent instantiation.
    */
   private Rfc4288Utils()
   {
   }

   /**
    * Validates the name against the RFC 4288 definition of a
    * <code>"reg-name"</code>.
    *
    * @param name
    *           the name to validate.
    * @return <code>null</code> if the name is valid or the explanation for the
    *         failure.
    */
   public static String validateRegName(final CharSequence name)
   {
      String reason = null;
      if (name.length() == 0)
      {
         reason = "The name cannot be blank";
      }
      else if (name.length() > 127)
      {
         reason = "The name '" + name + "' cannot be more than 127 characters";
      }
      else
      {
         for (int ii = 0; ii < name.length(); ii++)
         {
            final char ch = name.charAt(ii);
            if (!isRegNameChar(ch))
            {
               reason =
                  "The name '" + name + "' contains an illegal character at index "
                     + ii + ": " + ch;
            }
         }
      }
      return reason;
   }

   /**
    * Returns <code>true</code> if the given character sequence falls under the
    * definitions of "<code>reg-name</code>" as defined in RFC 4288.
    *
    * @param name
    *           the name to test.
    * @return <code>true</code> if the name is a "<code>reg-name</code>".
    */
   public static boolean isRegName(final CharSequence name)
   {
      final String reason = validateRegName(name);
      return reason == null;
   }

   /**
    * Returns <code>true</code> if the given character falls under the
    * definition of "<code>reg-name-chars</code>" as defined in RFC 4288.
    *
    * @param ch
    *           the character to test.
    * @return <code>true</code> if the character is defined by "
    *         <code>reg-name-chars</code>".
    */
   public static boolean isRegNameChar(final char ch)
   {
      boolean isRegNameChar;
      switch (ch)
      {
      case '!':
      case '#':
      case '$':
      case '&':
      case '.':
      case '+':
      case '-':
      case '^':
      case '_':
         isRegNameChar = true;
         break;
      default:
         isRegNameChar =
            ch >= 'a' && ch <= 'z'
               || ch >= 'A' && ch <= 'Z'
               || ch >= '0' && ch <= '9';
         break;
      }
      return isRegNameChar;
   }
}
