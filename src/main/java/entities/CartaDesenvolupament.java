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
import jakarta.persistence.Table;

@Entity
@Table(name = "carta_desenvolupament")
public class CartaDesenvolupament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private TipusCarta tipus;

    @Column(name = "usada")
    private boolean usada;

    @Column(name = "ronda_compra", nullable = false)
    private Integer rondaCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    public CartaDesenvolupament() {
    }

    public CartaDesenvolupament(TipusCarta tipus) {
        this.tipus = tipus;
    }
    
    

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public TipusCarta getTipus() {
		return tipus;
	}

	public void setTipus(TipusCarta tipus) {
		this.tipus = tipus;
	}

	public boolean isUsada() {
		return usada;
	}

	public void setUsada(boolean usada) {
		this.usada = usada;
	}

	public Integer getRondaCompra() {
		return rondaCompra;
	}

	public void setRondaCompra(Integer rondaCompra) {
		this.rondaCompra = rondaCompra;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public Jugador getJugador() {
		return jugador;
	}

	public void setJugador(Jugador jugador) {
		this.jugador = jugador;
	}
    
    
}
