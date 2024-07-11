package group.flyfish.framework.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * saas配额属性
 *
 * @author wangyu
 */
@Getter
@Setter
public class SaasQuota implements Serializable {

    private static final long serialVersionUID = -3615943142615938771L;

    // 用户限额
    @Property("在线用户数限制")
    private Integer userOnlineLimit;

    // mysql 运行限额
    @Property("每小时最大查询")
    private Integer databaseMaxQueriesPerHour;
    @Property("每小时最大修改")
    private Integer databaseMaxUpdatesPerHour;
    @Property("每小时最大连接数")
    private Integer databaseMaxConnectionsPerHour;
    @Property("当前用户最大连接数")
    private Integer databaseMaxUserConnections;

    // mysql 存储限额
    @Property("单库最大存储量(byte)")
    private Long databaseMaxSize;

    // 文件存储限额
    @Property("文件最大存储量(byte)")
    private Long storageMaxSize;
    @Property("上行速率最大值(KB/s)")
    private Long storageUpLinkMaxSpeed;
    @Property("下行速率最大值(KB/s)")
    private Long storageDownLinkMaxSpeed;

    // 内存数据库配额限制
    @Property("最大存储键值对数`")
    private Long cacheMaxKeys;

    // 应用（短信、收费api等）
    @Property("短信最大发送条数")
    private Integer smsMaxCount;
    @Property("实名认证最大次数")
    private Integer verifyMaxCount;
}
