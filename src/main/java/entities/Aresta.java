package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "aresta", uniqueConstraints = @UniqueConstraint(name = "UK_aresta_partida_codi", columnNames = { "partida_id", "codi" }))
public class Aresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int codi;

    // N:1 PROPIETARI. LAZY: referència de tornada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    /*
     * Dues FK cap a la MATEIXA entitat (Vertex): dos @JoinColumn amb NOMS DIFERENTS.
     * EAGER: una aresta sempre s'usa amb els seus dos vèrtexs (adjacència, connectivitat),
     * són dos objectes petits i Vertex no arrossega cap EAGER.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_a_id", nullable = false)
    private Vertex vertexA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_b_id", nullable = false)
    private Vertex vertexB;

    public Aresta() {
    }

    public Aresta(int codi, Vertex vertexA, Vertex vertexB) {
        this.codi = codi;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
    }

    public Integer getId() { return id; }
    public int getCodi() { return codi; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public Vertex getVertexA() { return vertexA; }
    public Vertex getVertexB() { return vertexB; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aresta other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Aresta.class.hashCode();
    }

    @Override
    public String toString() {
        return "Aresta[id=" + id + ", codi=" + codi + "]";
    }
}
