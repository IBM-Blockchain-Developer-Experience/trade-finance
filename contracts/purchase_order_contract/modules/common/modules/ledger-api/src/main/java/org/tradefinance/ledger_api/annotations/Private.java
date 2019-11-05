package org.tradefinance.ledger_api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public abstract @interface Private {

  public abstract java.lang.String collections() default "AnyOf('*')";
}
