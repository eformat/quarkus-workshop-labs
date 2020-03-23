package org.acme.people.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.event.Observes;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.transaction.Transactional;
import org.acme.people.utils.CuteNameGenerator;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.runtime.StartupEvent;

@Entity
public class Person extends PanacheEntity {
    // the person's name
    public String name;

    // the person's birthdate
    public LocalDate birth;

    // the person's eye color
    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    public EyeColor eyes;

    public static List<Person> findByColor(EyeColor color) {
        return list("eyes", color);
    }
    
    public static List<Person> getBeforeYear(int year) {
        return Person.<Person>streamAll()
        .filter(p -> p.birth.getYear() <= year)
        .collect(Collectors.toList());
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        for (int i = 0; i < 1000; i++) {
            String name = CuteNameGenerator.generate();
            LocalDate birth = LocalDate.now().plusWeeks(Math.round(Math.floor(Math.random() * 20 * 52 * -1)));
            EyeColor color = EyeColor.values()[(int)(Math.floor(Math.random() * EyeColor.values().length))];
            Person p = new Person();
            p.birth = birth;
            p.eyes = color;
            p.name = name;
            Person.persist(p);
        }
    }    
}