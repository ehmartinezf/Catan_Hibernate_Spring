package entities;

import java.util.ArrayList;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "partida")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatPartida estat;

    @Column(nullable = false)
    private int ronda;

    @Column(nullable = false)
    private int puntsPerGuanyar;

    @Column(nullable = false)
    private boolean dausTirats;

    @Column(nullable = false)
    private boolean lladrePendent;

    @OneToMany(mappedBy = "partida")
    private List<Jugador> jugadors;

    @OneToMany(mappedBy = "partida", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Vertex> vertex;

    @OneToMany(mappedBy = "partida", fetch = FetchType.LAZY)
    private List<Hexagon> hexagons;

    public Partida() {
    }

    public Partida(Long id, EstatPartida estat, int ronda, int puntsPerGuanyar, boolean dausTirats, boolean lladrePendent, List<Jugador> jugadors, Set<Vertex> vertex, List<Hexagon> hexagons) {
        this.id = id;
        this.estat = estat;
        this.ronda = ronda;
        this.puntsPerGuanyar = puntsPerGuanyar;
        this.dausTirats = dausTirats;
        this.lladrePendent = lladrePendent;
        this.jugadors = jugadors;
        this.vertex = vertex;
        this.hexagons = hexagons;
    }

    public Long getId() {
        return id;
    }

    public EstatPartida getEstado() {
        return estat;
    }

    public void setEstado(EstatPartida estado) {
        this.estat = estado;
    }

    public int getRonda() {
        return ronda;
    }

    public void setRonda(int ronda) {
        this.ronda = ronda;
    }

    public int getPuntosParaGanar() {
        return puntsPerGuanyar;
    }

    public void setPuntosParaGanar(int puntosParaGanar) {
        this.puntsPerGuanyar = puntosParaGanar;
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

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public EstatPartida getEstat() {
        return estat;
    }

    public void setEstat(EstatPartida estat) {
        this.estat = estat;
    }

    public int getPuntsPerGuanyar() {
        return puntsPerGuanyar;
    }

    public void setPuntsPerGuanyar(int puntsPerGuanyar) {
        this.puntsPerGuanyar = puntsPerGuanyar;
    }

    public Set<Vertex> getVertex() {
        return vertex;
    }

    public void setVertex(Set<Vertex> vertex) {
        this.vertex = vertex;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

}
