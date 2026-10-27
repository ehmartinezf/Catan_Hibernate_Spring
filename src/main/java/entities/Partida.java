package entities;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Check;

import enums.EstatPartida;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "partida")
@Check(constraints = "ultimaTirada <= 2 AND ultimaTirada <= 12")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estat", nullable = false, length = 20)
    private EstatPartida estat;

    @Column(name = "ronda", nullable = false)
    private int ronda;

    @Column(name = "punts_per_guanyar", nullable = false)
    private int puntsPerGuanyar;

    @Column(name = "daus_tirats", nullable = false)
    private boolean dausTirats;

    @Column(name = "lladre_pendent", nullable = false)
    private boolean lladrePendent;

    @Column(name = "ultima_tirada")
    private int ultimaTirada;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    @JoinColumn(name = "jugador_actiu_id")
    private Jugador jugadorActiu;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "guanyador_id")
    private Jugador guanyador;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hexagon_lladre_id")
    private Hexagon hexagonLladre;

    @OneToMany(mappedBy = "partida")
    private Set<Jugador> jugadors = new HashSet<>();

    @OneToMany(mappedBy = "partida")
    private Set<Hexagon> hexagons = new HashSet<>();

    @OneToMany(mappedBy = "partida")
    private Set<Vertex> vertexes;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Aresta> arestes;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CartaDesenvolupament> pila;

    public Partida() {
        this.estat = EstatPartida.PREPARACIO;
        this.ronda = 1;
        this.puntsPerGuanyar = 10;
        this.dausTirats = false;
        this.lladrePendent = false;
    }

    public Partida(int puntsPerGuanyar) {
        this.estat = EstatPartida.PREPARACIO;
        this.ronda = 1;
        this.puntsPerGuanyar = puntsPerGuanyar;
        this.dausTirats = false;
        this.lladrePendent = false;
    }

    public void afegirCarta(CartaDesenvolupament carta) {
        this.pila.add(carta);
        carta.setPartida(this);
    }

    public void eliminarCarta(CartaDesenvolupament carta) {
        this.pila.remove(carta);
        carta.setPartida(null);
    }

    public Integer getId() {
        return id;
    }

    public EstatPartida getEstat() {
        return estat;
    }

    public void setEstat(EstatPartida estat) {
        this.estat = estat;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
    }

    public int getPuntsPerGuanyar() {
        return puntsPerGuanyar;
    }

    public void setPuntsPerGuanyar(int puntsPerGuanyar) {
        this.puntsPerGuanyar = puntsPerGuanyar;
    }

    public boolean isDausTirats() {
        return dausTirats;
    }

    public void setDausTirats(boolean dausTirats) {
        this.dausTirats = dausTirats;
    }

    public boolean isLladrePendent() {
        return lladrePendent;
    }

    public void setLladrePendent(boolean lladrePendent) {
        this.lladrePendent = lladrePendent;
    }

    public Jugador getJugadorActiu() {
        return jugadorActiu;
    }

    public void setJugadorActiu(Jugador jugadorActiu) {
        this.jugadorActiu = jugadorActiu;
    }

    public Jugador getGuanyador() {
        return guanyador;
    }

    public void setGuanyador(Jugador guanyador) {
        this.guanyador = guanyador;
    }

    public Hexagon getHexagonLladre() {
        return hexagonLladre;
    }

    public void setHexagonLladre(Hexagon hexagonLladre) {
        this.hexagonLladre = hexagonLladre;
    }

    public Set<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(Set<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public Set<Hexagon> getHexagons() {
        return hexagons;
    }

    public void setHexagons(Set<Hexagon> hexagons) {
        this.hexagons = hexagons;
    }

    public void afegirJugador(Jugador jugador) {
        this.jugadors.add(jugador);
        jugador.setPartida(this);
    }

    public void afegirHexagon(Hexagon hexagon) {
        this.hexagons.add(hexagon);
        hexagon.setPartida(this);
    }

    public void afegirVertex(Vertex vertex) {
        this.vertexes.add(vertex);
        vertex.setPartida(this);
    }

    public void afegirAresta(Aresta aresta) {
        this.arestes.add(aresta);
        aresta.setPartida(this);
    }

    public void afegirCartaDesenvolupament(CartaDesenvolupament carta) {
        this.pila.add(carta);
        carta.setPartida(this);
    }

    public void eliminarJugador(Jugador jugador) {
        this.jugadors.remove(jugador);
    }

    public void eliminarHexagon(Hexagon hexagon) {
        this.hexagons.remove(hexagon);
    }

    public void eliminarVertex(Vertex vertex) {
        this.vertexes.remove(vertex);
    }

    public void eliminarAresta(Aresta aresta) {
        this.arestes.remove(aresta);
    }

    public void eliminarCartaDesenvolupament(CartaDesenvolupament cartaDesenvolupament) {
        this.pila.remove(cartaDesenvolupament);
    }

    @Override
    public int hashCode() {
        return Partida.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Partida other)) {
            return false;
        }
        return getId() != null && getId().equals(other.getId());
    }

}
