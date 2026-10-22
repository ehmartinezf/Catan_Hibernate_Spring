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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/*
 * N:M AMB ATRIBUT (posicio) entre Hexagon i Vertex -> entitat intermèdia amb id propi
 * + UNIQUE (cap clau composta, ho exigeix l'enunciat).
 */
@Entity
@Table(name = "hexagon_vertex", uniqueConstraints = {
        @UniqueConstraint(name = "UK_hv_hexagon_posicio", columnNames = { "hexagon_id", "posicio" }),
        @UniqueConstraint(name = "UK_hv_hexagon_vertex", columnNames = { "hexagon_id", "vertex_id" }) })
@NamedQuery(name = "HexagonVertex.perPartida", query = """
        select hv from HexagonVertex hv
        join fetch hv.hexagon h
        join fetch hv.vertex v
        where h.partida.id = :idPartida
        order by h.codi, hv.posicio
        """)
@NamedQuery(name = "HexagonVertex.perVertex", query = """
        select hv from HexagonVertex hv
        join fetch hv.hexagon
        where hv.vertex.id = :idVertex
        """)
public class HexagonVertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Min(value = 0, message = "La posició mínima és 0")
    @Max(value = 5, message = "La posició màxima és 5")
    @Column(nullable = false)
    private int posicio;

    // PROPIETARI (FK hexagon_id). LAZY: referència de tornada (si fos EAGER arrossegaria tot l'hexàgon)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hexagon_id", nullable = false)
    private Hexagon hexagon;

    // PROPIETARI (FK vertex_id). EAGER: un vèrtex és un objecte petit i sense relacions EAGER
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;

    public HexagonVertex() {
    }

    public HexagonVertex(Hexagon hexagon, Vertex vertex, int posicio) {
        this.hexagon = hexagon;
        this.vertex = vertex;
        this.posicio = posicio;
    }

    public Integer getId() { return id; }
    public int getPosicio() { return posicio; }
    public Hexagon getHexagon() { return hexagon; }
    public Vertex getVertex() { return vertex; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HexagonVertex other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return HexagonVertex.class.hashCode();
    }
}
