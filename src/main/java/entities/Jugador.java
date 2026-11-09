package entities;

import enums.ColorJugador;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "jugador")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    private ColorJugador color;

    @Column(name = "ordre_torn", nullable = false)
    private int ordreTorn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @OneToMany(
            mappedBy = "jugador",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<RecursJugador> recursos = new HashSet<>();

    @OneToMany(mappedBy = "jugador")
    private Set<CartaDesenvolupament> cartesDesenvolupament = new HashSet<>();

    @OneToMany(mappedBy = "propietari")
    private Set<Construccio> construccions = new HashSet<>();

    @Column(name = "cavallers_jugats")
    private int cavallersJugats;

    public Jugador() {}

    public Jugador(String nom, ColorJugador color, int ordreTorn, Partida partida) {
        this.nom = nom;
        this.color = color;
        this.ordreTorn = ordreTorn;
        this.partida = partida;

        for (enums.TipusRecurs tipus : enums.TipusRecurs.values()) {
            RecursJugador recurs = new RecursJugador();
            recurs.setJugador(this);
            recurs.setTipusRecurs(tipus);
            recurs.setQuantitat(0);
            this.recursos.add(recurs);
        }
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

    public int getCavallersJugats() {
        return cavallersJugats;
    }

    public void setCavallersJugats(int cavallersJugats) {
        this.cavallersJugats = cavallersJugats;
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

    public Set<RecursJugador> getRecursos() {
        return recursos;
    }

    public void afegirConstruccio(Construccio construccio) {
        this.construccions.add(construccio);
        construccio.setPropietari(this);
    }

    public void eliminarConstruccio(Construccio construccio) {
        this.construccions.remove(construccio);
    }

    @Override
    public int hashCode() {
        return Jugador.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Jugador other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

    public Set<CartaDesenvolupament> getCartesDesenvolupament() {
        return cartesDesenvolupament;
    }

    public void setCartesDesenvolupament(Set<CartaDesenvolupament> cartesDesenvolupament) {
        this.cartesDesenvolupament = cartesDesenvolupament;
    }

    public Set<Construccio> getConstruccions() {
        return construccions;
    }

    public void setConstruccions(Set<Construccio> construccions) {
        this.construccions = construccions;
    }

    @Override
    public String toString() {
        return "Jugador{id="
                + id
                + ", nom="
                + nom
                + ", color="
                + color
                + ", ordreTorn="
                + ordreTorn
                + "}";
    }
}
