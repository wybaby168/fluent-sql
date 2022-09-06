package group.flyfish.fluent.update;

import group.flyfish.fluent.chain.SQLSegment;
import group.flyfish.fluent.chain.update.AfterSetSqlChain;
import group.flyfish.fluent.utils.sql.ConcatSegment;
import group.flyfish.fluent.utils.sql.SFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 更新实现
 *
 * @author wangyu
 */
@RequiredArgsConstructor
public class UpdateImpl implements Update {

    private final Function<Update, AfterSetSqlChain> chain;

    // 参数源
    private final Collection<Object> parameters = new ArrayList<>();

    // 片段
    private final List<SQLSegment> segments = new ArrayList<>();

    /**
     * 设置值，来自具体值
     *
     * @param target 目标字段
     * @param value  具体值
     * @return 链式
     */
    @Override
    public <T> Update set(SFunction<T, ?> target, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return this;
        }
        this.parameters.add(value);
        segments.add(new UpdatePart().concat(target::getName).concat("=").concat("?"));
        return this;
    }

    /**
     * 设置值，来自其他表字段
     *
     * @param target 目标字段
     * @param source 源端字段
     * @return 链式
     */
    @Override
    public <T, V> Update set(SFunction<T, ?> target, SFunction<V, ?> source) {
        segments.add(new UpdatePart().concat(target::getName).concat("=").concat(source::getName));
        return this;
    }

    /**
     * 接下来要做的事
     *
     * @return 结果
     */
    @Override
    public AfterSetSqlChain then() {
        return chain.apply(this);
    }

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        return segments.stream().map(SQLSegment::get).collect(Collectors.joining(", "));
    }

    /**
     * 获取当前对象包含的参数
     * 返回空集合，代表不需要参数
     * 返回null，代表参数为空，不处理该条件或查询
     *
     * @return 结果
     */
    @Override
    @Nullable
    public Collection<Object> getParameters() {
        if (segments.isEmpty()) {
            return null;
        }
        return parameters;
    }

    /**
     * 内部的部分
     */
    private static class UpdatePart extends ConcatSegment<UpdatePart> implements SQLSegment {

        /**
         * @return 得到sql片段
         */
        @Override
        public String get() {
            return this.segments.stream().map(SQLSegment::get).collect(Collectors.joining(" "));
        }
    }
}
