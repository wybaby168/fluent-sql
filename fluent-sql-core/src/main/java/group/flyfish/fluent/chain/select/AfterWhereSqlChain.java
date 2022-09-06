package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.Order;

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
}
