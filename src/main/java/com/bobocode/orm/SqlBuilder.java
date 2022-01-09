package com.bobocode.orm;

import com.bobocode.factory.PersonFactory;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;;
import java.util.Comparator;
import java.util.stream.Stream;

import static com.bobocode.util.NameUtil.getColumnName;
import static com.bobocode.util.NameUtil.getTableName;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

@UtilityClass
public class SqlBuilder {
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM %s WHERE id=?;";
    private static final String SQL_SELECT_ALL = "SELECT * FROM %s;";
    private static final String SQL_UPDATE = "UPDATE %s SET %s WHERE id=?;";

    /**
     * This method generate SDL queries for finding any entity by id
     * @param entityKey
     * @return SQL select query
     */
    public static String findByIdQuery(EntityKey<?> entityKey) {
        return String.format(SQL_SELECT_BY_ID, getTableName(entityKey.type()));
    }

    public static String findAllQuery(EntityKey<?> entityKey) {
        return String.format(SQL_SELECT_ALL, getTableName(entityKey.type()));
    }

    public static <T extends BaseEntity> String updateQuery(T entity) {
        return String.format(SQL_UPDATE,
                getTableName(entity.getClass()), getFieldNameFromEntityForUpdateQuery(entity));
    }

    private static String getFieldNameFromEntityForUpdateQuery(BaseEntity entity) {
        var fields = entity.getClass().getDeclaredFields();
        return Stream.of(fields)
                .sorted(comparing(Field::getName))
                .map(SqlBuilder::getFieldNameWithPlaceholderForUpdateQuery)
                .collect(joining(", "));
    }

    private static String getFieldNameWithPlaceholderForUpdateQuery(Field field) {
        field.setAccessible(true);
        return getColumnName(field).orElse(field.getName()) + "=" + "?";
    }

}
