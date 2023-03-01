package group.flyfish.fluent.utils.cache;

import java.util.function.Supplier;

/**
 * 缓存的supplier
 *
 * @author wangyu
 */
public interface CachedWrapper {

    /**
     * 包装简单的supplier
     *
     * @param supplier 提供者
     * @param <T>      泛型
     * @return 结果
     */
    static <T> Supplier<T> wrap(Supplier<T> supplier) {
        BoundObject<T> object = new BoundObject<>();
        return () -> object.computeIfAbsent(supplier);
    }
}
