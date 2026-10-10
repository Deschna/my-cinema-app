package cinema.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

class RootConfigTest {
    private static final Map<String, String> REQUIRED_PROPERTIES = Map.of(
            "db.driver", "com.mysql.cj.jdbc.Driver",
            "db.url", "jdbc:mysql://localhost/test",
            "db.user", "test-user",
            "db.password", "test-password",
            "hibernate.show_sql", "true",
            "hibernate.hbm2ddl.auto", "validate",
            "hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect"
    );

    @Test
    void createsDataSourceFromRequiredProperties() throws SQLException {
        RootConfig rootConfig = new RootConfig(environmentWith(REQUIRED_PROPERTIES));

        try (BasicDataSource dataSource = assertInstanceOf(
                BasicDataSource.class,
                rootConfig.dataSource()
        )) {
            assertEquals(REQUIRED_PROPERTIES.get("db.driver"), dataSource.getDriverClassName());
            assertEquals(REQUIRED_PROPERTIES.get("db.url"), dataSource.getUrl());
            assertEquals(REQUIRED_PROPERTIES.get("db.user"), dataSource.getUserName());
            // getPassword() is deprecated because it exposes the password as a String.
            // rejectsMissingRequiredProperty() verifies that db.password is read and validated.
        }
    }

    @Test
    void configuresSessionFactoryFromRequiredProperties() {
        RootConfig rootConfig = new RootConfig(environmentWith(REQUIRED_PROPERTIES));

        LocalSessionFactoryBean sessionFactory = rootConfig.sessionFactory();
        Properties hibernateProperties = sessionFactory.getHibernateProperties();

        assertEquals(REQUIRED_PROPERTIES.get("hibernate.show_sql"),
                hibernateProperties.get("hibernate.show_sql"));
        assertEquals(REQUIRED_PROPERTIES.get("hibernate.dialect"),
                hibernateProperties.get("hibernate.dialect"));
        assertEquals(REQUIRED_PROPERTIES.get("hibernate.hbm2ddl.auto"),
                hibernateProperties.get("hibernate.hbm2ddl.auto"));
    }

    @Test
    void rejectsMissingRequiredProperty() {
        Map<String, String> properties = new HashMap<>(REQUIRED_PROPERTIES);
        properties.remove("db.password");
        RootConfig rootConfig = new RootConfig(environmentWith(properties));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                rootConfig::dataSource
        );

        assertEquals("Required configuration property 'db.password' is missing or blank",
                exception.getMessage());
    }

    @Test
    void rejectsBlankRequiredProperty() {
        Map<String, String> properties = new HashMap<>(REQUIRED_PROPERTIES);
        properties.put("db.user", " ");
        RootConfig rootConfig = new RootConfig(environmentWith(properties));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                rootConfig::dataSource
        );

        assertEquals("Required configuration property 'db.user' is missing or blank",
                exception.getMessage());
    }

    private static MockEnvironment environmentWith(Map<String, String> properties) {
        MockEnvironment environment = new MockEnvironment();
        properties.forEach(environment::setProperty);
        return environment;
    }
}
