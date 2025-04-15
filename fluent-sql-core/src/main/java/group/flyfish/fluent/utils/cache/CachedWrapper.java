package group.flyfish.fluent.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 缓存的supplier
 * 组件内部通过该核心逻辑缓存编译的sql
 * <p>
 * 工作机制参考自前端 memoize 函数
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

    /**
     * 包装简单的supplier
     *
     * @param supplier 提供者
     * @param <T>      泛型
     * @return 结果
     */
    static <T, E> Supplier<T> wrap(Supplier<T> supplier, Supplier<E> identity) {
        ConditionalBoundObject<T, E> object = new ConditionalBoundObject<>();
        return () -> object.computeIfAbsent(k -> supplier.get(), identity);
    }


    /**
     * 通过给定的条件进行缓存
     *
     * @param supplier 原数据提供者
     * @param identity 提供者计算后的标识
     * @param <T>      实际缓存内容的泛型
     * @return 结果
     */
    static <T, E> Supplier<T> wrap(Function<E, T> computer, Supplier<E> identity) {
        ConditionalBoundObject<T, E> object = new ConditionalBoundObject<>();
        return () -> object.computeIfAbsent(computer, identity);
    }

    /**
     * 绑定的单值对象
     *
     * @param <T> 泛型
     */
    class BoundObject<T> {

        private T object;

        public T computeIfAbsent(Supplier<T> supplier) {
            if (null == object) {
                object = supplier.get();
            }
            return object;
        }
    }

    class ConditionalBoundObject<T, E> {

        private final Map<E, T> caches = new HashMap<>();

        public T computeIfAbsent(Function<E, T> computer, Supplier<E> identity) {
            E key = identity.get();
            return caches.computeIfAbsent(key, computer);
        }
    }

}
