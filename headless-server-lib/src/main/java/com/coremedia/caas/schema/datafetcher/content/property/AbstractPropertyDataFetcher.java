package com.coremedia.caas.schema.datafetcher.content.property;

import com.coremedia.caas.schema.datafetcher.content.AbstractContentDataFetcher;
import com.coremedia.caas.service.expression.FieldExpression;
import com.coremedia.caas.service.repository.content.ContentProxy;

import java.util.List;

import static com.coremedia.caas.service.repository.content.util.ContentUtil.isNullOrEmpty;

public abstract class AbstractPropertyDataFetcher extends AbstractContentDataFetcher {

  private List<FieldExpression<?>> fallbackExpressions;


  AbstractPropertyDataFetcher(FieldExpression<?> expression, List<FieldExpression<?>> fallbackExpressions) {
    super(expression);
    this.fallbackExpressions = fallbackExpressions;
  }


  protected <E> E getProperty(ContentProxy contentProxy, FieldExpression<?> expression, Class<E> targetClass) {
    // source name is a bean property/path expression
    E result = expression.fetch(contentProxy, targetClass);
    // check for fallback sources if result is empty
    if (fallbackExpressions != null && isNullOrEmpty(result)) {
      // iterate manually to skip possibly costly 'isNullOrEmpty' check on last element
      for (int i = 0, c = fallbackExpressions.size() - 1; i <= c; i++) {
        result = fallbackExpressions.get(i).fetch(contentProxy, targetClass);
        if (i == c || !isNullOrEmpty(result)) {
          break;
        }
      }
    }
    return result;
  }
}
