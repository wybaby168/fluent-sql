package group.flyfish.fluent.chain;

import group.flyfish.fluent.utils.sql.SFunction;
import lombok.RequiredArgsConstructor;

/**
 * 排序支持
 *
 * @author wangyu
 */
@RequiredArgsConstructor
final class OrderImpl implements Order, SQLSegment {

    private final SFunction<?, ?> field;

    private String order = "asc";

    @Override
    public OrderImpl asc() {
        order = "asc";
        return this;
    }

    @Override
    public OrderImpl desc() {
        order = "desc";
        return this;
    }

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        return String.join(" ", field.getName(), order);
    }
}
