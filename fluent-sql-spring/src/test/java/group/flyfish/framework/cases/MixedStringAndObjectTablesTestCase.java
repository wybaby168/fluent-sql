package group.flyfish.framework.cases;

import group.flyfish.fluent.chain.select.AfterWhereSqlChain;
import group.flyfish.fluent.debug.FluentSqlDebugger;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import group.flyfish.framework.TestCase;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.vo.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static group.flyfish.fluent.chain.SQL.select;
import static group.flyfish.fluent.chain.select.SelectComposite.composite;
import static group.flyfish.fluent.query.Query.where;

/**
 * 混合对象与字符串表名的 SQL 构建测试
 */
@TestCase.Name("混合对象与字符串表名拼接SQL测试")
public class MixedStringAndObjectTablesTestCase extends AbstractTestCase<List<TenantContext>> {

    private AfterWhereSqlChain sql;

    public MixedStringAndObjectTablesTestCase(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void initialize() throws Exception {
        // 绑定 JDBC 实现
        new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));

        FluentSqlDebugger.enable();

        // 演示字符串表名混用
        // from 使用对象表，join 使用字符串表名 + 别名，where/on 条件混用类字段与字符串列名
        this.sql = select(
                composite(SaasTenant::getId, SaasTenant::getName, SaasTenant::getIdentifier),
                composite(SaasOrder::getOrderTime, SaasOrder::getExpireTime)
        )
                .from(SaasTenant.class)
                // 字符串方式 join 已存在的表 saas_order
                .leftJoin("saas_order", "o").on(
                        where("o.tenant_id").eq(SaasTenant::getId)
                )
                // 再字符串方式 join 已存在的表 saas_plan，等于对象字段
                .leftJoin("saas_plan", "p").on(
                        where("p.id").eq(SaasOrder::getPlanId)
                )
                // 条件混合写法：类字段 + 字符串列名
                .matching(
                        where(SaasTenant::getEnable).eq(true)
                                .and("o.order_type").in(List.of(1, 2, 3))
                );
    }

    @Override
    public List<TenantContext> run() throws Exception {
        // 执行并返回
        return sql.as(TenantContext.class).block().all();
    }
}


