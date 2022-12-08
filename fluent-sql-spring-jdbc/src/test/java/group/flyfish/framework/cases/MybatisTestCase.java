package group.flyfish.framework.cases;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import group.flyfish.framework.TestCase;
import group.flyfish.framework.entity.SaasQuota;
import group.flyfish.framework.entity.SaasTenant;
import group.flyfish.framework.mapper.TenantContextMapper;
import group.flyfish.framework.vo.TenantContext;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.util.List;

@TestCase.Name("使用mybatis执行")
public class MybatisTestCase extends AbstractTestCase<List<TenantContext>> {

    private SqlSessionFactory sqlSessionFactory;

    public MybatisTestCase(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    @Override
    public void initialize() throws Exception {
        // 使用mybatis执行
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(TenantContextMapper.class);
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        registry.register(SaasTenant.DataSourceConfig.class, JacksonTypeHandler.class);
        registry.register(SaasQuota.class, JacksonTypeHandler.class);
        registry.register(SaasTenant.StorageConfig.class, JacksonTypeHandler.class);
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * 测试运行逻辑
     *
     * @return 运行结果
     * @throws Exception 异常
     */
    @Override
    public List<TenantContext> run() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            TenantContextMapper mapper = session.getMapper(TenantContextMapper.class);
            return mapper.selectList(true);
        }
    }
}
