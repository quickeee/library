/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.security.blueprint.service;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.beangle.security.blueprint.UserCategory;
import org.beangle.security.core.GrantedAuthority;
import org.beangle.security.core.userdetail.User;
import org.beangle.security.web.session.category.CategoryPrincipal;

public class UserToken extends User implements CategoryPrincipal {

	private Long id;

	private String fullname;
	/** 用户类别 */
	private UserCategory category;

	public UserToken(Long id, String username, String fullname, String password,
			UserCategory category, boolean enabled, boolean accountExpired,
			boolean credentialsExpired, boolean accountLocked, GrantedAuthority[] authorities)
			throws IllegalArgumentException {
		super(username, password, enabled, accountExpired, credentialsExpired, accountLocked,
				authorities);
		this.id = id;
		this.fullname = fullname;
		this.category = category;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public UserCategory getCategory() {
		return category;
	}

	public void setCategory(UserCategory category) {
		this.category = category;
	}

	public void changeCategory(Object newCategory) {
		Validate.isTrue(newCategory instanceof UserCategory, 
				"newCategory should be instanceof UserCategory");
		this.category = (UserCategory) newCategory;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-64900959, -454788261).append(this.id).toHashCode();
	}

	/**
	 * 比较id,如果任一方id是null,则不相等
	 */
	public boolean equals(final Object object) {
		if (!(object instanceof UserToken)) { return false; }
		UserToken rhs = (UserToken) object;
		if (null == getId() || null == rhs.getId()) { return false; }
		return getId().equals(rhs.getId());
	}
}