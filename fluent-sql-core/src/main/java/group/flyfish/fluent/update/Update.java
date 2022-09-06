package group.flyfish.fluent.update;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.chain.update.AfterSetSqlChain;
import group.flyfish.fluent.query.Parameterized;
import group.flyfish.fluent.utils.sql.SFunction;

/**
 * 更新内容类
 *
 * @author wangyu
 */
public interface Update extends SQLSegment, Parameterized {

    /**
     * 基于对象设置所有同名值
     *
     * @param value 值
     * @return 链式
     */
    default Update setAll(Object value) {
        throw new UnsupportedOperationException("当前未实现该操作！");
    }

    /**
     * 设置值，来自具体值
     *
     * @param target 目标字段
     * @param value  具体值
     * @param <T>    泛型
     * @return 链式
     */
    <T> Update set(SFunction<T, ?> target, Object value);

    /**
     * 设置值，来自其他表字段
     *
     * @param target 目标字段
     * @param source 源端字段
     * @param <T>    目标泛型
     * @param <V>    源端泛型
     * @return 链式
     */
    <T, V> Update set(SFunction<T, ?> target, SFunction<V, ?> source);

    /**
     * 接下来要做的事
     *
     * @return 结果
     */
    AfterSetSqlChain then();
}
