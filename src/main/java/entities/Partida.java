package entities;
import java.util.ArrayList;
import java.util.List;
import enums.EstatPartida;
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

@Entity
@Table(name = "partida")

public class Partida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estat", nullable = false, length = 20)
    private EstatPartida estat;

    @Column(name = "ronda", nullable = false)
    private int ronda;

    @Column(name = "dau1")
    private Integer dau1;

    @Column(name = "dau2")
    private Integer dau2;

    @Column(name = "punts_per_guanyar", nullable = false)
    private int puntsPerGuanyar;

    @Column(name = "daus_tirats", nullable = false)
    private boolean dausTirats;

    @Column(name = "lladre_pendent", nullable = false)
    private boolean lladrePendent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jugador_actiu_id")
    private Jugador jugadorActiu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guanyador_id")
    private Jugador guanyador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hexagon_lladre_id")
    private Hexagon hexagonLladre;

    @OneToMany(mappedBy = "partida")
    private List<Jugador> jugadors = new ArrayList<>();

    @OneToMany(mappedBy = "partida")
    private List<Hexagon> hexagons = new ArrayList<>();

    public Partida() {
    }

    public Partida(int puntsPerGuanyar) {
        this.estat = EstatPartida.PREPARACIO;
        this.ronda = 1;
        this.puntsPerGuanyar = puntsPerGuanyar;
        this.dausTirats = false;
        this.lladrePendent = false;
    }

    public int getId() {
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

    public Integer getDau1() {
        return dau1;
    }

    public void setDau1(Integer dau1) {
        this.dau1 = dau1;
    }

    public Integer getDau2() {
        return dau2;
    }

    public void setDau2(Integer dau2) {
        this.dau2 = dau2;
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

    public List<Jugador> getJugadors() {
        return jugadors;
    }

    public void setJugadors(List<Jugador> jugadors) {
        this.jugadors = jugadors;
    }

    public List<Hexagon> getHexagons() {
        return hexagons;
    }

    public void setHexagons(List<Hexagon> hexagons) {
        this.hexagons = hexagons;
    }
}