package group.flyfish.fluent.utils.cache;

import java.util.function.Supplier;

/**
 * 缓存的supplier
 * 组件内部通过该核心逻辑缓存编译的sql
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

}
