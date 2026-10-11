package cinema.config;

import cinema.model.Role;
import cinema.model.User;
import cinema.service.RoleService;
import cinema.service.UserService;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.hibernate.tool.schema.Action;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {
    private static final String HIBERNATE_DDL_AUTO_PROPERTY = "hibernate.hbm2ddl.auto";
    // Initialize required data only when Hibernate guarantees a freshly recreated schema.
    private static final Set<Action> SCHEMA_RECREATION_ACTIONS =
            Set.of(Action.CREATE, Action.CREATE_DROP);
    private static final String INITIAL_ADMIN_EMAIL = "admin@i.ua";
    private static final String INITIAL_ADMIN_PASSWORD = "admin123";

    private final UserService userService;
    private final RoleService roleService;
    private final Environment environment;

    public DataInitializer(UserService userService,
                           RoleService roleService,
                           Environment environment) {
        this.userService = userService;
        this.roleService = roleService;
        this.environment = environment;
    }

    @PostConstruct
    void initializeData() {
        Action schemaAction = Action.interpretHbm2ddlSetting(
                environment.getRequiredProperty(HIBERNATE_DDL_AUTO_PROPERTY)
        );
        if (!SCHEMA_RECREATION_ACTIONS.contains(schemaAction)) {
            return;
        }

        Role adminRole = new Role();
        adminRole.setRoleName(Role.RoleName.ADMIN);
        roleService.add(adminRole);

        Role userRole = new Role();
        userRole.setRoleName(Role.RoleName.USER);
        roleService.add(userRole);

        User administrator = new User();
        administrator.setEmail(INITIAL_ADMIN_EMAIL);
        administrator.setPassword(INITIAL_ADMIN_PASSWORD);
        administrator.setRoles(Set.of(adminRole));
        userService.add(administrator);
    }
}
