package io.github.lflosantos.quarkussocial.domain.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@Data
public class User extends PanacheEntityBase {
    //NÃO precisa quando extende PanacheEntity, mas como o ID é incremental no BD temos que extender PanacheEntityBase pq não tem a coluna ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")//necessario só se a coluna no BD tiver nome diferente
    private String name;

    @Column
    private Integer age;

    public User() {
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

}
