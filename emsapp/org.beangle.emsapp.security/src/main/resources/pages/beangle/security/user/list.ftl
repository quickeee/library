[#ftl]
[@b.head/]
[#include "../status.ftl"/]
[@b.grid items=users var="user"]
	[@b.gridbar]
	bar.addItem("${b.text("action.new")}",action.add());
	bar.addItem("${b.text("action.modify")}",action.edit());
	bar.addItem("${b.text("action.freeze")}",activateUser('false'),'${b.theme.iconurl('actions/freeze.png')}');
	bar.addItem("${b.text("action.activate")}",activateUser('true'),'${b.theme.iconurl('actions/activate.png')}');
	bar.addItem("${b.text("action.delete")}",action.remove());
	bar.addItem("${b.text("action.export")}",exportUserList());
	function activateUser(isActivate){
		return action.multi("activate","确定提交?","isActivate="+isActivate);
	}
	function exportUserList(){
		var exp_properties="keys="+"name:登录名,fullname:姓名,mail:电子邮件,groups:用户组,creator.fullname:创建者,effectiveAt:账户生效时间,invalidAt:账户失效时间,passwordExpiredAt:密码失效时间,createdAt:创建时间,updatedAt:修改时间,enabled:状态";
		return action.export(exp_properties);
	}
	[/@]
	[@b.row]
		[@b.boxcol/]
		[@b.col property="name"]&nbsp;[@bs.avatar user=user/]&nbsp;[@b.a href="!dashboard?user.id=${user.id}" target="_blank"]${user.name}[/@][/@]
		[@b.col property="fullname"/]
		[@b.col property="mail" title="common.email" /]
		[@b.col property="creator.fullname"/]
		[@b.col property="defaultCategory.name" title="entity.userCategory"][#list user.categories as uc][#if uc!=user.defaultCategory]<em>${uc.name}</em>[#else]${uc.name}[/#if][#if uc_has_next]&nbsp;[/#if][/#list][/@]
		[@b.col property="updatedAt" title="common.updatedAt"]${user.updatedAt?string("yyyy-MM-dd")}[/@]
		[@b.col property="enabled" title="common.status"][@enableInfo user.enabled/][/@]
	[/@]
[/@]
[@b.foot/]