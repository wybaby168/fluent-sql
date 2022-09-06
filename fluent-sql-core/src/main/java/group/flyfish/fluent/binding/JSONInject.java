package group.flyfish.fluent.binding;

import java.lang.annotation.*;

/**
 * json注入
 *
 * @author wangyu
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JSONInject {
}
