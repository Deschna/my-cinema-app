package cinema.config;

import cinema.security.BCryptLengthLimitedPasswordEncoder;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@PropertySource("file:${CINEMA_DB_PROPERTIES_FILE:config/db.properties}")
@ComponentScan(basePackages = {
        "cinema.dao",
        "cinema.security",
        "cinema.service"
})
@Import(DataInitializer.class)
public class RootConfig {
    private final Environment environment;

    public RootConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(requireNonBlankProperty("db.driver"));
        dataSource.setUrl(requireNonBlankProperty("db.url"));
        dataSource.setUsername(requireNonBlankProperty("db.user"));
        dataSource.setPassword(requireNonBlankProperty("db.password"));
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(dataSource());

        Properties properties = new Properties();
        properties.put("hibernate.show_sql", requireNonBlankProperty("hibernate.show_sql"));
        properties.put("hibernate.dialect", requireNonBlankProperty("hibernate.dialect"));
        properties.put("hibernate.hbm2ddl.auto", requireNonBlankProperty("hibernate.hbm2ddl.auto"));

        factoryBean.setHibernateProperties(properties);
        factoryBean.setPackagesToScan("cinema.model");
        return factoryBean;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptLengthLimitedPasswordEncoder();
    }

    private String requireNonBlankProperty(String propertyName) {
        String propertyValue = environment.getProperty(propertyName);
        if (propertyValue == null || propertyValue.isBlank()) {
            throw new IllegalStateException("Required configuration property '"
                    + propertyName + "' is missing or blank");
        }
        return propertyValue;
    }
}
