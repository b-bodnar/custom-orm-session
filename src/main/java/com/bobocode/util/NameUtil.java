package com.bobocode.util;

import com.bobocode.annotation.Column;
import com.bobocode.annotation.Table;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Optional;

@UtilityClass
public class NameUtil {

    public static String getTableName(Class<?> entityType) {
        return Optional.ofNullable(entityType.getDeclaredAnnotation(Table.class))
                .map(Table::name)
                .orElseGet(entityType::getSimpleName);
    }

    public static Optional<String> getColumnName(Field field) {
        return Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name);
    }
}
