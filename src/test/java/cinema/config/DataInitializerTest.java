package cinema.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cinema.model.Role;
import cinema.model.User;
import cinema.service.RoleService;
import cinema.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.env.MockEnvironment;

class DataInitializerTest {
    @ParameterizedTest
    @ValueSource(strings = {"create", "create-drop", "drop-and-create", " create "})
    void initializesDataWhenHibernateRecreatesSchema(String schemaAction) {
        RecordingRoleService roleService = new RecordingRoleService();
        RecordingUserService userService = new RecordingUserService();
        DataInitializer initializer = new DataInitializer(
                userService,
                roleService,
                environmentWithSchemaAction(schemaAction)
        );

        initializer.initializeData();

        assertEquals(List.of(Role.RoleName.ADMIN, Role.RoleName.USER),
                roleService.roles.stream().map(Role::getRoleName).toList());
        assertEquals(1, userService.users.size());
        User administrator = userService.users.get(0);
        assertEquals("admin@i.ua", administrator.getEmail());
        assertEquals("admin123", administrator.getPassword());
        assertEquals(List.of(Role.RoleName.ADMIN),
                administrator.getRoles().stream().map(Role::getRoleName).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"validate", "update", "none", "drop", "create-only"})
    void skipsDataInitializationWhenSchemaIsNotRecreated(String schemaAction) {
        RecordingRoleService roleService = new RecordingRoleService();
        RecordingUserService userService = new RecordingUserService();
        DataInitializer initializer = new DataInitializer(
                userService,
                roleService,
                environmentWithSchemaAction(schemaAction)
        );

        initializer.initializeData();

        assertEquals(List.of(), roleService.roles);
        assertEquals(List.of(), userService.users);
    }

    private static MockEnvironment environmentWithSchemaAction(String schemaAction) {
        return new MockEnvironment().withProperty("hibernate.hbm2ddl.auto", schemaAction);
    }

    private static class RecordingRoleService implements RoleService {
        private final List<Role> roles = new ArrayList<>();

        @Override
        public Role add(Role role) {
            roles.add(role);
            return role;
        }

        @Override
        public Role getByName(String roleName) {
            throw new UnsupportedOperationException();
        }
    }

    private static class RecordingUserService implements UserService {
        private final List<User> users = new ArrayList<>();

        @Override
        public User add(User user) {
            users.add(user);
            return user;
        }

        @Override
        public User get(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            throw new UnsupportedOperationException();
        }
    }
}
