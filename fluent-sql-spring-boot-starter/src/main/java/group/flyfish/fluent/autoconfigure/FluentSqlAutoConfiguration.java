package group.flyfish.fluent.autoconfigure;

import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * fluent sql自动配置
 *
 * @author wangyu
 */
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class FluentSqlAutoConfiguration {

    /**
     * 动态注入初始化的bean，完成注入配置
     *
     * @param dataSource 从spring datasource注入
     */
    @Bean
    @ConditionalOnMissingBean(FluentSQLOperations.class)
    @ConditionalOnBean(DataSource.class)
    public FluentSQLOperations fluentSQLOperations(DataSource dataSource) {
        return new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));
    }
}
