package entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "carretara")
@DiscriminatorValue("carretera")
public class Carretera extends Construccio {

	@OneToOne(mappedBy = "")
	@Column(name = "aresta_id", unique = true, nullable = false)
	private Aresta aresta;

}
