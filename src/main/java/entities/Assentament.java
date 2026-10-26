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

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	
	@Override
	public int hashCode() {
		return Assentament.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Assentament other))
			return false;
		return getId() != null && getId().equals(other.getId());
	}

}
