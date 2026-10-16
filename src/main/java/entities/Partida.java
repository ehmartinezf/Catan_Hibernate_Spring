package entities;
import java.util.ArrayList;
import java.util.List;
import enums.EstatPartida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private List<Jugador> jugadors = new ArrayList<>();

    public Partida() {
    }

    public Partida(int puntosParaGanar) {
        this.estat = EstatPartida.PREPARACIO;
        this.ronda = 1;
        this.puntsPerGuanyar = puntosParaGanar;
        this.dausTirats = false;
        this.lladrePendent = false;
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
}