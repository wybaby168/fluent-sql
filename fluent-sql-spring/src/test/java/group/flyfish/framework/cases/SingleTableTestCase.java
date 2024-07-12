package group.flyfish.framework.cases;

import group.flyfish.fluent.chain.execution.BoundProxy;
import group.flyfish.framework.TestCase;
import group.flyfish.framework.entity.SaasTenant;

import java.util.List;

import static group.flyfish.fluent.chain.SQL.select;

/**
 * 单表测试用例
 *
 * @author wangyu
 */
@TestCase.Name("单表查询测试")
public class SingleTableTestCase extends AbstractTestCase<List<SaasTenant>> {

    public SingleTableTestCase() {
        super(null);
    }

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    @Override
    public void initialize() throws Exception {

    }

    /**
     * 测试运行逻辑
     *
     * @return 运行结果
     * @throws Exception 异常
     */
    @Override
    public List<SaasTenant> run() throws Exception {
        // 单表查询
        BoundProxy<SaasTenant> proxy = select().from(SaasTenant.class).fetch();
        return proxy.block().all();
    }
}
