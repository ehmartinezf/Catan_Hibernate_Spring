package entities;

import enums.TipusRecurs;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

@Entity
@Table(name = "recurs_jugador", uniqueConstraints = {
    @UniqueConstraint(name = "uk_id_jugador_id", columnNames = {"tipusRecurs", "jugador_id"})})
@NamedQueries({
    @NamedQuery(name = "RecursJugador.perJugador", query = "select r from recur_jugador r where r.jugador = :jugador order by r.tipusRecurs"),
    @NamedQuery(name = "RecursJugador.perJugadorITipus", query = "select r from recurs_jugador r where r.jugador = :jugador and r.tipusRecurs = :tipus")
})
public class RecursJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipusRecurs")
    private TipusRecurs tipusRecurs;

    @Column(name = "quantitat")
    private int quantitat;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public TipusRecurs getTipusRecurs() {
        return tipusRecurs;
    }

    public void setTipusRecurs(TipusRecurs tipusRecurs) {
        this.tipusRecurs = tipusRecurs;
    }

    public int getQuantitat() {
        return quantitat;
    }

    public void setQuantitat(int quantitat) {
        this.quantitat = quantitat;
    }

    @Override
    public int hashCode() {
        return RecursJugador.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RecursJugador other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }
}
