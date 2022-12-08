package group.flyfish.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mysql.cj.jdbc.Driver;
import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import group.flyfish.framework.cases.FluentSqlTestCase;
import group.flyfish.framework.cases.JdbcTestCase;
import group.flyfish.framework.cases.MybatisTestCase;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
                "Unicom#2018"
        );
        // 准备待测试用例
        List<TestCase<?>> cases = Arrays.asList(
                new JdbcTestCase(dataSource),
                new MybatisTestCase(dataSource),
                new FluentSqlTestCase(dataSource)
        );
        // 执行测试
        cases.forEach(TestCase::test);
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
