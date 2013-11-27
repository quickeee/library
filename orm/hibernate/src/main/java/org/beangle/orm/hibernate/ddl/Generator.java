/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.orm.hibernate.ddl;

import static org.beangle.commons.lang.Strings.isNotBlank;
import static org.beangle.commons.lang.Strings.split;
import static org.beangle.commons.lang.Strings.substringAfter;
import static org.beangle.commons.lang.Strings.substringAfterLast;
import static org.beangle.commons.lang.Strings.substringBeforeLast;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.entity.comment.Messages;
import org.beangle.commons.entity.orm.AbstractPersistModule;
import org.beangle.commons.entity.orm.EntityPersistConfig;
import org.beangle.commons.io.IOs;
import org.beangle.commons.lang.ClassLoaders;
import org.beangle.commons.lang.Locales;
import org.beangle.commons.lang.SystemInfo;
import org.beangle.orm.hibernate.DefaultTableNamingStrategy;
import org.beangle.orm.hibernate.RailsNamingStrategy;
import org.beangle.orm.hibernate.internal.OverrideConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.mapping.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author chaostone
 * @version $Id: DdlGenerator.java Mar 22, 2012 9:45:57 AM chaostone $
 */
public class Generator {
  private Configuration configuration = null;
  private Dialect dialect;
  private List<String> tables = new ArrayList<String>(50);
  private List<String> sequences = new ArrayList<String>(50);
  private List<String> comments = new ArrayList<String>(50);
  private List<String> constraints = new ArrayList<String>(50);
  private List<String> indexes = new ArrayList<String>(50);
  private Messages messages;

  private String defaultCatalog;
  private String defaultSchema;

  private Mapping mapping;
  private Set<Table> processed = CollectUtils.newHashSet();

  private Map<String, List<List<String>>> files = CollectUtils.newHashMap();

  @SuppressWarnings("unchecked")
  public Generator(Dialect dialect, Locale locale) {
    super();
    this.dialect = dialect;
    this.messages = Messages.build(locale);
    files.put("1-tables.sql", Arrays.asList(tables, constraints, indexes));
    files.put("2-sequences.sql", Arrays.asList(sequences));
    files.put("3-comments.sql", Arrays.asList(comments));
  }

  /**
   * build configration
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void buildConfiguration() throws Exception {
    configuration = new OverrideConfiguration();
    configuration.getProperties().put(Environment.DIALECT, dialect);

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
        Generator.class.getClassLoader());

    // config naming strategy
    DefaultTableNamingStrategy tableNamingStrategy = new DefaultTableNamingStrategy();
    for (Resource resource : resolver.getResources("classpath*:META-INF/beangle/table.properties"))
      tableNamingStrategy.addConfig(resource.getURL());
    RailsNamingStrategy namingStrategy = new RailsNamingStrategy();
    namingStrategy.setTableNamingStrategy(tableNamingStrategy);
    configuration.setNamingStrategy(namingStrategy);

    for (Resource resource : resolver.getResources("classpath*:META-INF/hibernate.cfg.xml"))
      configuration.configure(resource.getURL());

    for (Resource resource : resolver.getResources("classpath*:META-INF/beangle/persist*.properties")) {
      InputStream is = resource.getURL().openStream();
      Properties props = new Properties();
      if (null != is) props.load(is);

      Object module = props.remove("module");
      if (null == module) continue;

      Class<? extends AbstractPersistModule> moduleClass = (Class<? extends AbstractPersistModule>) ClassLoaders
          .loadClass(module.toString());
      addPersistInfo(moduleClass.newInstance().getConfig());
      Enumeration<String> enumer = (Enumeration<String>) props.propertyNames();
      while (enumer.hasMoreElements()) {
        String propertyName = enumer.nextElement();
        configuration.setProperty(propertyName, props.getProperty(propertyName));
      }
      IOs.close(is);
    }
    defaultCatalog = configuration.getProperties().getProperty(Environment.DEFAULT_CATALOG);
    defaultSchema = configuration.getProperties().getProperty(Environment.DEFAULT_SCHEMA);
    configuration.buildMappings();
    mapping = configuration.buildMapping();
  }

  /**
   * Generate sql scripts
   * 
   * @param fileName
   * @param packageName
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void gen(String fileName, String packageName) throws Exception {
    buildConfiguration();
    // 1. first process class mapping
    Iterator<PersistentClass> iterpc = configuration.getClassMappings();
    while (iterpc.hasNext()) {
      PersistentClass pc = iterpc.next();
      Class<?> clazz = pc.getMappedClass();
      if (isNotBlank(packageName) && !clazz.getPackage().getName().startsWith(packageName)) continue;
      // add comment to table and column
      pc.getTable().setComment(messages.get(clazz, clazz.getSimpleName()));
      commentProperty(clazz, pc.getTable(), pc.getIdentifierProperty());
      commentProperties(clazz, pc.getTable(), pc.getPropertyIterator());
      // generator sequence sql
      if (pc instanceof RootClass) {
        IdentifierGenerator ig = pc.getIdentifier().createIdentifierGenerator(
            configuration.getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema,
            (RootClass) pc);
        if (ig instanceof PersistentIdentifierGenerator) {
          String[] lines = ((PersistentIdentifierGenerator) ig).sqlCreateStrings(dialect);
          sequences.addAll(Arrays.asList(lines));
        }
      }
      // generater table sql
      generateTableSql(pc.getTable());
    }

    // 2. process collection mapping
    Iterator<Collection> itercm = configuration.getCollectionMappings();
    while (itercm.hasNext()) {
      Collection col = itercm.next();
      if (isNotBlank(packageName) && !col.getRole().startsWith(packageName)) continue;
      // collection sequences
      if (col.isIdentified()) {
        IdentifierGenerator ig = ((IdentifierCollection) col).getIdentifier().createIdentifierGenerator(
            configuration.getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema, null);

        if (ig instanceof PersistentIdentifierGenerator) {
          String[] lines = ((PersistentIdentifierGenerator) ig).sqlCreateStrings(dialect);
          sequences.addAll(Arrays.asList(lines));
        }
      }
      // collection table
      if (!col.isOneToMany()) {
        Table table = col.getCollectionTable();
        String owner = col.getTable().getComment();
        Class<?> ownerClass = col.getOwner().getMappedClass();
        // resolved nested compoent name in collection's role
        String colName = substringAfter(col.getRole(), col.getOwnerEntityName() + ".");
        if (colName.contains(".")) ownerClass = getPropertyType(col.getOwner(),
            substringBeforeLast(colName, "."));
        table.setComment(owner + "-" + messages.get(ownerClass, substringAfterLast(col.getRole(), ".")));

        Column keyColumn = table.getColumn((Column) col.getKey().getColumnIterator().next());
        if (null != keyColumn) keyColumn.setComment(owner + " ID");

        if (col instanceof IndexedCollection) {
          IndexedCollection idxCol = (IndexedCollection) col;
          Value idx = idxCol.getIndex();
          if (idx instanceof ToOne) commentToOne((ToOne) idx, (Column) idx.getColumnIterator().next());
        }
        if (col.getElement() instanceof ManyToOne) {
          Column valueColumn = (Column) col.getElement().getColumnIterator().next();
          commentToOne((ManyToOne) col.getElement(), valueColumn);
        } else if (col.getElement() instanceof Component) {
          Component cp = (Component) col.getElement();
          commentProperties(cp.getComponentClass(), table, cp.getPropertyIterator());
        }

        generateTableSql(col.getCollectionTable());
      }
    }
    Set<String> commentSet = CollectUtils.newHashSet(comments);
    comments.clear();
    comments.addAll(commentSet);
    // 3. export to files
    for (String key : files.keySet()) {
      List<List<String>> sqls = files.get(key);
      FileWriter writer = new FileWriter(fileName + "/" + key, false);
      writes(writer, sqls);
      writer.flush();
      writer.close();
    }
  }

  /**
   * get component class by component property string
   * 
   * @param pc
   * @param propertyString
   * @return
   */
  private Class<?> getPropertyType(PersistentClass pc, String propertyString) {
    String[] properties = split(propertyString, '.');
    Property p = pc.getProperty(properties[0]);
    Component cp = ((Component) p.getValue());
    int i = 1;
    for (; i < properties.length; i++) {
      p = cp.getProperty(properties[i]);
      cp = ((Component) p.getValue());
    }
    return cp.getComponentClass();
  }

  private void commentToOne(ToOne toOne, Column column) {
    String entityName = toOne.getReferencedEntityName();
    PersistentClass referClass = configuration.getClassMapping(entityName);
    if (null != referClass) {
      column.setComment(referClass.getTable().getComment() + " ID");
    }
  }

  @SuppressWarnings("unchecked")
  private void commentProperty(Class<?> clazz, Table table, Property p) {
    if (null == p) return;
    if (p.getColumnSpan() == 1) {
      Column column = (Column) p.getColumnIterator().next();
      if (isForeignColumn(table, column)) {
        column.setComment(messages.get(clazz, p.getName()) + " ID");
      } else {
        column.setComment(messages.get(clazz, p.getName()));
      }
    } else if (p.getColumnSpan() > 1) {
      Component pc = ((Component) p.getValue());
      Class<?> columnOwnerClass = pc.getComponentClass();
      commentProperties(columnOwnerClass, table, pc.getPropertyIterator());
    }
  }

  private void commentProperties(Class<?> clazz, Table table, Iterator<Property> ip) {
    while (ip.hasNext())
      commentProperty(clazz, table, ip.next());
  }

  @SuppressWarnings("unchecked")
  private void generateTableSql(Table table) {
    if (!table.isPhysicalTable()) return;
    Iterator<String> commentIter = table.sqlCommentStrings(dialect, defaultCatalog, defaultSchema);
    while (commentIter.hasNext()) {
      comments.add(commentIter.next());
    }

    if (processed.contains(table)) return;
    processed.add(table);
    tables.add(table.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));

    Iterator<UniqueKey> subIter = table.getUniqueKeyIterator();
    while (subIter.hasNext()) {
      UniqueKey uk = subIter.next();
      String constraintString = uk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
      if (constraintString != null) constraints.add(constraintString);
    }

    Iterator<Index> idxIter = table.getIndexIterator();
    while (idxIter.hasNext()) {
      final Index index = idxIter.next();
      indexes.add(index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
    }

    if (dialect.hasAlterTable()) {
      Iterator<ForeignKey> fkIter = table.getForeignKeyIterator();
      while (fkIter.hasNext()) {
        ForeignKey fk = fkIter.next();
        if (fk.isPhysicalConstraint()) {
          constraints.add(fk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private boolean isForeignColumn(Table table, Column column) {
    Iterator<ForeignKey> fkIter = table.getForeignKeyIterator();
    while (fkIter.hasNext()) {
      ForeignKey fk = fkIter.next();
      if (fk.isPhysicalConstraint()) {
        if (fk.getColumns().contains(column)) return true;
      }
    }
    return false;
  }

  private void writes(Writer writer, List<List<String>> contentList) throws IOException {
    for (List<String> contents : contentList) {
      Collections.sort(contents);
      for (String script : contents) {
        writer.write(script);
        writer.write(";\n");
      }
    }
  }

  private void addPersistInfo(EntityPersistConfig epconfig) {
    for (EntityPersistConfig.EntityDefinition definition : epconfig.getEntites()) {
      configuration.addAnnotatedClass(definition.getClazz());
      if (null != definition.getCacheUsage()) {
        String region = (null == definition.getCacheRegion()) ? definition.getEntityName() : definition
            .getCacheRegion();
        configuration.setCacheConcurrencyStrategy(definition.getEntityName(), definition.getCacheUsage(),
            region, true);
      }
    }
    for (EntityPersistConfig.CollectionDefinition definition : epconfig.getCollections()) {
      if (null == definition.getCacheUsage()) continue;
      String role = epconfig.getEntity(definition.getClazz()).getEntityName() + "."
          + definition.getProperty();
      String region = (null == definition.getCacheRegion()) ? role : definition.getCacheRegion();
      configuration.setCollectionCacheConcurrencyStrategy(role, definition.getCacheUsage(), region);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.out.println("Usage: Generator org.hibernate.dialect.Oracle10gDialect /tmp zh_CN");
    }
    String dir = SystemInfo.getTmpDir();
    if (args.length > 2) dir = args[2];
    Locale locale = Locale.getDefault();
    if (args.length > 3) locale = Locales.toLocale(args[3]);
    String pattern = null;
    if (args.length > 4) pattern = args[4];
    Generator generator = new Generator((Dialect) ClassLoaders.loadClass(args[1]).newInstance(), locale);
    generator.gen(dir, pattern);
  }
}