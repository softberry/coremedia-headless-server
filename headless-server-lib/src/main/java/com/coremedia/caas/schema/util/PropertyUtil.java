package com.coremedia.caas.schema.util;

import com.coremedia.caas.service.repository.content.ContentProxy;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PropertyUtil {

  public static boolean isNullOrEmpty(Object value) {
    return (value == null) ||
           (value instanceof String && ((String) value).isEmpty()) ||
           (value instanceof Collection && ((Collection) value).isEmpty()) ||
           (value instanceof Markup && MarkupUtil.isEmptyRichtext((Markup) value, true));
  }


  @SuppressWarnings("unchecked")
  public static <E> E get(Object source, String propertyPath) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    String nextExpression;
    // use default resolver for now
    DefaultResolver resolver = new DefaultResolver();
    // resolve parts until source expression is empty
    while ((nextExpression = resolver.next(propertyPath)) != null) {
      if (resolver.isMapped(nextExpression)) {
        // mapped expression will access a content property or map item
        if (source instanceof ContentProxy) {
          source = ((ContentProxy) source).get(resolver.getKey(nextExpression));
        }
        else if (source instanceof Map) {
          source = ((Map) source).get(resolver.getKey(nextExpression));
        }
        else {
          throw new IllegalAccessException("Source is not a content proxy or map");
        }
        if (source == null) {
          break;
        }
      }
      else if (resolver.isIndexed(nextExpression)) {
        int index = resolver.getIndex(nextExpression);
        // access list element of array element, abort expression evaluation on null result
        if (source instanceof List) {
          List list = (List) source;
          if (list.size() > index) {
            source = list.get(index);
          }
          else {
            source = null;
            break;
          }
        }
        else if (source instanceof Object[]) {
          Object[] array = (Object[]) source;
          if (array.length > index) {
            source = array[index];
          }
          else {
            source = null;
            break;
          }
        }
      }
      else {
        // simple bean property access, abort expression evaluation on null result
        String propertyName = resolver.getProperty(nextExpression);
        // special hardcoded self reference
        if (!"this".equals(propertyName)) {
          source = PropertyUtils.getSimpleProperty(source, propertyName);
        }
        if (source == null) {
          break;
        }
      }
      propertyPath = resolver.remove(propertyPath);
    }
    return (E) source;
  }
}
