package com.bitsys.common.http.auth;

import java.util.Objects;

import org.apache.http.auth.AuthScope;

/**
 * This class represents an authentication scope with which credentials apply.
 */
public class AuthenticationScope
{
   /** The host pattern that matches any host. */
   public static final String ANY_HOST = AuthScope.ANY_HOST;

   /** The port pattern that matches any port. */
   public static final int ANY_PORT = AuthScope.ANY_PORT;

   /** The realm pattern that matches any realm. */
   public static final String ANY_REALM = AuthScope.ANY_REALM;

   /** The scheme pattern that matches any scheme. */
   public static final String ANY_SCHEME = AuthScope.ANY_SCHEME;

   /** The delegated instance. */
   private final AuthScope authScope;

   /**
    * Constructs a {@linkplain AuthenticationScope} from the given host name and
    * port. The scope will apply to any realm and scheme.
    *
    * @param host
    *           the host name or {@link #ANY_HOST}.
    * @param port
    *           the port or {@link #ANY_PORT}.
    */
   public AuthenticationScope(final String host, final int port)
   {
      this(host, port, ANY_REALM);
   }

   /**
    * Constructs a {@linkplain AuthenticationScope} from the given host name,
    * port and realm. The scope will apply to any scheme.
    *
    * @param host
    *           the host name or {@link #ANY_HOST}.
    * @param port
    *           the port or {@link #ANY_PORT}.
    * @param realm
    *           the realm or {@link #ANY_REALM}.
    */
   public AuthenticationScope(final String host, final int port, final String realm)
   {
      this(host, port, realm, ANY_SCHEME);
   }

   /**
    * Constructs a {@linkplain AuthenticationScope} from the given host name,
    * port, realm and scheme.
    *
    * @param host
    *           the host name or {@link #ANY_HOST}.
    * @param port
    *           the port or {@link #ANY_PORT}.
    * @param realm
    *           the realm or {@link #ANY_REALM}.
    * @param scheme
    *           the scheme (e.g. http) or {@link #ANY_SCHEME}.
    */
   public AuthenticationScope(final String host, final int port, final String realm, final String scheme)
   {
      this(new AuthScope(host, port, realm, scheme));
   }

   /**
    * Constructs a {@linkplain AuthenticationScope} from an Apache
    * {@link AuthScope}.
    *
    * @param authScope
    *           the delegated instance.
    */
   AuthenticationScope(final AuthScope authScope)
   {
      if (authScope == null)
      {
         throw new IllegalArgumentException("The delegated instance cannot be null");
      }
      this.authScope = authScope;
   }

   /**
    * Returns the delegated Apache {@link AuthScope}.
    *
    * @return the Apache authentication scope.
    */
   AuthScope getAuthScope()
   {
      return authScope;
   }

   /**
    * Returns the host name.
    *
    * @return the host name.
    */
   public String getHost()
   {
      return authScope.getHost();
   }

   /**
    * Returns the port.
    *
    * @return the port.
    */
   public int getPort()
   {
      return authScope.getPort();
   }

   /**
    * Returns the realm.
    *
    * @return the realm.
    */
   public String getRealm()
   {
      return authScope.getRealm();
   }

   /**
    * Returns the scheme.
    *
    * @return the scheme.
    */
   public String getScheme()
   {
      return authScope.getScheme();
   }

   /**
    * Tests the other scope with the current one and determines how closely the
    * two match. A negative return value indicates that the two do not match at
    * all. The higher the number, the closer the match.
    *
    * @param other
    *           the other scope with which to test.
    * @return the degree of match. A negative number indicates no match. Higher
    *         numbers indicate a closer match.
    */
   public int match(final AuthenticationScope other)
   {
      return authScope.match(other.getAuthScope());
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return Objects.hash(getHost(), getPort(), getRealm(), getScheme());
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(final Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (getClass() != obj.getClass())
      {
         return false;
      }
      final AuthenticationScope other = (AuthenticationScope)obj;
      if (getHost() == null)
      {
         if (other.getHost() != null)
         {
            return false;
         }
      }
      else if (!getHost().equals(other.getHost()))
      {
         return false;
      }
      if (getPort() != other.getPort())
      {
         return false;
      }
      if (getRealm() == null)
      {
         if (other.getRealm() != null)
         {
            return false;
         }
      }
      else if (!getRealm().equals(other.getRealm()))
      {
         return false;
      }
      if (getScheme() == null)
      {
         if (other.getScheme() != null)
         {
            return false;
         }
      }
      else if (!getScheme().equals(other.getScheme()))
      {
         return false;
      }
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return authScope.toString();
   }
}
