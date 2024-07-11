package group.flyfish.framework.cases;

import com.fasterxml.jackson.core.JsonProcessingException;
import group.flyfish.fluent.utils.data.ObjectMappers;
import group.flyfish.framework.TestCase;
import group.flyfish.framework.entity.SaasOrder;
import group.flyfish.framework.entity.SaasQuota;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.vo.TenantContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@TestCase.Name("使用JDBC")
public class JdbcTestCase extends AbstractTestCase<List<TenantContext>> {

    public JdbcTestCase(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    @Override
    public void initialize() throws Exception {
        // do nothing
    }

    /**
     * 测试运行逻辑
     *
     * @return 运行结果
     * @throws Exception 异常
     */
    @Override
    public List<TenantContext> run() throws Exception {
        String statement = "SELECT t1.`id` as `id`,t1.`name` as `name`,t1.`identifier` as `identifier`,t1.`datasource` as `datasource`,t1.`storage` as `storage`,t1.`status` as `status`,t1.`enable` as `enable`,t2.`quota_config` as `quotaConfig`,t2.`order_time` as `orderTime`,t2.`expire_time` as `expireTime`,t2.`order_type` as `orderType` FROM saas_tenant `t1` LEFT JOIN saas_order `t2` ON t2.`tenant_id` = t1.`id` LEFT JOIN saas_plan `t3` ON t3.`id` = t2.`plan_id` WHERE t1.`enable` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pst = connection.prepareStatement(statement);
             ResultSet rs = execute(pst, true)
        ) {
            return extract(rs);
        }
    }

    /**
     * 执行prepared statement并绑定值
     *
     * @param statement 已经准备好的statement
     * @param args      参数
     * @return 结果集
     * @throws SQLException 异常
     */
    private ResultSet execute(PreparedStatement statement, Object... args) throws SQLException {
        statement.setBoolean(1, (Boolean) args[0]);
        return statement.executeQuery();
    }

    /**
     * 从结果集取得对象
     *
     * @param resultSet 结果集
     * @return 结果
     */
    private List<TenantContext> extract(ResultSet resultSet) throws SQLException {
        List<TenantContext> list = new ArrayList<>();
        while (resultSet.next()) {
            TenantContext context = new TenantContext();
            context.setId(resultSet.getString("id"));
            context.setName(resultSet.getString("name"));
            context.setIdentifier(resultSet.getString("identifier"));
            context.setDatasource(readValue(resultSet.getString("datasource"), SaasTenant.DataSourceConfig.class));
            context.setStorage(readValue(resultSet.getString("storage"), SaasTenant.StorageConfig.class));
            context.setStatus(SaasTenant.Status.valueOf(resultSet.getString("status")));
            context.setEnable(resultSet.getBoolean("enable"));
            context.setQuota(readValue(resultSet.getString("quotaConfig"), SaasQuota.class));
            context.setOrderTime(resultSet.getDate("orderTime"));
            context.setExpireTime(resultSet.getDate("expireTime"));
            context.setOrderType(SaasOrder.Type.valueOf(resultSet.getString("orderType")));
            list.add(context);
        }
        return list;
    }

    private <T> T readValue(String value, Class<T> clazz) {
        try {
            return ObjectMappers.shared().readValue(value, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
