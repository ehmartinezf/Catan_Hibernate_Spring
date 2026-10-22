package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "partida")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstatPartida estat;

    @NotNull
    @Column(name = "data_inici", nullable = false)
    private LocalDateTime dataInici;

    @Min(1)
    @Column(nullable = false)
    private int ronda;

    // 0 = encara no s'han tirat els daus
    @Min(0)
    @Max(12)
    @Column(name = "ultima_tirada", nullable = false)
    private int ultimaTirada;

    @Min(3)
    @Column(name = "punts_per_guanyar", nullable = false)
    private int puntsPerGuanyar;

    @Column(name = "daus_tirats", nullable = false)
    private boolean dausTirats;

    @Column(name = "lladre_pendent", nullable = false)
    private boolean lladrePendent;

    /*
     * Relacions "0..1" de la partida: encara no tenen valor quan es crea -> NULLABLE.
     * LAZY: són referències a un únic objecte que no sempre cal.
     * actiu i guanyador apunten a la MATEIXA entitat (Jugador): cal un @JoinColumn amb nom diferent a cadascuna.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lladre_id")
    private Hexagon lladre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actiu_id")
    private Jugador actiu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guanyador_id")
    private Jugador guanyador;

    /*
     * COSTAT INVERS de les relacions N:1 que tenen Hexagon, Vertex, Aresta, Jugador i CartaDesenvolupament.
     * LAZY: col·leccions grans (19, 54, 72...) que només cal portar amb JOIN FETCH quan una consulta les necessita.
     * cascade ALL + orphanRemoval: composició (si s'esborra la partida s'esborra tot el seu tauler).
     * Set (no List): permet fer JOIN FETCH de més d'una col·lecció sense MultipleBagFetchException.
     */
    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Hexagon> hexagons = new HashSet<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Vertex> vertexs = new HashSet<>();

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Aresta> arestes = new HashSet<>();

    // Només 2-4 jugadors i sempre ordenats per torn. És l'única List (no hi ha cap altre "bag" a la partida).
    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("ordreTorn")
    private List<Jugador> jugadors = new ArrayList<>();

    // La pila de cartes de la partida (les que encara no té cap jugador)
    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CartaDesenvolupament> cartes = new HashSet<>();

    public Partida() {
    }

    public Partida(int puntsPerGuanyar) {
        this.estat = EstatPartida.PREPARACIO;
        this.dataInici = LocalDateTime.now();
        this.ronda = 1;
        this.ultimaTirada = 0;
        this.puntsPerGuanyar = puntsPerGuanyar;
    }

    // ---------- mètodes auxiliars (sincronitzen els dos costats) ----------

    public void afegirHexagon(Hexagon h) {
        hexagons.add(h);
        h.setPartida(this);
    }

    public void afegirVertex(Vertex v) {
        vertexs.add(v);
        v.setPartida(this);
    }

    public void afegirAresta(Aresta a) {
        arestes.add(a);
        a.setPartida(this);
    }

    public void afegirJugador(Jugador j) {
        jugadors.add(j);
        j.setPartida(this);
    }

    public void afegirCarta(CartaDesenvolupament c) {
        cartes.add(c);
        c.setPartida(this);
    }

    // ---------- getters / setters ----------

    public Integer getId() { return id; }
    public EstatPartida getEstat() { return estat; }
    public void setEstat(EstatPartida estat) { this.estat = estat; }
    public LocalDateTime getDataInici() { return dataInici; }
    public int getRonda() { return ronda; }
    public void setRonda(int ronda) { this.ronda = ronda; }
    public int getUltimaTirada() { return ultimaTirada; }
    public void setUltimaTirada(int ultimaTirada) { this.ultimaTirada = ultimaTirada; }
    public int getPuntsPerGuanyar() { return puntsPerGuanyar; }
    public boolean isDausTirats() { return dausTirats; }
    public void setDausTirats(boolean dausTirats) { this.dausTirats = dausTirats; }
    public boolean isLladrePendent() { return lladrePendent; }
    public void setLladrePendent(boolean lladrePendent) { this.lladrePendent = lladrePendent; }
    public Hexagon getLladre() { return lladre; }
    public void setLladre(Hexagon lladre) { this.lladre = lladre; }
    public Jugador getActiu() { return actiu; }
    public void setActiu(Jugador actiu) { this.actiu = actiu; }
    public Jugador getGuanyador() { return guanyador; }
    public void setGuanyador(Jugador guanyador) { this.guanyador = guanyador; }
    public Set<Hexagon> getHexagons() { return hexagons; }
    public Set<Vertex> getVertexs() { return vertexs; }
    public Set<Aresta> getArestes() { return arestes; }
    public List<Jugador> getJugadors() { return jugadors; }
    public Set<CartaDesenvolupament> getCartes() { return cartes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partida other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Partida.class.hashCode();
    }

    // Sense relacions al toString: evita recursió infinita i LazyInitializationException
    @Override
    public String toString() {
        return "Partida[id=" + id + ", estat=" + estat + ", ronda=" + ronda + "]";
    }
}
