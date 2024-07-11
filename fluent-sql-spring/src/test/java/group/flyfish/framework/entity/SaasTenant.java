package group.flyfish.framework.entity;

import group.flyfish.fluent.binding.JSONInject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Table;
import java.io.Serializable;
import java.util.Map;

/**
 * saas租户
 *
 * @author wangyu
 */
@Getter
@Setter
@Table(name = "saas_tenant")
public class SaasTenant extends Po {

    @Property("租户名称")
    private String name;

    @Property(value = "租户标识", description = "系统唯一标识", unique = true)
    private String identifier;

    @Property(value = "租户代码", description = "统一社会信用代码")
    private String code;

    @Property(value = "数据库配置", description = "租户自有数据库配置")
    @JSONInject
    private DataSourceConfig datasource;

    @Property(value = "存储配置", description = "租户自有存储配置")
    @JSONInject
    private StorageConfig storage;

    @Property("租户状态")
    private Status status;

    @Property("合同状态")
    private ContractStatus contractStatus;

    @Property("是否启用")
    private Boolean enable;

    @Property("备注")
    private String comment;

    @Getter
    @AllArgsConstructor
    public enum Status {

        PENDING("未开通"),
        TRYING("试用中"),
        CREATED("已开通"),
        EXPIRED("已过期"),
        CANCELED("已注销");

        private final String name;
    }


    @Getter
    @AllArgsConstructor
    public enum ContractStatus {

        PENDING("未签订"),
        ENGAGED("已签订"),
        ABANDONED("已作废");

        private final String name;
    }

    /**
     * 租户数据源配置信息
     *
     * @author wangyu
     */
    @Data
    public static class DataSourceConfig implements Serializable {

        private static final long serialVersionUID = 3751414771731042514L;

        @Property("连接字符串")
        private String connection;

        @Property("驱动名称")
        private String driverClass = "com.mysql.cj.jdbc.Driver";

        @Property("用户名")
        private String username;

        @Property("密码")
        private String password;
    }

    /**
     * 租户存储信息
     */
    @Data
    public static class StorageConfig implements Serializable {

        private static final long serialVersionUID = -9032829515869208750L;

        //minio用户信息
        @Property(value = "服务器地址", description = "可以是sftp服务器地址、minio地址、azure地址")
        private String server;
        @Property(value = "目录名", description = "可以是本地目录、sftp目录、minio桶名称，azure桶名称")
        private String directory;
        @Property(value = "用户名", description = "可以是sftp用户名、minio用户名、azure用户名")
        private String username;
        @Property(value = "用户密码", description = "可以是sftp摩玛、minio密码、azure密码等")
        private String password;
        @Property("其他连接参数。根据不同的文件存储方式可以自由放入")
        private Map<String, Object> properties;
    }
}
