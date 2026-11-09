package entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Check;

@Entity
@Table(name = "aresta")
@Check(constraints = "codi >= 1 AND codi <= 72")
public class Aresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codi", nullable = false)
    private int codi;

    @OneToOne(mappedBy = "aresta", fetch = FetchType.EAGER)
    private Carretera carretera;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_a", nullable = false)
    private Vertex vertexA;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_b", nullable = false)
    private Vertex vertexB;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    public Aresta() {}

    public Aresta(int codi, Vertex vertexA, Vertex vertexB) {
        this.codi = codi;
        this.vertexA = vertexA;
        this.vertexB = vertexB;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(int codi) {
        this.codi = codi;
    }

    public Carretera getCarretera() {
        return carretera;
    }

    public void setCarretera(Carretera carretera) {
        this.carretera = carretera;
    }

    public Vertex getVertexA() {
        return vertexA;
    }

    public void setVertexA(Vertex vertexA) {
        this.vertexA = vertexA;
    }

    public Vertex getVertexB() {
        return vertexB;
    }

    public void setVertexB(Vertex vertexB) {
        this.vertexB = vertexB;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    @Override
    public int hashCode() {
        return Aresta.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Aresta other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }
}
