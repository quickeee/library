/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.webapp.portal.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.beangle.security.auth.AuthenticationDetailsSource;
import org.beangle.security.auth.AuthenticationManager;
import org.beangle.security.auth.UsernamePasswordAuthentication;
import org.beangle.security.core.Authentication;
import org.beangle.security.core.AuthenticationException;
import org.beangle.security.core.context.AuthenticationUtils;
import org.beangle.security.core.context.SecurityContextHolder;
import org.beangle.security.web.session.SessionStrategy;
import org.beangle.struts2.action.BaseAction;

import com.octo.captcha.service.CaptchaService;
import com.octo.captcha.service.CaptchaServiceException;

public class LoginAction extends BaseAction implements ServletRequestAware {

	private HttpServletRequest request;

	private CaptchaService captchaService;

	private AuthenticationDetailsSource authenticationDetailsSource;

	private AuthenticationManager authenticationManager;

	private SessionStrategy sessionStrategy;
	
	public static final String LOGIN_FAILURE_COUNT = "loginFailureCount";

	public String index() {
		if (AuthenticationUtils.hasValidAuthentication()) { return "home"; }
		if (!shouldLogin()) { return "failure"; }
		String errorMsg = doLogin();
		if (StringUtils.isNotEmpty(errorMsg)) {
			addActionError(getText(errorMsg));
			increaseLoginFailure();
			return "failure";
		}
		clearLoginFailure();
		return "home";
	}

	protected boolean shouldLogin() {
		String username = get("username");
		String password = get("password");
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) { return false; }
		if (notFailEnough()) { return true; }
		// 校验验证码
		if (null != captchaService) {
			try {
				String sessionId = request.getSession().getId();
				Boolean valid = captchaService.validateResponseForID(sessionId, get("captcha"));
				if (Boolean.FALSE.equals(valid)) {
					addActionError(getText("error.captcha"));
					return false;
				}
			} catch (CaptchaServiceException e) {
				addActionError(getText("error.captcha"));
				return false;
			}
		}
		return true;
	}

	protected String doLogin() {
		String username = get("username");
		String password = get("password");
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) { return "failure"; }
		username = username.trim();
		UsernamePasswordAuthentication auth= new UsernamePasswordAuthentication(username,
				password);
		auth.setDetails(authenticationDetailsSource.buildDetails(request));
		Authentication authRequest =auth;
		try {
			authRequest= authenticationManager.authenticate(authRequest);
			sessionStrategy.onAuthentication(authRequest, request, null);
			SecurityContextHolder.getContext().setAuthentication(authRequest);
		} catch (AuthenticationException e) {
			return e.getMessage();
		}
		return null;
	}

	private boolean notFailEnough() {
		Integer loginFailureCount = (Integer) request.getSession()
				.getAttribute(LOGIN_FAILURE_COUNT);
		if (null == loginFailureCount) {
			loginFailureCount = Integer.valueOf(0);
		}
		if (loginFailureCount.intValue() <= 1) { return true; }
		return false;
	}

	private void increaseLoginFailure() {
		Integer loginFailureCount = (Integer) request.getSession()
				.getAttribute(LOGIN_FAILURE_COUNT);
		if (null == loginFailureCount) {
			loginFailureCount = Integer.valueOf(0);
		}
		loginFailureCount++;
		request.getSession().setAttribute(LOGIN_FAILURE_COUNT, loginFailureCount);
	}

	private void clearLoginFailure() {
		request.getSession().removeAttribute(LOGIN_FAILURE_COUNT);
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setCaptchaService(CaptchaService captchaService) {
		this.captchaService = captchaService;
	}

	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource authenticationDetailsSource) {
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public void setSessionStrategy(SessionStrategy sessionStrategy) {
		this.sessionStrategy = sessionStrategy;
	}

}