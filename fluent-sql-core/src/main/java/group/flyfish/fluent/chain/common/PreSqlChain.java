package group.flyfish.fluent.chain.common;

/**
 * 刚设置了查询字段后的链
 */
public interface PreSqlChain {

    /**
     * 从指定表查询
     *
     * @param type 表实体
     * @return 处理环节
     */
    HandleSqlChain from(Class<?> type);

    /**
     * 从指定字符串表查询
     *
     * @param table 表名
     * @return 处理环节
     */
    HandleSqlChain from(String table);

    /**
     * 从指定表查询，附加别名
     * 该接口适用于同一张表多次from的情况，可以从自表进行多次查询
     * 大部分情况下，您都不需要指定别名
     *
     * @param type  类型
     * @param alias 别名
     * @return 处理环节
     */
    HandleSqlChain from(Class<?> type, String alias);

    /**
     * 从指定字符串表查询，附加别名
     *
     * @param table 表名
     * @param alias 别名
     * @return 处理环节
     */
    HandleSqlChain from(String table, String alias);
}
