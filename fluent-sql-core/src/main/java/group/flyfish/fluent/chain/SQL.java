package group.flyfish.fluent.chain;

import group.flyfish.fluent.chain.common.PreSqlChain;
import group.flyfish.fluent.chain.select.SelectComposite;
import group.flyfish.fluent.update.Update;
import group.flyfish.fluent.utils.sql.SFunction;
import org.springframework.jdbc.core.JdbcOperations;

import static group.flyfish.fluent.utils.sql.SqlNameUtils.cast;

/**
 * 链式查询入口
 *
 * @author wangyu
 */
public interface SQL {

    /**
     * 查询起手式
     *
     * @return 自身
     */
    @SafeVarargs
    static <T> PreSqlChain select(SFunction<T, ?>... fields) {
        return SQLFactory.produce().select(fields);
    }

    /**
     * 查询起手式
     *
     * @return 自身
     */
    static PreSqlChain select(SelectComposite<?>... composites) {
        return SQLFactory.produce().select(SelectComposite.combine(composites));
    }

    /**
     * 查询起手式，查询全部字段
     *
     * @return 结果
     */
    static PreSqlChain select() {
        return SQLFactory.produce().select(cast(new SFunction[]{}));
    }

    /**
     * 更新表起手
     *
     * @param clazz 表
     * @return 更新链式
     */
    static Update update(Class<?> clazz) {
        return SQLFactory.produce().update(clazz);
    }

    /**
     * 绑定数据源上下文，基于jdbc template
     *
     * @param operations jdbc操作
     */
    static void bind(JdbcOperations operations) {
        SQLImpl.bind(operations);
    }
}
