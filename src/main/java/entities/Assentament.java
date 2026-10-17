package entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assentament")
@DiscriminatorValue("assentament")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipus_assentament")
public abstract class Assentament extends Construccio {

	@OneToOne
	@Column(name = "vertex_id", unique = true, nullable = false)
	private Vertex vertex;

}
