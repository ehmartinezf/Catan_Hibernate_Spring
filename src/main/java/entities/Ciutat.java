package entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ciutat")
@DiscriminatorValue("ciutat")
public class Ciutat extends Assentament {

}
