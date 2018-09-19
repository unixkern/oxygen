package vip.justlive.oxygen.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常切面
 *
 * @author wubo
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Catching {

  /**
   * 增强的注解
   *
   * @return annotation
   */
  Class<? extends Annotation> annotation() default Annotation.class;
}
