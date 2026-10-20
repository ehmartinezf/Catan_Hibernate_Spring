package entities;
import java.util.ArrayList;
import java.util.List;
import enums.ColorJugador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "jugador")
@Inheritance(strategy = InheritanceType.JOINED)

public class Jugador {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String nom;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ColorJugador color;

	@Column(nullable = false)
	private int ordreTorn;

	@ManyToOne
	@JoinColumn(name = "partida_id", nullable = false)
	private Partida partida;

	@OneToMany(mappedBy = "jugador")
	private List<RecursJugador> recursos = new ArrayList<>();

	public Jugador() {
	}

	public Jugador(String nom, ColorJugador color, int ordreTorn, Partida partida) {
		this.nom = nom;
		this.color = color;
		this.ordreTorn = ordreTorn;
		this.partida = partida;
	}

	public Integer getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public ColorJugador getColor() {
		return color;
	}

	public void setColor(ColorJugador color) {
		this.color = color;
	}

	public int getOrdreTorn() {
		return ordreTorn;
	}

	public void setOrdreTorn(int ordreTorn) {
		this.ordreTorn = ordreTorn;
	}

	public Partida getPartida() {
		return partida;
	}

	public void setPartida(Partida partida) {
		this.partida = partida;
	}

	public List<RecursJugador> getRecursos() {
		return recursos;
	}
}
