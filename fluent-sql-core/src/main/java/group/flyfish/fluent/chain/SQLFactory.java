package group.flyfish.fluent.chain;

/**
 * sql工厂
 *
 * @author wangyu
 * 普通静态工厂，用于生产实现实例
 */
public interface SQLFactory {

    /**
     * 生产实例
     *
     * @return sql操作
     */
    static SQLOperations produce() {
        return new SQLImpl();
    }
}
