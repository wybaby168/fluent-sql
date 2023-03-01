package group.flyfish.fluent.utils.sql;

/**
 * sql方法，用于缓存执行
 *
 * @param <R> 返回值泛型
 */
@FunctionalInterface
public interface SqlMethod<R> {

    /**
     * 执行方法
     *
     * @param parameters 参数
     * @return 结果
     */
    R execute(Object... parameters);
}
