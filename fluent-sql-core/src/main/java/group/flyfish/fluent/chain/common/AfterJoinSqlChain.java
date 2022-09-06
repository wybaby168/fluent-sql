package group.flyfish.fluent.chain.common;

import group.flyfish.fluent.query.Query;

/**
 * join连接后可执行的操作
 *
 * @author wangyu
 */
public interface AfterJoinSqlChain {

    /**
     * 连接条件
     *
     * @param query 查询
     * @return 处理链
     */
    HandleSqlChain on(Query query);

    /**
     * 不带连接条件
     *
     * @return 处理链
     */
    HandleSqlChain then();
}
