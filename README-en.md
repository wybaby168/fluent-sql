# Preface

As developers, we've likely all used MyBatis at some point. But speaking for myself, despite its widespread use, I've never truly grown fond of it. While I appreciate the elegance of object-oriented programming, MyBatis often pulls me back into the realm of SQL-oriented thinking. 

Driven by a desire for a more streamlined and elegant approach to SQL development, I created **Fluent SQL**.

# What is Fluent SQL?

Let's break down the word "fluent."

| fluent | English [ˈfluːənt] |
| ------ | --------------------- |
| adj.   | (of a person) able to express themselves easily and accurately in a particular language; (of a language) spoken or written easily and accurately |

As the name suggests, Fluent SQL aims to enable writing **"elegant and natural SQL"** in a **fluent** manner. This lightweight component can readily integrate into your projects, offering a compelling alternative to MyBatis XML.

As Java developers, we prefer handling logic within Java code instead of embedding it in XML and resorting to SQL-oriented programming. The year is 2023, and it's time to explore more elegant solutions.

# Introducing the Component

Fluent SQL is an SQL builder based on the Fluent API design pattern, offering an intuitive and user-friendly API that simplifies SQL construction.

## Features

1. **Sophisticated SQL Parsing:** Implements underlying SQL grammar parsing with an innovative fragment-based building approach, enabling seamless nesting regardless of complexity.
2. **High-Quality Code:**  Designed with a focus on clean and concise code, minimizing redundancy and ensuring robustness.
3. **Built-in SQL Injection Protection:** Inherently safeguards against SQL injection and handles empty condition scenarios, with further enhancements planned for more granular configuration.
4. **Comprehensive Relational Queries:**  Supports arbitrary multi-table joins and data binding.
5. **Entity Mapping with Annotations:** Enables result mapping to entities, promoting decoupling and object-oriented principles through annotation-based development.
6. **Intelligent Alias Strategy:** Simplifies query writing by automatically managing aliases across multiple tables, making your code more concise and readable while providing a seamless Java-to-SQL experience.
7. **Precise API Control:** Offers fine-grained control over SQL construction, ensuring each API call seamlessly guides you through the process while preventing errors.

## Quick Start Guide

### Spring Boot Integration

For Maven-based Spring Boot projects, easily integrate Fluent SQL with the following dependency:

```xml
 <dependency>
    <artifactId>fluent-sql-spring-boot-starter</artifactId>
    <groupId>group.flyfish.framework</groupId>
    <version>0.1.0</version>
</dependency>
```

### Spring MVC Integration

For projects using Spring without Spring Boot, simply include the following dependency:

```xml
 <dependency>
    <artifactId>fluent-sql-spring-jdbc</artifactId>
    <groupId>group.flyfish.framework</groupId>
    <version>0.1.0</version>
</dependency>
```

Then, inject your data source to automatically configure Fluent SQL:

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
     * In a real-world application, instantiate using bean injection
     *
     * @param dataSource Inject from the Spring data source
     */
    @Bean
    public FluentSQLOperations fluentSQLOperations(DataSource dataSource) {
        return new JdbcTemplateFluentSQLOperations(new JdbcTemplate(dataSource));
    }
}
```

## Single Table Query

For single table queries, result mapping can be omitted, and the program automatically maps to the main table entity.

```java
class Test {
    
    public static void main(String[] args) {
        // Select all fields from a single table
        List<SaasTenant> tenants = select().from(SaasTenant.class).list();
        // Select specific fields (id, name) with a condition (age < 50)
        List<SaasTenant> points = select(SaasTenant::getId, SaasTenant::getName).from(SaasTenant.class)
                .matching(where(SaasTenant::getAge).lt(50))
                .list();
    }
}
```

## Comparison with Writing SQL Directly

This component primarily addresses the challenges of writing SQL, aiming to provide a more elegant way to construct SQL statements without concerns about database dialect variations.

To achieve the following SQL:

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

You can simply write the following Java code:

```java
public class TestSql {

    /**
     * Execute SQL test
     * @return The final assembled entity
     */
    public TenantContext executeSql() {
        // Query starts here
        return select(
                // Specific fields from a table
                composite(SaasTenant::getId, SaasTenant::getName, SaasTenant::getIdentifier),
                // All fields from a table
                all(SaasProperties.class),
                // Fields from other tables
                composite(SaasQuota::getId, SaasQuota::getRelatedType),
                // Fields with specified aliases
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

## General Steps

Using static imports is recommended for cleaner code:

1. `SQL.select` => `select`
2. `SelectComposite.composite` => `composite`
3. `SelectComposite.all` => `all`
4. `Order.by` => `by`
5. `Query.where` => `where`

For demonstration purposes, the following code snippets assume static imports:

1. Bind your data source for SQL execution:

```java
class SQLConfig {

    void doConfig() {
        // Create or obtain your data source
        DataSource dataSource = createDataSource(...);
        // Instantiate based on Spring JDBC template
        SQL.bind(new JdbcTemplate(dataSource));
    }
}
```

2. Create a result object VO to receive the execution results, such as `TenantContext` in the Quick Start.
3. Write SQL using Java syntax and then call `.one()` or `.list()` to retrieve a single or multiple results.
4. To query all fields from a single table, use `select().from(TableClass.class)`.
5. To query all fields from a specific table in a multi-table join, use `select(all(TableClass.class)).from(TableClass.class).join(Other.class).then()`.
6. To query specific fields from multiple tables with aliases, refer to: `select(composite(TableA::getName, "nameA"), composite(TableB::getName, "nameB")).from(TableA.class).join(TableB.class)...`. 
