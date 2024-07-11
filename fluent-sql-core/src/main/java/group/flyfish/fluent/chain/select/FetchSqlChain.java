package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.common.ExecutableSql;
import group.flyfish.fluent.chain.execution.BoundProxy;

import java.util.List;

public interface FetchSqlChain extends ExecutableSql {

    /**
     * 转换为SQL实体
     *
     * @return 结果
     */
    <T> BoundProxy<T> as(Class<T> type);
}
