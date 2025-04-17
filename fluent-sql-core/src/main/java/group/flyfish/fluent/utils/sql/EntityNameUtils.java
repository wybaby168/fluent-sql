package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.utils.cache.LRUCache;
import group.flyfish.fluent.utils.context.AliasComposite;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    private static final String JPA_TABLE = "javax.persistence.Table";
    private static final String SPRING_DATA_TABLE = "org.springframework.data.relational.core.mapping.Table";
    private static final String TRANSIENT_ANNOTATION = "org.springframework.data.annotation.Transient";

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
            MergedAnnotations annotations = MergedAnnotations.from(entityClass);
            if (annotations.isPresent(JPA_TABLE)) {
                String tableName = annotations.get(JPA_TABLE).getString("name");
                if (StringUtils.hasText(tableName)) {
                    return tableName;
                }
            }
            if (annotations.isPresent(SPRING_DATA_TABLE)) {
                String tableName = annotations.get(SPRING_DATA_TABLE).getString("name");
                if (StringUtils.hasText(tableName)) {
                    return tableName;
                }
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
        // 交由处理器处理
        String handled = handler.apply(column, finalName);
        // 返回完全限定名
        return cache.computeIfPresent(beanClass, name -> name + "." + handled)
                .orElse(handled);
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
        ReflectionUtils.doWithFields(type, field -> fields.put(field.getName(), resolveFinalName(field)),
                EntityNameUtils::isField);
        return fields;
    }

    private static boolean isField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers) || field.isAnnotationPresent(Transient.class)) {
            return false;
        }
        MergedAnnotations annotations = MergedAnnotations.from(field);
        if (annotations.isPresent(TRANSIENT_ANNOTATION)) {
            return false;
        }
        return true;
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
