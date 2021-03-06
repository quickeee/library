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
package org.beangle.struts2.interceptor;

import java.util.Map;

import org.beangle.struts2.convention.Flash;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * ROR's flash
 * 
 * @author chaostone
 */
public class FlashInterceptor extends AbstractInterceptor {

  private static final long serialVersionUID = 8451445989084058881L;

  @Override
  public String intercept(ActionInvocation invocation) throws Exception {
    String result = invocation.invoke();
    try {
      Map<String, Object> session = invocation.getInvocationContext().getSession();
      if (null != session) {
        Flash flash = (Flash) session.get("flash");
        if (null != flash) flash.nextToNow();
      }
    } catch (IllegalStateException e) {
    }
    return result;
  }

}
