package group.flyfish.fluent.chain.select;

import group.flyfish.fluent.chain.common.ExecutableSql;
import group.flyfish.fluent.chain.execution.BoundProxy;

public interface FetchSqlChain extends ExecutableSql {

    /**
     * 使用主表进行下一步操作
     *
     * @param <T> 泛型
     * @return 结果
     */
    <T> BoundProxy<T> fetch();

    /**
     * 转换为SQL实体
     *
     * @param type 具体结果类型
     * @param <T>  泛型
     * @return 结果
     */
    <T> BoundProxy<T> as(Class<T> type);
}
