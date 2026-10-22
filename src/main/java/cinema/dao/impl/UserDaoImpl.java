package cinema.dao.impl;

import cinema.dao.AbstractDao;
import cinema.dao.MySqlConstraintErrors;
import cinema.dao.UserDao;
import cinema.exception.DataProcessingException;
import cinema.exception.DuplicateEmailException;
import cinema.model.User;
import java.util.Optional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends AbstractDao<User> implements UserDao {
    private static final String USER_TABLE_NAME = "users";
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    public UserDaoImpl(SessionFactory factory) {
        super(factory, User.class);
    }

    @Override
    public User add(User user) {
        try {
            return super.add(user);
        } catch (DataProcessingException e) {
            // Treat only email constraint violations as duplicate emails.
            if (MySqlConstraintErrors.isDuplicateKey(e, USER_TABLE_NAME, EMAIL_UNIQUE_CONSTRAINT)) {
                throw new DuplicateEmailException(
                        "Error during insert operation for User; email already exists", e);
            }
            throw e;
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = factory.openSession()) {
            Query<User> findByEmail = session.createQuery(
                    "FROM User u JOIN FETCH u.roles WHERE u.email = :email", User.class);
            findByEmail.setParameter("email", email);
            return findByEmail.uniqueResultOptional();
        } catch (Exception e) {
            throw new DataProcessingException("Error during lookup operation for User by email", e);
        }
    }
}
