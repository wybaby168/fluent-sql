package group.flyfish.framework;

import group.flyfish.fluent.chain.select.FetchSqlChain;
import group.flyfish.fluent.operations.R2dbcFluentSQLOperations;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasPlan;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.vo.TenantContext;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.Test;
import org.springframework.r2dbc.core.DatabaseClient;

import java.sql.SQLException;

import static group.flyfish.fluent.chain.SQL.select;
import static group.flyfish.fluent.chain.select.SelectComposite.composite;
import static group.flyfish.fluent.query.Query.where;

public class FluentR2dbcTest {

    private FetchSqlChain sql;

    /**
     * 静态测试demo
     * 实际测试请根据自己的数据库字段书写实体
     *
     * @throws SQLException sql异常
     */
    @Test
    public void testSql() {
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .database("epi_project")
                .user("root")
                .password("Unicom#2018")
                .build();
        ConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);
        DatabaseClient databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory)
                .build();
        new R2dbcFluentSQLOperations(databaseClient);

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

        System.out.println(sql.as(TenantContext.class).reactive().all()
                .collectList()
                .block());
    }
}
