package group.flyfish.framework.cases;

import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import group.flyfish.framework.TestCase;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasPlan;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.vo.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static group.flyfish.fluent.chain.SQL.select;
import static group.flyfish.fluent.chain.select.SelectComposite.composite;
import static group.flyfish.fluent.query.Query.where;

@TestCase.Name("使用fluent-sql")
public class FluentSqlTestCase extends AbstractTestCase<List<TenantContext>> {

    private AfterWhereSqlChain sql;

    public FluentSqlTestCase(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    @Override
    public void initialize() throws Exception {
        // 基于构造器自动绑定注册，在实际应用中使用@Bean声明即可，可参考下面的demo
        new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));
        // 缓存构建结果
        this.sql = select(
                // 查询租户全量字段
                composite(SaasTenant::getId, SaasTenant::getName, SaasTenant::getIdentifier, SaasTenant::getDatasource,
                        SaasTenant::getStorage, SaasTenant::getStatus, SaasTenant::getEnable),
                // 查询套餐
                composite(SaasOrder::getQuotaConfig, SaasOrder::getOrderTime, SaasOrder::getExpireTime,
                        SaasOrder::getOrderType))
                .from(SaasTenant.class)
                .leftJoin(SaasOrder.class).on(where(SaasOrder::getTenantId).eq(SaasTenant::getId))
                .leftJoin(SaasPlan.class).on(where(SaasPlan::getId).eq(SaasOrder::getPlanId))
                .matching(where(SaasTenant::getEnable).eq(true));
        // 启用调试
//        FluentSqlDebugger.enable();
    }

    /**
     * 测试运行逻辑
     *
     * @return 运行结果
     * @throws Exception 异常
     */
    @Override
    public List<TenantContext> run() throws Exception {
        // 一个平平无奇的查询
        return sql.as(TenantContext.class).block().all();
    }
}
