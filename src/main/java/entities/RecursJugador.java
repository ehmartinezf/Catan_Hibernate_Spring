package entities;

import enums.TipusRecurs;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * N:M amb atribut (quantitat) entre Jugador i TipusRecurs (un enum).
 * Id propi + UNIQUE(jugador, tipus): una sola fila per jugador i tipus. Cap clau composta.
 */
@Entity
@Table(name = "recurs_jugador", uniqueConstraints = @UniqueConstraint(name = "UK_recurs_jugador_tipus", columnNames = { "jugador_id", "tipus" }))
public class RecursJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipusRecurs tipus;

    @Min(value = 0, message = "La quantitat no pot ser negativa")
    @Column(nullable = false)
    private int quantitat;

    // PROPIETARI. LAZY: referència de tornada (EAGER crearia un cercle jugador <-> inventari)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    public RecursJugador() {
    }

    public RecursJugador(Jugador jugador, TipusRecurs tipus, int quantitat) {
        this.jugador = jugador;
        this.tipus = tipus;
        this.quantitat = quantitat;
    }

    public Integer getId() { return id; }
    public TipusRecurs getTipus() { return tipus; }
    public int getQuantitat() { return quantitat; }
    public void setQuantitat(int quantitat) { this.quantitat = quantitat; }
    public Jugador getJugador() { return jugador; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecursJugador other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return RecursJugador.class.hashCode();
    }
}
