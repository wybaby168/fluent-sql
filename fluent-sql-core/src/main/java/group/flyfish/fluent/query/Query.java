package group.flyfish.fluent.query;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.utils.sql.SFunction;

/**
 * 查询构建入口
 *
 * @author wangyu
 */
public interface Query extends Parameterized, SQLSegment {

    /**
     * 从where开始
     *
     * @param getter 字段lambda
     * @param <T>    实体泛型
     * @return 构建条件
     */
    static <T> Condition where(SFunction<T, ?> getter) {
        return new SimpleCondition(getter, new SimpleQuery());
    }

    /**
     * 以且连接下一个条件
     *
     * @param getter 字段lambda
     * @return 构建操作
     */
    <T> Condition and(SFunction<T, ?> getter);

    /**
     * 以且直接连接其他条件
     *
     * @param condition 其他条件
     * @return 查询操作
     */
    Query and(Condition condition);

    /**
     * 以且嵌套其他查询条件
     *
     * @param query 查询条件
     * @return 结果
     */
    Query and(Query query);


    /**
     * 以或连接下一个条件
     *
     * @param getter 字段lambda
     * @return 构建操作
     */
    <T> Condition or(SFunction<T, ?> getter);

    /**
     * 以或直接连接其他条件
     *
     * @param condition 其他条件
     * @return 查询操作
     */
    Query or(Condition condition);

    /**
     * 以或嵌套其他查询条件
     *
     * @param query 查询条件
     * @return 结果
     */
    Query or(Query query);
}
