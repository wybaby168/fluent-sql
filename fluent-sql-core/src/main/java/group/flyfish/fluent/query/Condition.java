package group.flyfish.fluent.query;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.utils.sql.SFunction;

import java.util.Collection;

/**
 * 条件sql链
 *
 * @author wangyu
 */
public interface Condition extends Parameterized, SQLSegment {

    /**
     * 等于条件
     *
     * @param value 值
     * @return 查询链
     */
    Query eq(Object value);

    /**
     * 等于另外一个字段
     *
     * @param ref 引用
     * @param <T> 泛型
     * @return 查询链
     */
    <T> Query eq(SFunction<T, ?> ref);

    /**
     * 模糊查询
     *
     * @param pattern 关键字
     * @return 查询链
     */
    Query like(String pattern);

    /**
     * 在集合内
     *
     * @param collection 任意内容集合
     * @return 查询链
     */
    Query in(Collection<?> collection);
}
