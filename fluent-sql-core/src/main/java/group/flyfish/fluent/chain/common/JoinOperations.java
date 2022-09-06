package group.flyfish.fluent.chain.common;

import group.flyfish.fluent.query.JoinCandidate;

/**
 * 表连接操作
 *
 * @author wangyu
 */
public interface JoinOperations {

    /**
     * 全量的join连接支持
     *
     * @param type  连接类型
     * @param clazz 目标表
     * @param alias 别名
     * @return join后的操作
     */
    AfterJoinSqlChain join(JoinCandidate type, Class<?> clazz, String alias);

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @return join后的操作
     */
    default AfterJoinSqlChain join(Class<?> clazz) {
        return join(JoinCandidate.INNER_JOIN, clazz, null);
    }

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @param alias 别名
     * @return join后的操作
     */
    default AfterJoinSqlChain join(Class<?> clazz, String alias) {
        return join(JoinCandidate.INNER_JOIN, clazz, alias);
    }

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @return join后的操作
     */
    default AfterJoinSqlChain leftJoin(Class<?> clazz) {
        return join(JoinCandidate.LEFT_JOIN, clazz, null);
    }

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @param alias 别名
     * @return join后的操作
     */
    default AfterJoinSqlChain leftJoin(Class<?> clazz, String alias) {
        return join(JoinCandidate.LEFT_JOIN, clazz, alias);
    }

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @return join后的操作
     */
    default AfterJoinSqlChain rightJoin(Class<?> clazz) {
        return join(JoinCandidate.RIGHT_JOIN, clazz, null);
    }

    /**
     * 使用内连接连接其他表
     *
     * @param clazz 其他表实体
     * @param alias 别名
     * @return join后的操作
     */
    default AfterJoinSqlChain rightJoin(Class<?> clazz, String alias) {
        return join(JoinCandidate.RIGHT_JOIN, clazz, alias);
    }
}
