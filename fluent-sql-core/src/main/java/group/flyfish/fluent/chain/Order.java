package group.flyfish.fluent.chain;

import group.flyfish.fluent.utils.sql.SFunction;

/**
 * 排序链式声明
 *
 * @author wangyu
 */
public interface Order extends SQLSegment {

    /**
     * 以字段排序
     *
     * @param field 字段
     * @param <T>   泛型
     * @return 链式调用
     */
    static <T> Order by(SFunction<T, ?> field) {
        return new OrderImpl(field);
    }

    Order asc();

    Order desc();
}
