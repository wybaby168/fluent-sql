package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.Order;
import group.flyfish.fluent.chain.SQLSegment;

/**
 * where条件后支持的操作
 *
 * @author wangyu
 */
public interface AfterWhereSqlChain extends AfterOrderSqlChain {

    /**
     * 拼接排序条件
     *
     * @param orders 排序
     * @return 结果
     */
    AfterOrderSqlChain order(Order... orders);

    /**
     * 分组
     *
     * @param fields 分组字段
     * @return 结果
     */
    <T> AfterWhereSqlChain groupBy(SQLSegment... fields);

    /**
     * 分组（字符串列名）
     *
     * @param columns 列名
     * @return 结果
     */
    AfterWhereSqlChain groupBy(String... columns);

    /**
     * 分组过滤（having）
     *
     * @param query 查询
     * @return 结果
     */
    AfterWhereSqlChain having(group.flyfish.fluent.query.Query query);
}
