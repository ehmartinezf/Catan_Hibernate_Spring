package entities;

import enums.TipusTerreny;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "hexagon")
@Inheritance(strategy = InheritanceType.JOINED)
public class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codi", nullable = false, unique = true, length = 10)
    private int codi;

    @Enumerated(EnumType.STRING)
    @Column(name = "terreny", nullable = false, length = 20)
    private TipusTerreny terreny;

    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Column(name = "q", nullable = false)
    private int q;

    @Column(name = "r", nullable = false)
    private int r;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "partida_id", nullable = false, unique = true)
    private Partida partida;

    @OneToMany(mappedBy = "hexagon")
    private Set<HexagonVertex> hexagonVertexes;

    public Hexagon() {
    }

    public Hexagon(int codi, int q, int r) {
        this.codi = codi;
        this.q = q;
        this.r = r;
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

    public TipusTerreny getTerreny() {
        return terreny;
    }

    public void setTerreny(TipusTerreny terreny) {
        this.terreny = terreny;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Set<HexagonVertex> getHexagonVertexes() {
        return hexagonVertexes;
    }

    public void setHexagonVertexes(Set<HexagonVertex> hexagonVertexes) {
        this.hexagonVertexes = hexagonVertexes;
    }

    public void afegirHexagonVertex(HexagonVertex hexagonVertex) {
        this.hexagonVertexes.add(hexagonVertex);
        hexagonVertex.setHexagon(this);
    }

    public void eliminarHexagonVertex(HexagonVertex hexagonVertex) {
        this.hexagonVertexes.remove(hexagonVertex);
    }

    @Override
    public int hashCode() {
        return Hexagon.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Hexagon other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

}
