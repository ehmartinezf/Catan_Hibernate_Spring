package entities;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vertex")
public class Vertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "codi", nullable = false)
    private int codi;

    @OneToOne(mappedBy = "vertex", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Assentament assentament;

    @OneToMany(mappedBy = "vertex", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<HexagonVertex> hexagonVertex;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id")
    private Partida partida;

    public Vertex() {
    }

    public Vertex(int codi) {
        this.codi = codi;
    }

    public void afegirHexagonVertex(HexagonVertex hexagonVertex) {
        this.hexagonVertex.add(hexagonVertex);
        hexagonVertex.setVertex(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodi() {
        return codi;
    }

    public void setCodi(int codi) {
        this.codi = codi;
    }

    public Assentament getAssentament() {
        return assentament;
    }

    public void setAssentament(Assentament assentament) {
        this.assentament = assentament;
    }

    public Set<HexagonVertex> getHexagonVertex() {
        return hexagonVertex;
    }

    public void setHexagonVertex(Set<HexagonVertex> hexagonVertex) {
        this.hexagonVertex = hexagonVertex;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    @Override
    public int hashCode() {
        return Vertex.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vertex other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

}
