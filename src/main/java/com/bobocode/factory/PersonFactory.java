package com.bobocode.factory;

import com.bobocode.entity.Person;
import com.bobocode.orm.EntityKey;

public class PersonFactory {
    public static final Long DEFAULT_ID = 1L;
    public static final String DEFAULT_FIRST_NAME = "Bohdan";
    public static final String DEFAULT_LAST_NAME = "Bodnar";
    public static final String DEFAULT_EMAIL = DEFAULT_FIRST_NAME + "." + DEFAULT_LAST_NAME + "@gmail.com";

    public static final EntityKey<Person> DEFAULT_ENTITY_KEY = new EntityKey<>(Person.class, DEFAULT_ID);

    public static Person newDefaultPerson() {
        var person = new Person();
        person.setId(DEFAULT_ID);
        person.setFirstName(DEFAULT_FIRST_NAME);
        person.setLastName(DEFAULT_LAST_NAME);
        person.setEmail(DEFAULT_EMAIL);
        return person;
    }
}
