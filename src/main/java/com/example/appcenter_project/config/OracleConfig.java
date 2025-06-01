package com.example.appcenter_project.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Profile("!test")
public class OracleConfig {

    @Value("${schoolDbUrl}")
    private String url;
    @Value("${schoolDbUser}")
    private String username;
    @Value("${schoolDbPassword}")
    private String password;
    @Bean(name = "oracleDataSource")
    @ConfigurationProperties(prefix = "school.datasource")
    public DataSource secondDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean(name = "oracleJdbc")
    public JdbcTemplate jdbcTemplate(@Qualifier("oracleDataSource")DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

}