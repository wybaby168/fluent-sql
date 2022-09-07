package group.flyfish.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mysql.cj.jdbc.Driver;
import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import group.flyfish.fluent.utils.data.ObjectMappers;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasPlan;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.vo.TenantContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static group.flyfish.fluent.chain.SQL.select;
import static group.flyfish.fluent.chain.select.SelectComposite.composite;
import static group.flyfish.fluent.query.Query.where;

/**
 * 链式jdbc测试
 *
 * @author wangyu
 */
public class FluentJdbcTest {

    /**
     * 静态测试demo
     * 实际测试请根据自己的数据库字段书写实体
     *
     * @throws SQLException sql异常
     */
    @Test
    public void testSql() throws SQLException, JsonProcessingException {
        DataSource dataSource = new SimpleDriverDataSource(
                new Driver(),
                "jdbc:mysql://127.0.0.1:3306/epi_project?autoReconnect=true&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false&serverTimezone=Asia/Shanghai",
                "root",
                "oI3WtMO8h%mSYARp"
        );
        // 基于构造器自动绑定注册，在实际应用中使用@Bean声明即可，可参考下面的demo
        new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));

        // 一个平平无奇的查询
        List<TenantContext> list = select(
                // 查询租户全量字段
                composite(SaasTenant::getId, SaasTenant::getName, SaasTenant::getIdentifier, SaasTenant::getDatasource,
                        SaasTenant::getStorage, SaasTenant::getStatus, SaasTenant::getEnable),
                // 查询套餐
                composite(SaasOrder::getQuotaConfig, SaasOrder::getOrderTime, SaasOrder::getExpireTime,
                        SaasOrder::getOrderType))
                .from(SaasTenant.class)
                .leftJoin(SaasOrder.class).on(where(SaasOrder::getTenantId).eq(SaasTenant::getId))
                .leftJoin(SaasPlan.class).on(where(SaasPlan::getId).eq(SaasOrder::getPlanId))
                .matching(where(SaasTenant::getEnable).eq(true))
                .list(TenantContext.class);

        // 打印效果
        System.out.println(ObjectMappers.shared().writeValueAsString(list));
    }

    /**
     * 实际应用中，使用bean注入并实例化
     *
     * @param operations 从spring依赖注入的jdbc template
     */
    @Bean
    public FluentSQLOperations fluentSQLOperations(JdbcOperations operations) {
        return new JdbcTemplateFluentSQLOperations(operations);
    }
}
