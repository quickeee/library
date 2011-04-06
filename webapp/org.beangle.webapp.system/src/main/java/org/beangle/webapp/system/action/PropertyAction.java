/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.webapp.system.action;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.property.PropertyConfigFactory;
import org.beangle.model.query.builder.OqlBuilder;
import org.beangle.struts2.action.BaseAction;
import org.beangle.webapp.system.model.PropertyConfigItemBean;

public class PropertyAction extends BaseAction {

	private PropertyConfigFactory configFactory;

	public String index() {
		OqlBuilder<PropertyConfigItemBean> builder = OqlBuilder.from(PropertyConfigItemBean.class, "config");
		builder.orderBy("config.name");
		List<PropertyConfigItemBean> configs = entityDao.search(builder);
		put("propertyConfigs", configs);
		Set<String> staticNames = configFactory.getConfig().getNames();
		for (PropertyConfigItemBean config : configs) {
			staticNames.remove(config.getName());
		}
		put("config", configFactory.getConfig());
		put("staticNames", staticNames);
		return forward();
	}

	public String save() {
		List<PropertyConfigItemBean> configs = entityDao.getAll(PropertyConfigItemBean.class);
		Set<String> names = CollectUtils.newHashSet();
		for (PropertyConfigItemBean config : configs) {
			populate(config, "config" + config.getId());
			names.add(config.getName());
		}
		entityDao.saveOrUpdate(configs);

		String msg = "info.save.success";
		PropertyConfigItemBean newConfig = populate(PropertyConfigItemBean.class, "configNew");
		if (StringUtils.isNotBlank(newConfig.getName()) && StringUtils.isNotBlank(newConfig.getValue())
				&& !names.contains(newConfig.getName())) {
			entityDao.saveOrUpdate(newConfig);
		}
		configFactory.reload();
		return redirect("index", msg);
	}

	public String remove() {
		PropertyConfigItemBean config = entityDao.get(PropertyConfigItemBean.class, getLong("config.id"));
		if (null != config) entityDao.remove(config);
		return redirect("index", "info.save.success");
	}

	public void setConfigFactory(PropertyConfigFactory configFactory) {
		this.configFactory = configFactory;
	}

}
