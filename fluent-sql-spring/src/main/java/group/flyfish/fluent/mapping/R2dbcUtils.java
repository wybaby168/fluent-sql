package group.flyfish.fluent.mapping;

import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.Map;

/**
 * r2dbc自定义转换器
 *
 * @author wangyu
 */
final class R2dbcUtils {

    private static final DateTimeFormatter UNIVERSAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<Class<?>, Converter<Object, Object>> converters = Map.of(
            Boolean.class, To::toBoolean,
            Date.class, To::toDate
    );

    private static boolean isCommonType(Class<?> clazz) {
        // 判断是否是基础数据类型或其包装类
        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return true;
        }
        // 判断是否是 String 类型
        if (String.class.isAssignableFrom(clazz)) {
            return true;
        }
        // 判断是否是数组类型
        if (ObjectUtils.isArray(clazz)) {
            return true;
        }
        // 判断是否是日期类型
        if (Temporal.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)) {
            return true;
        }
        // 其他常见类型可以继续添加判断逻辑
        return false;
    }

    /**
     * 安全的抽取列值，不支持复杂类型是，尝试使用惯用转换逻辑
     *
     * @param row    行数据
     * @param column 列
     * @param type   参数类型
     * @return 结果
     */
    static Object getRowValue(Row row, String column, Class<?> type) {
        // 不是公共类型，获取真实数据库类型，后续转换
        if (!isCommonType(type)) {
            return row.get(column);
        }
        // 转换器不支持的逻辑，交由原生r2dbc spi
        if (!converters.containsKey(type)) {
            return row.get(column, type);
        }
        // 基于转换器
        Object value = row.get(column);
        if (null == value) return null;
        return converters.get(type).convert(value);
    }

    /**
     * 一些公用的转换逻辑
     */
    private static class To {

        static Boolean toBoolean(Object value) {
            if (value instanceof ByteBuffer v) {
                return v.get() != 0;
            }
            if (value instanceof Boolean v) {
                return v;
            }
            return Boolean.parseBoolean(value.toString());
        }

        static Date toDate(Object value) {
            if (value instanceof Date v) {
                return v;
            }
            if (value instanceof LocalDate v) {
                return Date.from(v.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (value instanceof LocalDateTime v) {
                return Date.from(v.atZone(ZoneId.systemDefault()).toInstant());
            }
            if (value instanceof Number v) {
                return new Date(v.longValue());
            }
            if (value instanceof CharSequence) {
                return Date.from(LocalDateTime.parse(value.toString(), UNIVERSAL_DATE_FORMATTER)
                        .atZone(ZoneId.systemDefault()).toInstant());
            }
            throw new IllegalArgumentException("Cannot convert " + value + " to a Date");
        }
    }
}
