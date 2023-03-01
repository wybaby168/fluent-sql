package group.flyfish.fluent.utils.cache;

import java.util.function.Supplier;

/**
 * 绑定的单值对象
 *
 * @param <T> 泛型
 */
public class BoundObject<T> {

    private T object;

    public T computeIfAbsent(Supplier<T> supplier) {
        if (null == object) {
            object = supplier.get();
        }
        return object;
    }
}
