package com.bobocode.orm;


import com.bobocode.annotation.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class BaseEntity {

    @Column(name = "id")
    Object id;

    @Override
    public String toString() {
        return "id=" + id;
    }
}
