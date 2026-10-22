package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "poblat")
public class Poblat extends Assentament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

}
