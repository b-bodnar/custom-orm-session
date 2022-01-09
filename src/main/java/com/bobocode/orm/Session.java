package com.bobocode.orm;

import com.bobocode.exception.OrmException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bobocode.orm.SqlBuilder.findByIdQuery;
import static com.bobocode.util.NameUtil.getColumnName;
import static java.util.Comparator.comparing;

@RequiredArgsConstructor
public class Session {
    private final DataSource dataSource;
    private final Map<EntityKey<?>, BaseEntity> cachedEntities = new HashMap<>();
    private final Map<EntityKey<?>, List<Object>> snapshot = new HashMap<>();

    /**
     * Search for an entity of the specified class and primary key.
     * If the entity instance is contained in the cache, it is returned from there.
     *
     * @param entityType - entity class
     * @param id         - primary key
     * @return the found entity instance or null if the entity does not exist
     */
    public <T extends BaseEntity> T find(Class<T> entityType, Object id) {
        var entityKey = new EntityKey<>(entityType, id);
        return findById(entityKey);
    }

    private <T extends BaseEntity> T findById(EntityKey<T> entityKey) {
        return cachedEntities.containsKey(entityKey)
                ? loadEntityFromCache(entityKey)
                : findEntityInDb(entityKey);
    }

    private <T extends BaseEntity> T loadEntityFromCache(EntityKey<T> entityKey) {
        return (T) cachedEntities.get(entityKey);
    }

    private <T extends BaseEntity> T findEntityInDb(EntityKey<T> entityKey) {
        try (Connection connection = dataSource.getConnection()) {
            var entity = prepareSelectByIdStatement(entityKey, connection);
            cachedEntities.put(entityKey, entity);
            snapshot.put(entityKey, getFieldValues(entity));
            return entity;
        } catch (SQLException e) {
            throw new OrmException("Cannot open connection to DB", e);
        }
    }

    private <T extends BaseEntity> T prepareSelectByIdStatement(EntityKey<T> entityKey, Connection connection) {
        var id = entityKey.id();
        try (PreparedStatement preparedStatement = connection.prepareStatement(findByIdQuery(entityKey))) {
            preparedStatement.setObject(1, id);
            var resultSet = preparedStatement.executeQuery();
            return parsRow(entityKey, resultSet);
        } catch (SQLException e) {
            throw new OrmException(String.format("Cannot prepare select by id statement for id = %b", id), e);
        }
    }

    private <T extends BaseEntity> T parsRow(EntityKey<T> entityKey, ResultSet resultSet) {
        try {
            resultSet.next();
            return createEntityFromResultSet(entityKey.type(), resultSet);
        } catch (SQLException e) {
            throw new OrmException(
                    String.format("Cannot get row record from DB for %s with id %b", entityKey.type(), entityKey.id()), e);
        }
    }

    @SneakyThrows
    private <T extends BaseEntity> T createEntityFromResultSet(Class<T> type, ResultSet resultSet) {
        var entity = type.getDeclaredConstructor().newInstance();
        for (Field field : getFieldsFromObject(type)) {
            field.setAccessible(true);
            var columnName = getColumnName(field);
            if (columnName.isPresent()) {
                field.set(entity, resultSet.getObject(columnName.get()));
            } else {
                field.set(entity, resultSet.getObject(field.getName()));
            }
        }
        return entity;
    }

    private List<Field> getFieldsFromObject(Class<?> objType) {
        return Stream.of(Arrays.asList(objType.getDeclaredFields()),
                        Arrays.asList(objType.getSuperclass().getDeclaredFields()))
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     *
     * @param entity - entity for update
     */
    public <T extends BaseEntity> void update(T entity) {
        Objects.requireNonNull(entity);
        try (Connection connection = dataSource.getConnection()) {
            updateEntity(entity, connection);
        } catch (SQLException e) {
            throw new OrmException("Cannot open connection to DB", e);
        }
    }

    @SneakyThrows
    private <T extends BaseEntity> void updateEntity(T entity, Connection connection) {
        try (var preparedStatement = connection.prepareStatement(SqlBuilder.updateQuery(entity))) {
            var fields = entity.getClass().getDeclaredFields();
            AtomicInteger index = new AtomicInteger(1);

            Stream.of(fields)
                    .sorted(comparing(Field::getName))
                    .forEach(fillPreparedStatement(entity, preparedStatement, index));
            preparedStatement.setObject(index.get(), entity.id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException(String.format("Cannot prepare update statement for %s", entity), e);
        }
    }

    private <T extends BaseEntity> Consumer<Field> fillPreparedStatement(
            T entity, PreparedStatement preparedStatement, AtomicInteger index) {
        return field -> {
            field.setAccessible(true);
            try {
                preparedStatement.setObject(index.getAndIncrement(), field.get(entity));
            } catch (SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
        };
    }

    public void close() {
        collectChangedEntities().forEach(this::update);
        cachedEntities.clear();
        snapshot.clear();
    }

    private List<BaseEntity> collectChangedEntities() {
        return cachedEntities.entrySet().stream()
                .filter(this::isChanged)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<Object> getFieldValues(BaseEntity entity) {
        List<Object> fieldValues = new ArrayList<>();
        List<Field> sortedFields = Arrays.stream(entity.getClass().getDeclaredFields())
                .sorted(comparing(Field::getName)).toList();

        for (Field field : sortedFields) {
            field.setAccessible(true);
            fieldValues.add(field.get(entity));
        }
        return fieldValues;
    }

    private boolean isChanged(Map.Entry<EntityKey<?>, BaseEntity> cachedObject) {
        List<Object> currentFieldValues = getFieldValues(cachedObject.getValue());
        List<Object> snapshotFieldValues = snapshot.get(cachedObject.getKey());

        for (int i = 0; i < currentFieldValues.size(); i++) {
            if (!currentFieldValues.get(i).equals(snapshotFieldValues.get(i))) {
                return true;
            }
        }
        return false;
    }
}
