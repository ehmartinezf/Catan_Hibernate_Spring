package entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "poblat")
@DiscriminatorValue("poblat")
public class Poblat extends Assentament {

}