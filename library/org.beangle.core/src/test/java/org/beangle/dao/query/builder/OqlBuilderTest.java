/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.dao.query.builder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.beangle.dao.query.LimitQuery;
import org.beangle.dao.query.TestModel;
import org.testng.annotations.Test;

@Test
public class OqlBuilderTest {

	public void testToCountString1() throws Exception {
		OqlBuilder<TestModel> queryBuilder = OqlBuilder.from(TestModel.class, "model");
		LimitQuery<?> query = (LimitQuery<?>) queryBuilder.build();
		assertEquals("select count(*) from org.beangle.dao.query.TestModel model", query.getCountQuery()
				.getStatement());
		queryBuilder.where(new Condition("name like :name", "testName"));
		query = (LimitQuery<?>) queryBuilder.build();
		assertTrue(query.getCountQuery().getStatement().endsWith("(name like :name)"));
	}

	public void testToCountString2() throws Exception {
		OqlBuilder<Object> queryBuilder = OqlBuilder
				.hql("from Ware where price is not null order by releaseDate desc "
						+ " union all from Ware where price is null order by releaseDate desc");
		LimitQuery<Object> query = (LimitQuery<Object>) queryBuilder.build();
		assertNull(query.getCountQuery());
	}

}