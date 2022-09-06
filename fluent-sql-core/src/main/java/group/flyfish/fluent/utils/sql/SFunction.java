package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.utils.context.AliasComposite;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import static group.flyfish.fluent.utils.sql.SqlNameUtils.wrap;

/**
 * 支持序列化的function
 *
 * @param <T> 泛型入参
 * @param <R> 泛型返回值
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * 快速获取名称
     *
     * @return 序列化后的名称
     */
    default String getName() {
        return EntityNameUtils.toName(this);
    }

    /**
     * 快速获取选择语句，附带别名
     *
     * @return 结果
     */
    default String getSelect() {
        return EntityNameUtils.toSelect(this);
    }

    @SuppressWarnings("unchecked")
    default <V, P> SFunction<V, P> cast() {
        return (SFunction<V, P>) this;
    }

    /**
     * 覆盖写法
     *
     * @param <T> 泛型
     * @param <R> 泛型
     */
    @RequiredArgsConstructor
    class StaticRef<T, R> implements SFunction<T, R> {

        private final Class<?> type;

        private final String name;

        private final String column;

        @Override
        public String getName() {
            return handle(() -> wrap(name));
        }

        @Override
        public String getSelect() {
            return handle(() -> String.join(" ", wrap(column), "as", wrap(name)));
        }

        private String handle(Supplier<String> handler) {
            if (AliasComposite.has(type)) {
                return AliasComposite.get(type) + "." + handler.get();
            }
            return handler.get();
        }

        @Override
        public R apply(T t) {
            return null;
        }
    }

}
