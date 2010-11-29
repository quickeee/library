/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.security.blueprint.session;

import org.beangle.model.pojo.LongIdEntity;
import org.beangle.security.blueprint.UserCategory;

public interface CategoryProfile extends LongIdEntity {

	public SessionProfile getSessionProfile();

	public void setSessionProfile(SessionProfile sessionProfile);

	public UserCategory getCategory();

	public void setCategory(UserCategory category);

	public int getCapacity();

	public void setCapacity(int max);

	public int getInactiveInterval();

	public void setInactiveInterval(int inactiveInterval);

	public int getUserMaxSessions();

	public void setUserMaxSessions(int maxSessions);
}