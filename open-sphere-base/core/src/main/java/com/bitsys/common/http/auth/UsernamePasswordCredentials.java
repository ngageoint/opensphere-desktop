package com.bitsys.common.http.auth;

import java.security.Principal;

/**
 * This class is a {@link Credentials} that provides a basic user name and
 * password pair.
 */
public class UsernamePasswordCredentials implements Credentials
{
   /** The user's principal. */
   private final Principal userPrincipal;

   /** The password. */
   private final char[] password;

   /**
    * Constructs a new {@linkplain UsernamePasswordCredentials} with the given
    * user name and password. A clone of the password is stored.
    *
    * @param userName
    *           the user name.
    * @param password
    *           the password.
    */
   public UsernamePasswordCredentials(final String userName, final char[] password)
   {
      if (userName == null)
      {
         throw new IllegalArgumentException("The user name is null");
      }
      if (password == null)
      {
         throw new IllegalArgumentException("The password is null");
      }
      userPrincipal = new Principal()
      {
         @Override
         public String getName()
         {
            return userName;
         }
      };
      this.password = password.clone();
   }

   /**
    * Returns the user's principal.
    *
    * @return the user's principal.
    */
   @Override
   public Principal getUserPrincipal()
   {
      return userPrincipal;
   }

   /**
    * Returns the user name.
    *
    * @return the user name.
    */
   public String getUserName()
   {
      return getUserPrincipal().getName();
   }

   /**
    * Returns a clone of the password. The caller is expected to clear the
    * password.
    *
    * @return the password.
    */
   @Override
   public char[] getPassword()
   {
      return password.clone();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("UsernamePasswordCredentials [User Principal=");
      builder.append(getUserPrincipal());
      builder.append(", User Name=");
      builder.append(getUserName());
      builder.append(", Password=");
      builder.append(getPassword() != null);
      builder.append("]");
      return builder.toString();
   }
}
