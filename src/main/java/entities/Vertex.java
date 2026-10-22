package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/*
 * Vèrtex disponible per construir un poblat: lliure, que compleixi la regla de la distància
 * i (fora de PREPARACIO) connectat a una carretera pròpia. Es resol TOT amb JPQL (no es recorren els vèrtexs a Java).
 */
@Entity
@Table(name = "vertex", uniqueConstraints = @UniqueConstraint(name = "UK_vertex_partida_codi", columnNames = { "partida_id", "codi" }))
@NamedQuery(name = "Vertex.disponiblesPreparacio", query = """
        select v from Vertex v
        where v.partida.id = :idPartida
          and not exists (select a.id from Assentament a where a.vertex.id = v.id)
          and not exists (select ar.id from Aresta ar, Assentament a2
                          where (ar.vertexA.id = v.id and a2.vertex.id = ar.vertexB.id)
                             or (ar.vertexB.id = v.id and a2.vertex.id = ar.vertexA.id))
        order by v.codi
        """)
@NamedQuery(name = "Vertex.disponibles", query = """
        select v from Vertex v
        where v.partida.id = :idPartida
          and not exists (select a.id from Assentament a where a.vertex.id = v.id)
          and not exists (select ar.id from Aresta ar, Assentament a2
                          where (ar.vertexA.id = v.id and a2.vertex.id = ar.vertexB.id)
                             or (ar.vertexB.id = v.id and a2.vertex.id = ar.vertexA.id))
          and exists (select c.id from Carretera c
                      where c.propietari.id = :idJugador
                        and (c.aresta.vertexA.id = v.id or c.aresta.vertexB.id = v.id))
        order by v.codi
        """)
public class Vertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int codi;

    // N:1 PROPIETARI. LAZY: referència de tornada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    public Vertex() {
    }

    public Vertex(int codi) {
        this.codi = codi;
    }

    public Integer getId() { return id; }
    public int getCodi() { return codi; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vertex other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Vertex.class.hashCode();
    }

    @Override
    public String toString() {
        return "Vertex[id=" + id + ", codi=" + codi + "]";
    }
}
