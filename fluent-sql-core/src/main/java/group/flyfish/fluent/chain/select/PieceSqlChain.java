package group.flyfish.fluent.chain.select;

/**
 * 支持分片的sql链
 *
 * @author wangyu
 */
public interface PieceSqlChain extends FetchSqlChain {

    /**
     * 限制返回条数
     *
     * @param count 条数
     * @return 结果
     */
    PieceSqlChain limit(int count);

    /**
     * 跳过多少行
     *
     * @param rows 行数
     * @return 结果
     */
    PieceSqlChain offset(int rows);
}
