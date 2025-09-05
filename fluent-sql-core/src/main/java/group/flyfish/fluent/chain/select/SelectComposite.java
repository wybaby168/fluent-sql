package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.utils.context.AliasComposite;
import group.flyfish.fluent.utils.sql.EntityNameUtils;
import group.flyfish.fluent.utils.sql.SFunction;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 选择语句泛型包装
 *
 * @author wangyu
 */
@FunctionalInterface
@SuppressWarnings("unchecked")
public interface SelectComposite extends SQLSegment {

    /**
     * 基于泛型的包装，以实体类为T，可以组合任意字段集合
     *
     * @param getter 字段属性getter
     * @param <T>    实体泛型
     * @return 包装集合
     */
    @SafeVarargs
    static <T> SelectComposite composite(SFunction<T, ?>... getter) {
        return () -> columns(getter);
    }

    /**
     * 基于泛型的包装，以实体类为T，可以组合任意字段集合，返回字段名称
     *
     * @param <T>    实体泛型
     * @param getter 字段属性getter
     * @return 包装集合
     */
    @SafeVarargs
    static <T> SelectComposite names(SFunction<T, ?>... getter) {
        return () -> Arrays.stream(getter).map(SFunction::getName).collect(Collectors.joining(","));
    }

    /**
     * 基于单字段composite，指定别名
     *
     * @param getter 字段属性getter
     * @param alias  别名
     * @param <T>    实体泛型
     * @return 包装集合
     */
    static <T> SelectComposite composite(SFunction<T, ?> getter, String alias) {
        AliasComposite.add(getter, alias);
        return () -> columns(getter);
    }

    /**
     * 查询表下面的所有字段
     *
     * @param clazz 实体类
     * @param <T>   实体类泛型
     * @return 字段集合
     */
    static <T> SelectComposite all(Class<T> clazz) {
        return () -> columns(EntityNameUtils.getFields(clazz).entrySet().stream()
                .map(entry -> new SFunction.StaticRef<>(clazz, entry.getKey(), entry.getValue()))
                .toArray(SFunction[]::new));
    }

    /**
     * 拼接多个getter
     *
     * @param <T>     实体泛型
     * @param getters 多个getter
     * @return 拼接后的字段列表
     */
    private static <T> String columns(SFunction<T, ?>... getters) {
        return Arrays.stream(getters).map(SFunction::get).collect(Collectors.joining(","));
    }
}
