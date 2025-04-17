package group.flyfish.fluent.utils.context;

import group.flyfish.fluent.utils.sql.SFunction;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 别名管理器
 * 因sql构建器运行时线程确定，故该类使用线程局部变量
 *
 * @author wangyu
 */
public final class AliasComposite {

    private static final String PREFIX = "t";

    // 表、列别名存储，本质上是一个简单map。线程局部缓存，阅后即焚
    private static final ThreadLocal<AliasCache> ALIAS = new ThreadLocal<>();

    /**
     * 添加别名
     *
     * @param key   别名key
     * @param alias 别名
     */
    public static String add(Class<?> key, String alias) {
        return sharedCache().add(key, alias);
    }

    /**
     * 添加别名
     *
     * @param key   别名key
     * @param alias 别名
     */
    public static <T> String add(SFunction<T, ?> key, String alias) {
        return sharedCache().add(key, alias);
    }


    /**
     * 判断表是否有别名
     *
     * @param key 实体类
     * @return 是否存在
     */
    public static boolean has(Class<?> key) {
        return sharedCache().has(key);
    }

    /**
     * 判断表是否有别名
     *
     * @param key 实体类
     * @return 是否存在
     */
    public static <T> boolean has(SFunction<T, ?> key) {
        return sharedCache().has(key);
    }

    /**
     * 获取别名
     *
     * @param key 键
     * @return 结果
     */
    public static String get(Class<?> key) {
        return sharedCache().get(key);
    }

    /**
     * 获取别名
     *
     * @param key 键
     * @return 结果
     */
    public static <T> String get(SFunction<T, ?> key) {
        return sharedCache().get(key);
    }

    /**
     * 清空别名缓存
     */
    public static void flush() {
        ALIAS.remove();
    }

    public static AliasCache sharedCache() {
        AliasCache cache = ALIAS.get();
        if (null == cache) {
            cache = new AliasCache();
            ALIAS.set(cache);
        }
        return cache;
    }

    public static class AliasCache {

        // 表别名缓存map
        private final Map<String, String> instance = new ConcurrentHashMap<>();

        // 表别名内置计数
        private final AtomicInteger counter = new AtomicInteger(0);

        // 列别名缓存map
        private final Map<SFunction<?, ?>, String> columns = new ConcurrentHashMap<>();

        /**
         * 添加缓存，基本规则：
         * 1. 新缓存指定手动别名，优先以手动别名为准
         * 2. 未指定手动别名，且存在缓存的，不更新别名
         *
         * @param key   缓存key
         * @param alias 缓存别名，可为空
         * @return 返回缓存的别名
         */
        public String add(Class<?> key, @Nullable String alias) {
            if (StringUtils.hasText(alias)) {
                instance.put(key.getName(), alias);
                return alias;
            } else {
                return get(key);
            }
        }

        public boolean has(Class<?> key) {
            return instance.containsKey(key.getName());
        }

        public String get(Class<?> key) {
            return instance.computeIfAbsent(key.getName(), this::generate);
        }

        public Optional<String> computeIfPresent(Class<?> key, Function<String, String> computer) {
            if (has(key)) {
                return Optional.ofNullable(computer.apply(get(key)));
            }
            return Optional.empty();
        }

        /**
         * 生成key
         *
         * @param key 类型key
         * @return 生成的key
         */
        public String generate(String key) {
            return PREFIX + counter.incrementAndGet();
        }

        /**
         * 判断是否含有某个列引用的别名
         *
         * @param column 列引用
         * @param <T>    泛型
         * @return 是否存在
         */
        public <T> boolean has(SFunction<T, ?> column) {
            return columns.containsKey(column);
        }

        /**
         * 列缓存
         *
         * @param column 列引用
         * @param alias  别名
         * @param <T>    实体泛型
         */
        public <T> String add(SFunction<T, ?> column, String alias) {
            columns.put(column, alias);
            return alias;
        }

        /**
         * 获取列引用所绑定的别名
         *
         * @param column 列引用
         * @param <T>    泛型
         * @return 别名，可能为空
         */
        public <T> String get(SFunction<T, ?> column) {
            return columns.get(column);
        }
    }
}
