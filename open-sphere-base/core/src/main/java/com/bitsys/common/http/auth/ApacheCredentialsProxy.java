package com.bitsys.common.http.auth;

import java.security.Principal;
import java.util.Arrays;

import org.apache.http.auth.Credentials;

/**
 * This class is an Apache {@link Credentials} that delegates calls to a
 * {@link com.bitsys.common.http.auth.Credentials}.
 */
public class ApacheCredentialsProxy implements Credentials
{
   /** This library's credentials. */
   private final com.bitsys.common.http.auth.Credentials credentials;

   /**
    * Constructs a new <code>ApacheCredentialsProxy</code> from the given credentials.
    */
   public ApacheCredentialsProxy(final com.bitsys.common.http.auth.Credentials credentials)
   {
      if (credentials == null)
      {
         throw new IllegalArgumentException("The credentials are null");
      }
      this.credentials = credentials;
   }

   /**
    * @see org.apache.http.auth.Credentials#getUserPrincipal()
    */
   @Override
   public Principal getUserPrincipal()
   {
      return credentials.getUserPrincipal();
   }

   /**
    * @see org.apache.http.auth.Credentials#getPassword()
    */
   @Override
   public String getPassword()
   {
      final char[] password = credentials.getPassword();
      final String string = new String(password);
      Arrays.fill(password, '\0');
      return string;
   }
}
