package group.flyfish.framework.entity;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Property {

    /**
     * @return 属性名称
     */
    String value();

    /**
     * @return 属性描述
     */
    String description() default "";

    /**
     * @return 是否唯一
     */
    boolean unique() default false;
}
