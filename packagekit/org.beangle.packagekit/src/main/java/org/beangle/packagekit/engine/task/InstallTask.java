/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.packagekit.engine.task;

import org.beangle.packagekit.Resource;
import org.beangle.packagekit.engine.ResourceTask;
import org.beangle.packagekit.engine.UpdateAction;

public class InstallTask extends AbstractResourceTask implements ResourceTask {

	public InstallTask() {
		super();
	}

	public InstallTask(Resource resource) {
		super();
		this.resource = resource;
	}

	public void run() {
		System.out.println("install:" + resource.getId());
	}

	public UpdateAction getAction() {
		return UpdateAction.INSTALL;
	}

}
