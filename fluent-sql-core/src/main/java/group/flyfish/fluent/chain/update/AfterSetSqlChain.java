package group.flyfish.fluent.chain.update;

import group.flyfish.fluent.chain.common.ExecutableSql;
import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.query.Query;

/**
 * set之后能干的事儿
 *
 * @author wangyu
 */
public interface AfterSetSqlChain extends PreSqlChain, ExecutableSql {

    /**
     * 查询条件
     *
     * @param query 查询
     * @return 链式调用
     */
    AfterWhereSqlChain matching(Query query);
}
