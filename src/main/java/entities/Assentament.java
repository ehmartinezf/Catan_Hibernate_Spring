package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assentament")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Assentament extends Construccio {

    @OneToOne
    @JoinColumn(name = "vertex_id", unique = true, nullable = false)
    private Vertex vertex;

}
