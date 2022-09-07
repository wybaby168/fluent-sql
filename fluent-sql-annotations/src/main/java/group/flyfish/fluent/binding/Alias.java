package group.flyfish.fluent.binding;

import java.lang.annotation.*;

/**
 * 结果映射指定别名
 *
 * @author wangyu
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alias {

    /**
     * @return 别名
     */
    String value();
}
