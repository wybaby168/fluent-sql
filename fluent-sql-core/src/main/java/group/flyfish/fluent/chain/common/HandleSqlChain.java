package group.flyfish.fluent.chain.common;

import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.query.Query;

/**
 * 处理阶段的sql链
 *
 * @author wangyu
 */
public interface HandleSqlChain extends JoinOperations, AfterWhereSqlChain {

    /**
     * 拼接查询条件
     *
     * @param query 条件
     * @return 结果
     */
    AfterWhereSqlChain matching(Query query);
}
