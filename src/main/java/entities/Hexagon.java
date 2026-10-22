package entities;

import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * El codi (id del CSV) es repeteix a cada partida: cada partida crea el seu propi tauler.
 * Per això NO és unique per si sol, sinó la parella (partida_id, codi).
 */
@Entity
@Table(name = "hexagon", uniqueConstraints = @UniqueConstraint(name = "UK_hexagon_partida_codi", columnNames = { "partida_id", "codi" }))
@NamedQuery(name = "Hexagon.perNumero", query = "select h from Hexagon h where h.partida.id = :idPartida and h.numero = :numero")
public class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private int codi;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipusTerreny terreny;

    // Integer (wrapper) perquè el desert no té número: "encara no en té" és un estat vàlid
    @Min(2)
    @Max(12)
    @Column(nullable = true)
    private Integer numero;

    // coordenades axials del CSV (només informatives)
    @Column(nullable = false)
    private int q;

    @Column(nullable = false)
    private int r;

    // N:1 PROPIETARI (la FK partida_id és aquí). LAZY: referència de tornada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    // 1:N INVERS cap a HexagonVertex (sempre 6). LAZY: només es carrega quan una consulta el demana amb JOIN FETCH.
    @OneToMany(mappedBy = "hexagon", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("posicio")
    private List<HexagonVertex> hexagonVertexs = new ArrayList<>();

    public Hexagon() {
    }

    public Hexagon(int codi, int q, int r) {
        this.codi = codi;
        this.q = q;
        this.r = r;
    }

    public void afegirVertex(Vertex vertex, int posicio) {
        hexagonVertexs.add(new HexagonVertex(this, vertex, posicio));
    }

    // El desert no té número; la resta en tenen un de 2 a 12 (mai el 7). Validació ENTRE camps.
    @AssertTrue(message = "Número incoherent amb el tipus de terreny")
    public boolean isNumeroCoherent() {
        if (terreny == null) return true; // ja ho cobreix @NotNull
        if (terreny == TipusTerreny.DESERT) return numero == null;
        return numero != null && numero >= 2 && numero <= 12 && numero != 7;
    }

    public Integer getId() { return id; }
    public int getCodi() { return codi; }
    public TipusTerreny getTerreny() { return terreny; }
    public void setTerreny(TipusTerreny terreny) { this.terreny = terreny; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public int getQ() { return q; }
    public int getR() { return r; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public List<HexagonVertex> getHexagonVertexs() { return hexagonVertexs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hexagon other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Hexagon.class.hashCode();
    }

    @Override
    public String toString() {
        return "Hexagon[id=" + id + ", codi=" + codi + ", " + terreny + ", " + numero + "]";
    }
}
