package com.bitsys.common.http.client;

import java.util.Date;

import com.bitsys.common.http.auth.AuthenticationScope;

/**
 * This class specifies the various options for clearing cached data in an HTTP
 * client.
 */
public class ClearDataOptions
{
   /**
    * The optional the date before which the cache will <i>not</i> be cleared.
    */
   private Date clearSince;

   /**
    * The optional authentication scope for which cached data will be cleared.
    */
   private AuthenticationScope scope;

   /**
    * Indicates if saved passwords should be cleared.
    */
   private boolean clearSavedPasswords = false;

   /**
    * Constructs a <code>ClearDataOptions</code>.
    */
   public ClearDataOptions()
   {
   }

   /**
    * Constructs a <code>ClearDataOptions</code>. However, limits the clearing
    * to only data cached on or after the specified time. It further limits the
    * clearing to only data associated with the given authentication scope.
    *
    * @param clearSince
    *           the date before which the cache will <i>not</i> be cleared.
    * @param scope
    *           the authentication scope or <code>null</code>.
    */
   public ClearDataOptions(final Date clearSince, final AuthenticationScope scope)
   {
      this.clearSince = clearSince;
      this.scope = scope;
   }

   /**
    * Sets the date before which the cache will <i>not</i> be cleared. In other
    * words this option limits the clearing to only data that was cached on or
    * after this date.
    *
    * @param clearSince
    *           the date before which the cache will <i>not</i> be cleared or
    *           <code>null</code> to clear for all time.
    */
   public void setClearSince(final Date clearSince)
   {
      this.clearSince = clearSince;
   }

   /**
    * Returns the date before which the cache will <i>not</i> be cleared. Only
    * data that was cached on or after this date should be cleared.
    *
    * @return the date before which the cache will <i>not</i> be cleared or
    *         <code>null</code> to clear for all time.
    */
   public Date getClearSince()
   {
      return clearSince;
   }

   /**
    * Sets the authentication scope for limiting the cache clearing. If non-
    * <code>null</code>, only data associated with this scope will be cleared.
    *
    * @param scope
    *           the authentication scope or <code>null</code> if data should be
    *           cleared without regard to an authentication scope.
    */
   public void setScope(final AuthenticationScope scope)
   {
      this.scope = scope;
   }

   /**
    * Returns the authentication scope for which cached data will be cleared. If
    * non-<code>null</code>, only data associated with this scope will be
    * cleared.
    *
    * @return the authentication scope or <code>null</code> if data should be
    *         cleared without regard to an authentication scope.
    */
   public AuthenticationScope getScope()
   {
      return scope;
   }

   /**
    * Sets the clear saved passwords flag. If set, saved passwords will be
    * cleared.
    *
    * @param clearSavedPasswords
    *           indicates if saved passwords should be cleared.
    */
   public void setClearSavedPasswords(final boolean clearSavedPasswords)
   {
      this.clearSavedPasswords = clearSavedPasswords;
   }

   /**
    * Returns the state of the saved passwords flag.
    *
    * @return <code>true</code> if saved passwords should be cleared.
    */
   public boolean isClearSavedPasswords()
   {
      return clearSavedPasswords;
   }
}
