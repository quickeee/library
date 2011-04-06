/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.security.blueprint;

import static org.testng.Assert.assertEquals;

import org.beangle.security.blueprint.service.AuthorityService;
import org.beangle.security.blueprint.service.AuthorityServiceImpl;
import org.testng.annotations.Test;

@Test
public class AuthorityServiceImplTest {

	AuthorityService authorityService=new AuthorityServiceImpl();
	public void testextractResource() throws Exception {
		assertEquals(authorityService.extractResource("a.jsp"), "a");
		assertEquals(authorityService.extractResource("/b.do"), "/b");
		assertEquals(authorityService.extractResource("/c/d.action"), "/c/d");
		assertEquals(authorityService.extractResource("c/d.action"), "c/d");
		assertEquals(authorityService.extractResource("//c/d.action"), "//c/d");
		
		//assertEquals(authorityService.extractResource("  //c/d.action "), "c/d");
		//assertEquals(authorityService.extractResource("  c/d.action "), "c/d");

		assertEquals(authorityService.extractResource("c/d!remove.action "), "c/d");
	}
}
