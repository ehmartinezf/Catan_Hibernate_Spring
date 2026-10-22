package entities;

import enums.TipusCarta;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "carta_desenvolupament")
@NamedQuery(name = "Carta.pila", query = "select c from CartaDesenvolupament c where c.partida.id = :idPartida and c.jugador is null")
@NamedQuery(name = "Carta.comptarMa", query = "select count(c) from CartaDesenvolupament c where c.jugador.id = :idJugador and c.tipus = :tipus")
public class CartaDesenvolupament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipusCarta tipus;

    @Column(nullable = false)
    private boolean usada;

    // Ronda en què es va comprar. Integer: mentre és a la pila encara no s'ha comprat (null)
    @Column(name = "ronda_compra")
    private Integer rondaCompra;

    // La carta pertany SEMPRE a una partida (a la pila o a la mà d'un jugador)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    // Mentre és a la pila no té propietari -> NULLABLE. En comprar-la s'omple.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    public CartaDesenvolupament() {
    }

    public CartaDesenvolupament(TipusCarta tipus) {
        this.tipus = tipus;
    }

    public Integer getId() { return id; }
    public TipusCarta getTipus() { return tipus; }
    public boolean isUsada() { return usada; }
    public void setUsada(boolean usada) { this.usada = usada; }
    public Integer getRondaCompra() { return rondaCompra; }
    public void setRondaCompra(Integer rondaCompra) { this.rondaCompra = rondaCompra; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CartaDesenvolupament other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return CartaDesenvolupament.class.hashCode();
    }
}
