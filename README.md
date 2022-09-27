# fluent-sql

基于Fluent Api实现的SQL构建器，秒杀mybatis plus的存在，易用性的API让你爽到飞起。

## 特性介绍

1. 实现了SQL底层的语法解析，创新使用片段的构建模式，忽略嵌套层级任意嵌套
2. 高质量的代码，没有一点点冗余的设计，为您的代码保驾护航
3. 天生自带防SQL注入和条件空策略解析，后续会增加更加精细的配置
4. 支持任意多表的关联查询和数据绑定
5. 支持返回实体映射，基于注解式开发，更解耦，更面向对象
6. 智能别名策略，写查询再也不用担心多张表的别名问题，代码简介易懂，用java跟sql体验直接拉满
7. 高精度api控制，sql构建每个步骤严格把关，保证输入一个api立即能写出来接下来的步骤还不出错

## 快速接入使用

### SpringBoot
如果您的项目使用maven，并且使用spring-boot，可通过以下配置快速集成：
```xml
 <dependency>
    <artifactId>fluent-sql-spring-boot-starter</artifactId>
    <groupId>group.flyfish.framework</groupId>
    <version>0.0.5</version>
</dependency>
```

### Spring MVC
没有使用spring boot也没关系，如果项目使用spring，请只依赖它：
```xml
 <dependency>
    <artifactId>fluent-sql-spring-jdbc</artifactId>
    <groupId>group.flyfish.framework</groupId>
    <version>0.0.5</version>
</dependency>
```

然后，您只需要注入您的数据源即可自动完成配置。

```java
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import group.flyfish.fluent.operations.FluentSQLOperations;
import group.flyfish.fluent.operations.JdbcTemplateFluentSQLOperations;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class FluentSqlConfig {

    /**
     * 实际应用中，使用bean注入并实例化
     *
     * @param dataSource 从spring datasource注入
     */
    @Bean
    public FluentSQLOperations fluentSQLOperations(DataSource dataSource) {
        return new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));
    }
}
```

## 对比直接书写SQL

本小组件主要解决的是sql的书写问题，旨在用更加优雅的方式实现sql，并且不用再担心数据库方言（SQL Dialect）
变化导致的频繁变更SQL问题。

如果要实现下面一段SQL

```sql
SELECT t1.`id`         AS `id`,
       t1.`name`       AS `name`,
       t1.`identifier` AS `identifier`,
       `max_tenant`    AS `maxTenant`,
       t2.`related_id` AS `relatedId`
FROM saas_tenant `t1`
         INNER JOIN saas_quota `t2` ON t2.`related_id` = t1.`id`
    AND t1.`identifier` = ?
WHERE t1.`id` = ?
  AND t1.`name` LIKE CONCAT(%,?,%)
  AND t2.`related_id` IN (?, ?)
  AND t2.`related_type` = ?
ORDER BY t1.`create_time` DESC,
         t1.`id` ASC
```

您只需要写以下java代码：

```java
public class TestSql {

    /**
     * 执行sql测试
     * @return 最终组装的实体
     */
    public TenantContext executeSql() {
        // 查询开始
        return select(
                // 某张表的几个字段
                composite(SaasTenant::getId, SaasTenant::getName, SaasTenant::getIdentifier),
                // 某张表的全量字段
                all(SaasProperties.class),
                // 其他表的字段
                composite(SaasQuota::getId, SaasQuota::getRelatedType),
                // 指定别名的字段
                composite(SaasQuota::getRelatedId, "relatedOtherId"))
                .from(SaasTenant.class)
                .join(SaasQuota.class).on(where(SaasQuota::getRelatedId).eq(SaasTenant::getId)
                        .and(SaasTenant::getIdentifier).eq(5))
                .join(SaasProperties.class)
                .then()
                .matching(where(SaasTenant::getId).eq("1")
                        .and(SaasTenant::getName).like("王大锤")
                        .and(SaasQuota::getRelatedId).in(Arrays.asList("5", "10"))
                        .and(SaasQuota::getRelatedType).eq(SaasQuota.RelatedType.TENANT)
                )
                .order(by(SaasTenant::getCreateTime).desc(), by(SaasTenant::getId).asc())
                .one(TenantContext.class);
    }
}
```

## 常规步骤

建议使用静态导入，美观代码，如下：

1. SQL.select => select
2. SelectComposite.composite => composite
3. SelectComposite.all => all
4. Order.by => by
5. Query.where => where

为了方便演示，我们下面的代码都基于静态导入函数：

1. 绑定您执行sql的数据源。如下：

```java
class SQLConfig {

    void doConfig() {
        // 创建或者获取您的数据源
        DataSource = createDataSource(...)
        // 基于spring jdbc template实例化
        SQL.bind(new JdbcTemplate(dataSource));
    }
}

```

2. 写一个结果对象Vo用来接取执行结果，如快速开始中的`TenantContext`
3. 用java写sql，然后执行`.one()`或者`.list()`返回一条或者多条。
4. 查询单表所有字段，请使用`select().from(TableClass.class)`
5. 查询多表中某个单表的所有字段，请使用`select(all(TableClass.class)).from(TableClass.class).join(Other.class).then()`
6.
多张表中查询某些字段，并使用别名，请参考`select(composite(TableA::getName, "nameA"), composite(TableB::getName, "nameB")).from(TableA.class).join(TableB.class)...`
