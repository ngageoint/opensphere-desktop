package com.bitsys.common.http.header;

/**
 * This class provides helper methods relating to RFC 1341.
 */
public final class Rfc1341Utils
{
   /**
    * Constructs a <code>Rfc1341Utils</code>. This constructor is private to
    * prevent instantiation.
    */
   private Rfc1341Utils()
   {
   }

   /**
    * Validates the token against the RFC 1341 definition of a "token".
    *
    * @param token
    *           the token to validate.
    * @return <code>null</code> if the token is valid or the explanation for the
    *         failure.
    */
   public static String validateToken(final CharSequence token)
   {
      String reason = null;

      // Ensure that the token is not blank.
      if (token.length() == 0)
      {
         reason = "The token '" + token + "' cannot be blank";
      }
      else
      {
         // Validate each character of the token.
         for (int ii = 0; ii < token.length(); ii++)
         {
            final char ch = token.charAt(ii);
            if (ch == ' ' || Character.isISOControl(ch) || isTspecial(ch))
            {
               reason =
                  "The token '" + token
                     + "' contains an illegal character at index " + ii + ": " + ch;
               break;
            }
         }
      }
      return reason;
   }

   /**
    * Returns <code>true</code> if the given character sequence fits the RFC
    * 1341 definition of a "token".
    *
    * @param token
    *           the token to test.
    * @return <code>true</code> if the given character sequence is a "token".
    */
   public static boolean isToken(final CharSequence token)
   {
      final String reason = validateToken(token);
      return reason == null;
   }

   /**
    * Returns <code>true</code> if the given character falls under the
    * definition of <code>"tspecials"</code> as defined in RFC 1341.
    *
    * @param ch
    *           the character to test.
    * @return <code>true</code> if the character is a <code>"tspecials"</code>
    *         character.
    */
   public static boolean isTspecial(final char ch)
   {
      boolean isTspecial;
      switch (ch)
      {
      case '/':
      case '?':
      case '=':
         isTspecial = true;
         break;
      default:
         isTspecial = Rfc822Utils.isSpecial(ch);
         break;
      }
      return isTspecial;
   }
}
