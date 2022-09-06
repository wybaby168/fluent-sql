package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.utils.context.AliasComposite;
import group.flyfish.fluent.utils.sql.EntityNameUtils;
import group.flyfish.fluent.utils.sql.SFunction;
import group.flyfish.fluent.utils.sql.SqlNameUtils;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * 选择语句泛型包装
 *
 * @author wangyu
 */
@FunctionalInterface
public interface SelectComposite<T> {

    /**
     * 基于泛型的包装，以实体类为T，可以组合任意字段集合
     *
     * @param getter 字段属性getter
     * @param <T>    实体泛型
     * @return 包装集合
     */
    @SafeVarargs
    static <T> SelectComposite<T> composite(SFunction<T, Object>... getter) {
        return () -> Arrays.stream(getter);
    }

    /**
     * 基于单字段composite，指定别名
     *
     * @param getter 字段属性getter
     * @param alias  别名
     * @param <T>    实体泛型
     * @return 包装集合
     */
    static <T> SelectComposite<T> composite(SFunction<T, Object> getter, String alias) {
        AliasComposite.add(getter, alias);
        return () -> Stream.of(getter);
    }

    /**
     * 组合多个组合条件，并进行解包，忽略泛型
     *
     * @param composites 多个包
     * @param <T>        泛型，任意返回
     * @return 扁平化的字段列表
     */
    static <T> T combine(SelectComposite<?>... composites) {
        return SqlNameUtils.cast(Arrays.stream(composites).flatMap(SelectComposite::stream).toArray(SFunction[]::new));
    }

    /**
     * 查询表下面的所有字段
     *
     * @param clazz 实体类
     * @param <T>   实体类泛型
     * @return 字段集合
     */
    static <T> SelectComposite<T> all(Class<T> clazz) {
        return () -> EntityNameUtils.getFields(clazz).entrySet().stream()
                .map(entry -> new SFunction.StaticRef<>(clazz, entry.getKey(), entry.getValue()));
    }

    /**
     * 返回stream
     *
     * @return 结果流对象
     */
    Stream<SFunction<T, Object>> stream();
}
