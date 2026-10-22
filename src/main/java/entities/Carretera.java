package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * Carretera hereta DIRECTAMENT de Construccio (no és un assentament).
 */
@Entity
@Table(name = "carretera")
@NamedQuery(name = "Carretera.perAresta", query = "select count(c) from Carretera c where c.aresta.id = :idAresta")
// Carreteres d'un jugador que arriben a un vèrtex (per una de les dues puntes de l'aresta)
@NamedQuery(name = "Carretera.tocaVertex", query = """
        select count(c) from Carretera c
        where c.propietari.id = :idJugador
          and (c.aresta.vertexA.id = :idVertex or c.aresta.vertexB.id = :idVertex)
        """)
public class Carretera extends Construccio {

    // 1:1 UNIDIRECCIONAL, FK aresta_id aquí. unique: una aresta només pot tenir una carretera. EAGER pel mateix motiu que Assentament.vertex.
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "aresta_id", nullable = false, unique = true)
    private Aresta aresta;

    public Carretera() {
    }

    public Carretera(Jugador propietari, Aresta aresta) {
        super(propietari);
        this.aresta = aresta;
    }

    public Aresta getAresta() { return aresta; }
}
