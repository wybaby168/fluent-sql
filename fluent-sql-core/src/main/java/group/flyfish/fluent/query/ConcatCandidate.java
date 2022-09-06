package group.flyfish.fluent.query;

import group.flyfish.fluent.chain.SQLSegment;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 连接用的候选
 *
 * @author wangyu
 */
@AllArgsConstructor
@Getter
public enum ConcatCandidate implements SQLSegment {

    AND("且"), OR("或");

    private final String name;

    /**
     * @return 得到sql片段
     */
    @Override
    public String get() {
        return name();
    }
}
