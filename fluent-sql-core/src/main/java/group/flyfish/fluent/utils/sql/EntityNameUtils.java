package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.utils.cache.LRUCache;
import group.flyfish.fluent.utils.context.AliasComposite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;

import static group.flyfish.fluent.utils.sql.SqlNameUtils.wrap;

/**
 * 属性名字处理器
 *
 * @author wangyu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityNameUtils {

    // SerializedLambda 反序列化缓存
    private static final Map<String, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

    // 列别名缓存
    private static final Map<Class<?>, Map<String, String>> COLUMN_CACHE = new LRUCache<>(5);

    // 表名缓存
    private static final Map<Class<?>, String> TABLE_CACHE = new LRUCache<>(3);

    public static <T> String toName(SFunction<T, ?> func) {
        return resolve(func, (column, property) -> wrap(column));
    }

    public static <T> String toSelect(SFunction<T, ?> func) {
        return resolve(func,
                (column, property) -> String.join(" ", wrap(column), "as", wrap(property)));
    }

    public static Map<String, String> getFields(Class<?> clazz) {
        return tryCache(clazz);
    }

    /**
     * 从一个实体类中取得表名
     *
     * @param entityClass 实体类
     * @return 结果
     */
    public static String getTableName(Class<?> entityClass) {
        return TABLE_CACHE.computeIfAbsent(entityClass, k -> {
            Table table = entityClass.getAnnotation(Table.class);
            if (null != table && StringUtils.hasText(table.name())) {
                return table.name();
            }
            return wrap(SqlNameUtils.camelToUnderline(entityClass.getSimpleName()));
        });
    }

    /**
     * 解析列并使用处理函数处理
     *
     * @param func    方法引用
     * @param handler 处理器
     * @param <T>     实体泛型
     * @return 处理结果
     */
    private static <T> String resolve(SFunction<T, ?> func, BinaryOperator<String> handler) {
        SerializedLambda lambda = resolve(func);
        String property = SqlNameUtils.methodToProperty(lambda.getImplMethodName());
        Class<?> beanClass = resolveEntityClass(lambda);
        String column = tryCache(beanClass).getOrDefault(property, SqlNameUtils.camelToUnderline(property));
        // 取得别名缓存
        AliasComposite.AliasCache cache = AliasComposite.sharedCache();
        // 确定最终名称
        String finalName = cache.has(func) ? cache.get(func) : property;
        if (cache.has(beanClass)) {
            return cache.get(beanClass) + "." + handler.apply(column, finalName);
        }
        return handler.apply(column, finalName);
    }


    /**
     * 解析方法引用为序列化lambda实例
     * 该方式会使用缓存
     *
     * @param func 方法引用
     * @param <T>  泛型
     * @return 解析结果
     */
    private static <T> SerializedLambda resolve(SFunction<T, ?> func) {
        Class<?> clazz = func.getClass();
        String name = clazz.getName();
        return Optional.ofNullable(FUNC_CACHE.get(name))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = SerializedLambda.resolve(func);
                    FUNC_CACHE.put(name, new WeakReference<>(lambda));
                    return lambda;
                });
    }

    /**
     * 解析获得实体类
     *
     * @param lambda 序列化的lambda
     * @return 最终获取的类
     */
    private static Class<?> resolveEntityClass(SerializedLambda lambda) {
        Class<?> type = lambda.getInstantiatedType();
        tryCache(type);
        return type;
    }

    /**
     * 构建字段缓存
     *
     * @param type 类型
     * @return 构建后的缓存
     */
    private static Map<String, String> buildFieldsCache(Class<?> type) {
        Map<String, String> fields = new HashMap<>();
        ReflectionUtils.doWithFields(type, field -> fields.put(field.getName(), resolveFinalName(field)));
        return fields;
    }

    /**
     * 解析字段注解或直接取用下划线逻辑
     *
     * @return 解析结果
     */
    private static String resolveFinalName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (null != column && StringUtils.hasText(column.name())) {
            return column.name();
        }
        return SqlNameUtils.camelToUnderline(field.getName());
    }

    /**
     * 尝试缓存
     *
     * @param entityClass bean的类型
     */
    private static Map<String, String> tryCache(Class<?> entityClass) {
        return COLUMN_CACHE.computeIfAbsent(entityClass, EntityNameUtils::buildFieldsCache);
    }
}
