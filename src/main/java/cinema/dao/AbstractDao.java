package cinema.dao;

import cinema.exception.DataProcessingException;
import cinema.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public abstract class AbstractDao<T> {
    protected final SessionFactory factory;
    private final Class<T> clazz;

    public AbstractDao(SessionFactory factory, Class<T> clazz) {
        this.factory = factory;
        this.clazz = clazz;
    }

    public T add(T t) {
        return executeInTransaction(session -> {
            session.save(t);
            return t;
        }, "Error during insert operation for " + clazz.getSimpleName());
    }

    public Optional<T> get(Long id) {
        try (Session session = factory.openSession()) {
            return Optional.ofNullable(session.get(clazz, id));
        } catch (Exception e) {
            throw new DataProcessingException("Error during lookup operation for "
                    + clazz.getSimpleName() + ", id: " + id, e);
        }
    }

    public List<T> getAll() {
        try (Session session = factory.openSession()) {
            return session.createQuery("from " + clazz.getSimpleName(), clazz).getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Error during list operation for "
                    + clazz.getSimpleName(), e);
        }
    }

    public T update(T t) {
        return executeInTransaction(session -> {
            Object id = Objects.requireNonNull(factory.getPersistenceUnitUtil().getIdentifier(t),
                    "Entity identifier must not be null");
            // Select only the ID to avoid loading an entity that would conflict
            // with session.update(t).
            Long existingId = session.createQuery("select e.id from " + clazz.getSimpleName()
                            + " e where e.id = :id", Long.class)
                    .setParameter("id", id)
                    .setLockOptions(new LockOptions(LockMode.PESSIMISTIC_WRITE))
                    .uniqueResult();
            if (existingId == null) {
                throw new ResourceNotFoundException(
                        clazz.getSimpleName() + " not found by id: " + id
                );
            }
            session.update(t);
            return t;
        }, "Error during update operation for " + clazz.getSimpleName());
    }

    public void delete(Long id) {
        executeInTransaction(session -> {
            T entity = session.get(clazz, id, LockMode.PESSIMISTIC_WRITE);
            if (entity == null) {
                throw new ResourceNotFoundException(
                        clazz.getSimpleName() + " not found by id: " + id
                );
            }
            session.delete(entity);
            return null;
        }, "Error during delete operation for " + clazz.getSimpleName() + ", id: " + id);
    }

    protected <R> R executeInTransaction(Function<Session, R> operation, String errorMessage) {
        try (Session session = factory.openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                R result = operation.apply(session);
                transaction.commit();
                return result;
            } catch (RuntimeException e) {
                if (transaction != null) {
                    try {
                        transaction.rollback();
                    } catch (RuntimeException rollbackFailure) {
                        e.addSuppressed(rollbackFailure);
                    }
                }
                throw e;
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DataProcessingException(errorMessage, e);
        }
    }
}
