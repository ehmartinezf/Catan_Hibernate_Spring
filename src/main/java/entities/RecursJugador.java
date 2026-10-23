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

@Entity
@Table(name = "recurs_jugador", uniqueConstraints = {
    @UniqueConstraint(name = "uk_id_jugador_id", columnNames = {"tipusRecurs", "jugador_id"})})
public class RecursJugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    @Enumerated(EnumType.STRING)
    @Column(name="tipusRecurs")
    private TipusRecurs tipusRecurs;

    @Column(name = "quantitat")
    private int quantitat;
}
