package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

/*
 * ARREL de la jerarquia (estratègia JOINED, ho exigeix l'enunciat): una taula per cada classe,
 * unides per l'id. L'@Id i l'@Inheritance NOMÉS van aquí, a l'arrel.
 * Abstracta: no existeix cap "construcció genèrica", sempre és un poblat, una ciutat o una carretera.
 * El discriminador és opcional amb JOINED: Hibernate dedueix el tipus mirant a quina taula hi ha la fila.
 */
@Entity
@Table(name = "construccio")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQuery(name = "Construccio.comptarPerTipus", query = "select count(c) from Construccio c where c.propietari.id = :idJugador and type(c) = :tipus")
public abstract class Construccio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Allò COMÚ a totes les construccions va a l'arrel: el propietari.
    // N:1 PROPIETARI. LAZY: referència de tornada.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "propietari_id", nullable = false)
    private Jugador propietari;

    // Constructor sense paràmetres: obligatori per a Hibernate. protected: només el fan servir les subclasses i Hibernate.
    protected Construccio() {
    }

    protected Construccio(Jugador propietari) {
        this.propietari = propietari;
    }

    public Integer getId() { return id; }
    public Jugador getPropietari() { return propietari; }

    // equals/hashCode es defineixen UNA vegada, a l'arrel (herència: sessió 2, apartat 14)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Construccio other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Construccio.class.hashCode();
    }
}
