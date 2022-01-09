package com.bobocode.orm;

public record EntityKey<T extends BaseEntity>(Class<T> type, Object id) {
}
