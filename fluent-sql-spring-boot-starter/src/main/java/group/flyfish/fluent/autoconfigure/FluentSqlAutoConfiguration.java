package group.flyfish.fluent.autoconfigure;

import group.flyfish.fluent.autoconfigure.FluentSqlAutoConfiguration.JdbcFluentSqlAutoConfigure;
import group.flyfish.fluent.autoconfigure.FluentSqlAutoConfiguration.R2dbcFluentSqlAutoConfigure;
import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import group.flyfish.fluent.operations.R2dbcFluentSQLOperations;
import group.flyfish.fluent.operations.ReactiveFluentSQLOperations;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import javax.sql.DataSource;

/**
 * fluent sql自动配置
 *
 * @author wangyu
 */
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Import({R2dbcFluentSqlAutoConfigure.class, JdbcFluentSqlAutoConfigure.class})
public class FluentSqlAutoConfiguration {

    @ConditionalOnClass(DatabaseClient.class)
    static class R2dbcFluentSqlAutoConfigure {

        /**
         * 动态注入初始化的bean，完成注入配置
         *
         * @param databaseClient 从spring r2dbc注入
         */
        @Bean
        @ConditionalOnMissingBean(ReactiveFluentSQLOperations.class)
        @ConditionalOnBean(DatabaseClient.class)
        public ReactiveFluentSQLOperations fluentSQLOperations(DatabaseClient databaseClient) {
            return new R2dbcFluentSQLOperations(databaseClient);
        }
    }

    @ConditionalOnClass(DataSource.class)
    static class JdbcFluentSqlAutoConfigure {

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
}
