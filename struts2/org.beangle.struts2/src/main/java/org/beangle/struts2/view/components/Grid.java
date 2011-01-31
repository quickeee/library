/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.struts2.view.components;

import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.xwork.ObjectUtils;
import org.apache.commons.lang.xwork.StringUtils;
import org.apache.struts2.util.MakeIterator;
import org.apache.struts2.util.TextProviderHelper;
import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.collection.page.Page;
import org.beangle.commons.lang.StrUtils;

import com.opensymphony.xwork2.util.ValueStack;

/**
 * Data table
 * 
 * <pre>
 * performance column rendering
 * filter  FIXME
 * </pre>
 * 
 * @author chaostone
 */
public class Grid extends ClosingUIBean {
	final private static transient Random RANDOM = new Random();
	private List<Col> cols = CollectUtils.newArrayList();
	private Set<Object> colNames = CollectUtils.newHashSet();
	private Object items;
	private String var;
	// gridbar
	private String bar;
	private String sortable="true";

	public Grid(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
		super(stack, req, res);
	}

	public boolean getHasbar() {
		return (null != bar || items instanceof Page);
	}

	public boolean isPageable(){
		return items instanceof Page<?>; 
	}
	
	public boolean isNotFullPage(){
		return ((Page<?>)items).size()<((Page<?>)items).getPageSize();
	}
	public String defaultSort(String property) {
		return StrUtils.concat(var, ".", property);
	}

	public boolean isSortable(Col cln) {
		String sortby = (String) cln.getParameters().get("sort");
		if (null != sortby) return true;
		return ("true".equals(sortable)
				&& !ObjectUtils.equals(cln.getParameters().get("sortable"), "false") && null != cln
				.getProperty());
	}

	protected void addCol(Col column) {
		Object name = column.getName();
		if (null == name) {
			name = column.getProperty();
		}
		if (!colNames.contains(name)) {
			colNames.add(name);
			cols.add(column);
		}
	}

	public boolean start(Writer writer) {
		if (StringUtils.isEmpty(this.id)) {
			int nextInt = RANDOM.nextInt();
			nextInt = (nextInt == Integer.MIN_VALUE) ? Integer.MAX_VALUE : Math.abs(nextInt);
			this.id = "grid_" + String.valueOf(nextInt);
		}
		return true;
	}

	public List<Col> getCols() {
		return cols;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

	public void setItems(Object datas) {
		this.items = datas;
	}

	public Object getItems() {
		return items;
	}

	public String getSortable() {
		return sortable;
	}

	public void setSortable(String sortable) {
		this.sortable = sortable;
	}

	public String getBar() {
		return bar;
	}

	public static class Bar extends ClosingUIBean {
		private Grid table;

		public Bar(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
			super(stack, req, res);
			table = (Grid) findAncestor(Grid.class);
		}

		@Override
		public boolean end(Writer writer, String body) {
			table.bar = body;
			return false;
		}

		public Grid getTable() {
			return table;
		}
	}

	public static class Row extends IterableUIBean {
		private Grid table;
		private String var_index;
		private Iterator<?> iterator;
		private int index = -1;
		private Object obj;
		private Boolean hasInnerTr;

		public Row(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
			super(stack, req, res);
			table = (Grid) findAncestor(Grid.class);
			Object iteratorTarget = table.items;
			if (table.items instanceof String) {
				iteratorTarget = findValue((String) table.items);
			}
			iterator = MakeIterator.convert(iteratorTarget);
			this.var_index = table.var + "_index";
		}

		public Boolean hasInnerTr(String nestedBody) {
			if (null != hasInnerTr) return hasInnerTr;
			hasInnerTr = StringUtils.contains(nestedBody, "<tr");
			return hasInnerTr;
		}

		@Override
		protected boolean next() {
			if (iterator != null && iterator.hasNext()) {
				index++;
				obj = iterator.next();
				stack.getContext().put(table.var, obj);
				stack.getContext().put(var_index, index);
				return true;
			} else {
				stack.getContext().remove(table.var);
				stack.getContext().remove(var_index);
			}
			return false;
		}
	}

	/**
	 * @author chaostone
	 */
	public static class Col extends ClosingUIBean {
		String property;
		String name;
		String width;
		Row row;

		public Col(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
			super(stack, req, res);
		}

		@Override
		public boolean start(Writer writer) {
			row = (Row) findAncestor(Row.class);
			if(row.index==0){
				row.table.addCol(this);
			}
			return true;
		}

		public String getProperty() {
			return property;
		}

		public void setProperty(String property) {
			this.property = property;
		}

		/**
		 * find value of row.obj's property
		 * 
		 * @return
		 */
		public Object getValue() {
			stack.push(row.obj);
			try {
				return stack.findValue(property);
			} finally {
				stack.pop();
			}
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			if (null == name) {
				name = StrUtils.concat(row.table.var, ".", property);
			}
			if (-1 == name.indexOf('.')) {
				return name;
			} else {
				return TextProviderHelper
						.getText(name, name, Collections.emptyList(), stack, false);
			}
		}

		public String getWidth() {
			return width;
		}

		public void setWidth(String width) {
			this.width = width;
		}
		
	}

	public static class Boxcol extends Col {

		public Boxcol(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
			super(stack, req, res);
		}

		String type = "checkbox";
		// checkbox or radiobox name
		String boxname = null;
		boolean checked;
		
		@Override
		public boolean start(Writer writer) {
			row = (Row) findAncestor(Row.class);
			if(row.index==0){
				row.table.addCol(this);
			}
			if (null == boxname) {
				boxname = row.table.var + "." + property;
			}
			if(null==property){
				this.property="id";
			}
			return true;
		}

		public String getType() {
			return type;
		}

		public String getBoxname() {
			return boxname;
		}

		public void setBoxname(String boxname) {
			this.boxname = boxname;
		}

		public void setType(String type) {
			this.type = type;
		}

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}
		
	}
}