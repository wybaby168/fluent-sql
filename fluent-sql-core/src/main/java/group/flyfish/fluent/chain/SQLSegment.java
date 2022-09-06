package group.flyfish.fluent.chain;

import group.flyfish.fluent.utils.sql.SqlNameUtils;

/**
 * sql片段
 *
 * @author wangyu
 */
@FunctionalInterface
public interface SQLSegment {

    /**
     * @return 得到sql片段
     */
    String get();

    /**
     * 类型强转，请慎用，除非你知道真实类型
     *
     * @param value 任意类型值
     * @param <T>   入参泛型
     * @param <R>   出参泛型
     * @return 转换类型值
     */
    default <T, R> R cast(T value) {
        return SqlNameUtils.cast(value);
    }
}
