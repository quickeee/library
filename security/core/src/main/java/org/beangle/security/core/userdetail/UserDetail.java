/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.security.core.userdetail;

import java.io.Serializable;
import java.util.Collection;

import org.beangle.security.core.GrantedAuthority;

/**
 * Provides core user information.
 */
public interface UserDetail extends Serializable {
  /**
   * Returns the username used to authenticate the user. Cannot return <code>null</code>.
   * 
   * @return the username (never <code>null</code>)
   */
  String getUsername();

  /**
   * Returns the password used to authenticate the user. Cannot return <code>null</code>.
   * 
   * @return the password (never <code>null</code>)
   */
  String getPassword();

  /**
   * Returns the authorities granted to the user. Cannot return <code>null</code>.
   * 
   * @return the authorities, sorted by natural key (never <code>null</code>)
   */
  Collection<? extends GrantedAuthority> getAuthorities();

  /**
   * Indicates whether the user's account has expired. An expired account
   * cannot be authenticated.
   * 
   * @return <code>true</code> if the user's account is valid (ie
   *         non-expired), <code>false</code> if no longer valid (ie expired)
   */
  boolean isAccountExpired();

  /**
   * Indicates whether the user is locked or unlocked. A locked user cannot be
   * authenticated.
   * 
   * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
   */
  boolean isAccountLocked();

  /**
   * Indicates whether the user's credentials (password) has expired. Expired
   * credentials prevent authentication.
   * 
   * @return <code>true</code> if the user's credentials are valid (ie
   *         non-expired), <code>false</code> if no longer valid (ie expired)
   */
  boolean isCredentialsExpired();

  /**
   * Indicates whether the user is enabled or disabled. A disabled user cannot
   * be authenticated.
   * 
   * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
   */
  boolean isEnabled();
}
