/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.entity.util;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtils;
import org.beangle.commons.collection.CollectUtils;
import org.beangle.commons.entity.Entity;
import org.beangle.commons.entity.pojo.TemporalActiveEntity;
import org.beangle.commons.lang.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实体类辅助工具箱
 * 
 * @author chaostone 2005-10-31
 * @version $Id: $
 */
public final class EntityUtils {

  private static final Logger logger = LoggerFactory.getLogger(EntityUtils.class);

  private EntityUtils() {
  }

  /**
   * <p>
   * extractIds.
   * </p>
   * 
   * @param entities a {@link java.util.Collection} object.
   * @param <T> a T object.
   * @return a {@link java.util.List} object.
   */
  public static <T extends Entity<?>> List<?> extractIds(Collection<T> entities) {
    List<Object> idList = CollectUtils.newArrayList();
    for (Iterator<T> iter = entities.iterator(); iter.hasNext();) {
      Entity<?> element = iter.next();
      try {
        idList.add(PropertyUtils.getProperty(element, "id"));
      } catch (Exception e) {
        logger.error("getProperty error", e);
        continue;
      }
    }
    return idList;
  }

  /**
   * <p>
   * getCommandName.
   * </p>
   * 
   * @param clazz a {@link java.lang.Class} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getCommandName(Class<?> clazz) {
    String name = clazz.getName();
    return Strings.uncapitalize(name.substring(name.lastIndexOf('.') + 1));
  }

  /**
   * <p>
   * getCommandName.
   * </p>
   * 
   * @param entityName a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getCommandName(String entityName) {
    return Strings.uncapitalize(Strings.substringAfterLast(entityName, "."));
  }

  /**
   * <p>
   * getCommandName.
   * </p>
   * 
   * @param obj a {@link java.lang.Object} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getCommandName(Object obj) {
    String name = obj.getClass().getName();
    int dollar = name.indexOf('$');
    if (-1 == dollar) {
      name = name.substring(name.lastIndexOf('.') + 1);
    } else {
      name = name.substring(name.lastIndexOf('.') + 1, dollar);
    }
    return Strings.uncapitalize(name);
  }

  /**
   * <p>
   * extractIdSeq.
   * </p>
   * 
   * @param entities a {@link java.util.Collection} object.
   * @param <T> a T object.
   * @return a {@link java.lang.String} object.
   */
  public static <T extends Entity<?>> String extractIdSeq(Collection<T> entities) {
    if (null == entities || entities.isEmpty()) { return ""; }
    StringBuilder idBuf = new StringBuilder(",");
    for (Iterator<T> iter = entities.iterator(); iter.hasNext();) {
      T element = iter.next();
      try {
        idBuf.append(PropertyUtils.getProperty(element, "id"));
        idBuf.append(',');
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    return idBuf.toString();
  }

  /**
   * 判断实体类中的属性是否全部为空
   * 
   * @param entity a {@link org.beangle.commons.dao.Entity} object.
   * @param ignoreDefault
   *          忽略数字和字符串的默认值
   * @return a boolean.
   */
  @SuppressWarnings("unchecked")
  public static boolean isEmpty(Entity<?> entity, boolean ignoreDefault) {
    BeanMap map = new BeanMap(entity);
    List<String> attList = CollectUtils.newArrayList();
    attList.addAll(map.keySet());
    attList.remove("class");
    try {
      for (final String attr : attList) {
        if (!PropertyUtils.isWriteable(entity, attr)) {
          continue;
        }
        Object value = map.get(attr);
        if (null == value) {
          continue;
        }
        if (ignoreDefault) {
          if (value instanceof Number) {
            if (((Number) value).intValue() != 0) { return false; }
          } else if (value instanceof String) {
            String str = (String) value;
            if (Strings.isNotEmpty(str)) { return false; }
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    } catch (Exception e) {
      logger.error("isEmpty error", e);
    }
    return true;
  }

  /**
   * 为了取出CGLIB代来的代理命名
   * 
   * @param clazz a {@link java.lang.Class} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getEntityClassName(Class<?> clazz) {
    String name = clazz.getName();
    int dollar = name.indexOf('$');
    if (-1 == dollar) {
      return name;
    } else {
      return name.substring(0, dollar);
    }
  }

  /**
   * <p>
   * isExpired.
   * </p>
   * 
   * @param entity a {@link org.beangle.commons.entity.pojo.TemporalActiveEntity} object.
   * @return a boolean.
   */
  public static boolean isExpired(TemporalActiveEntity entity) {
    Date now = new Date();
    if (null == entity.getEffectiveAt()) return true;
    return entity.getEffectiveAt().after(now)
        || (null != entity.getInvalidAt() && !now.before(entity.getInvalidAt()));
  }

}
