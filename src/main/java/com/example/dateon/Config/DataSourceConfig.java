package com.example.dateon.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:}") String defaultUrl,
            @Value("${spring.datasource.username:}") String defaultUsername,
            @Value("${spring.datasource.password:}") String defaultPassword,
            @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}") String driverClassName) {

        String url = defaultUrl;
        String username = defaultUsername;
        String password = defaultPassword;

        // Check if environment has standard cloud DATABASE_URL or INTERNAL_DATABASE_URL
        String cloudDbUrl = System.getenv("DATABASE_URL");
        if (cloudDbUrl == null || cloudDbUrl.isBlank()) {
            cloudDbUrl = System.getenv("INTERNAL_DATABASE_URL");
        }

        if (cloudDbUrl != null && !cloudDbUrl.isBlank()) {
            if (cloudDbUrl.startsWith("postgres://") || cloudDbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(cloudDbUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && userInfo.contains(":")) {
                        String[] parts = userInfo.split(":", 2);
                        username = parts[0];
                        password = parts[1];
                    }
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    url = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + path;
                } catch (Exception e) {
                    System.err.println("Failed to parse cloud DATABASE_URL: " + e.getMessage());
                }
            } else {
                url = cloudDbUrl;
            }
        }

        if ((username == null || username.isBlank()) && System.getenv("DB_USER") != null) {
            username = System.getenv("DB_USER");
        }
        if ((password == null || password.isBlank()) && System.getenv("DB_PASS") != null) {
            password = System.getenv("DB_PASS");
        }
        if ((password == null || password.isBlank()) && System.getenv("RENDER_DB_PASSWORD") != null) {
            password = System.getenv("RENDER_DB_PASSWORD");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        return new HikariDataSource(config);
    }
}
