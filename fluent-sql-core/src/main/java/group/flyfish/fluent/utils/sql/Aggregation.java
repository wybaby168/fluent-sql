package group.flyfish.fluent.utils.sql;

import group.flyfish.fluent.chain.SQLSegment;

import java.util.function.Supplier;

/**
 * 聚合函数表达式（实现 SQLSegment），支持 as 别名；
 * 使用 Supplier 延迟计算，确保在最终渲染 get() 时解析列名与别名上下文。
 */
public final class Aggregation implements SQLSegment {

    private final Supplier<String> expressionSupplier;

    private final String alias;

    private Aggregation(Supplier<String> expressionSupplier, String alias) {
        this.expressionSupplier = expressionSupplier;
        this.alias = alias;
    }

    private Aggregation(Supplier<String> expressionSupplier) {
        this(expressionSupplier, null);
    }

    /**
     * 设置别名，返回新实例
     */
    public Aggregation as(String alias) {
        return new Aggregation(this.expressionSupplier, alias);
    }

    /**
     * 渲染最终选择片段
     */
    @Override
    public String get() {
        String expression = expressionSupplier.get();
        if (alias == null || alias.isEmpty()) {
            return expression;
        }
        return expression + " as " + SqlNameUtils.wrap(alias);
    }

    // ---------- 工厂方法（延迟解析） ----------

    public static Aggregation countAll() {
        return new Aggregation(() -> "COUNT(1)");
    }

    public static <T> Aggregation count(SFunction<T, ?> column) {
        return new Aggregation(() -> "COUNT(" + column.getName() + ")");
    }

    public static Aggregation count(String column) {
        return new Aggregation(() -> "COUNT(" + smartWrap(column) + ")");
    }

    public static <T> Aggregation sum(SFunction<T, ?> column) {
        return new Aggregation(() -> "SUM(" + column.getName() + ")");
    }

    public static Aggregation sum(String column) {
        return new Aggregation(() -> "SUM(" + smartWrap(column) + ")");
    }

    public static <T> Aggregation max(SFunction<T, ?> column) {
        return new Aggregation(() -> "MAX(" + column.getName() + ")");
    }

    public static Aggregation max(String column) {
        return new Aggregation(() -> "MAX(" + smartWrap(column) + ")");
    }

    public static <T> Aggregation min(SFunction<T, ?> column) {
        return new Aggregation(() -> "MIN(" + column.getName() + ")");
    }

    public static Aggregation min(String column) {
        return new Aggregation(() -> "MIN(" + smartWrap(column) + ")");
    }

    public static <T> Aggregation avg(SFunction<T, ?> column) {
        return new Aggregation(() -> "AVG(" + column.getName() + ")");
    }

    public static Aggregation avg(String column) {
        return new Aggregation(() -> "AVG(" + smartWrap(column) + ")");
    }

    private static String smartWrap(String identifier) {
        if (identifier.contains(".")) {
            String[] parts = identifier.split("\\.", 2);
            return parts[0] + "." + SqlNameUtils.wrap(parts[1]);
        }
        return SqlNameUtils.wrap(identifier);
    }
}
