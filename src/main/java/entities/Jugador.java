package entities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import enums.ColorJugador;
import enums.TipusRecurs;
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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jugador", uniqueConstraints = {
        @UniqueConstraint(name = "UK_jugador_partida_ordre", columnNames = { "partida_id", "ordre_torn" }),
        @UniqueConstraint(name = "UK_jugador_partida_color", columnNames = { "partida_id", "color" }) })
@NamedQuery(name = "Jugador.perPartida", query = "select j from Jugador j where j.partida.id = :idPartida order by j.ordreTorn")
@NamedQuery(name = "Jugador.ambAssentamentAlHexagon", query = """
        select distinct a.propietari from Assentament a
        where a.vertex.id in (select hv.vertex.id from HexagonVertex hv where hv.hexagon.id = :idHexagon)
        """)
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nom del jugador no pot estar buit")
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String nom;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ColorJugador color;

    @Min(1)
    @Max(4)
    @Column(name = "ordre_torn", nullable = false)
    private int ordreTorn;

    @Min(0)
    @Column(name = "cavallers_jugats", nullable = false)
    private int cavallersJugats;

    // N:1 PROPIETARI. LAZY: referència de tornada. Un jugador pertany a UNA sola partida.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    /*
     * Inventari: SEMPRE 5 files (una per tipus de recurs).
     * EAGER: es fa servir a quasi totes les accions (construir, comerciar...), és petit i fix,
     * i RecursJugador no arrossega res (la seva referència de tornada és LAZY). És un Set.
     * cascade ALL + orphanRemoval: sense jugador no hi ha inventari.
     */
    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<RecursJugador> inventari = new HashSet<>();

    // Construccions del jugador (propietari). LAZY: poden ser fins a 24 i només cal en alguns casos.
    @OneToMany(mappedBy = "propietari", fetch = FetchType.LAZY)
    private Set<Construccio> construccions = new HashSet<>();

    // Cartes a la mà. LAZY: ús ocasional.
    @OneToMany(mappedBy = "jugador", fetch = FetchType.LAZY)
    private Set<CartaDesenvolupament> ma = new HashSet<>();

    public Jugador() {
    }

    // En crear el jugador es creen les seves 5 files de recursos amb quantitat 0
    public Jugador(String nom, ColorJugador color, int ordreTorn, Partida partida) {
        this.nom = nom;
        this.color = color;
        this.ordreTorn = ordreTorn;
        this.partida = partida;
        for (TipusRecurs t : TipusRecurs.values()) {
            inventari.add(new RecursJugador(this, t, 0));
        }
    }

    // ---------- lògica d'inventari (mètodes: Hibernate els ignora amb accés per camp) ----------

    public int getQuantitat(TipusRecurs tipus) {
        for (RecursJugador r : inventari) {
            if (r.getTipus() == tipus) return r.getQuantitat();
        }
        return 0;
    }

    public void afegirRecurs(TipusRecurs tipus, int quantitat) {
        for (RecursJugador r : inventari) {
            if (r.getTipus() == tipus) {
                r.setQuantitat(r.getQuantitat() + quantitat);
                return;
            }
        }
    }

    // Torna els que s'han pogut treure realment
    public int treureRecurs(TipusRecurs tipus, int quantitat) {
        int treure = Math.min(quantitat, getQuantitat(tipus));
        afegirRecurs(tipus, -treure);
        return treure;
    }

    public int getTotalRecursos() {
        int total = 0;
        for (RecursJugador r : inventari) {
            total += r.getQuantitat();
        }
        return total;
    }

    public boolean potPagar(Map<TipusRecurs, Integer> cost) {
        for (Map.Entry<TipusRecurs, Integer> e : cost.entrySet()) {
            if (getQuantitat(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    public void pagar(Map<TipusRecurs, Integer> cost) {
        for (Map.Entry<TipusRecurs, Integer> e : cost.entrySet()) {
            afegirRecurs(e.getKey(), -e.getValue());
        }
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public ColorJugador getColor() { return color; }
    public int getOrdreTorn() { return ordreTorn; }
    public int getCavallersJugats() { return cavallersJugats; }
    public void setCavallersJugats(int cavallersJugats) { this.cavallersJugats = cavallersJugats; }
    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }
    public Set<RecursJugador> getInventari() { return inventari; }
    public Set<Construccio> getConstruccions() { return construccions; }
    public Set<CartaDesenvolupament> getMa() { return ma; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jugador other)) return false;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Jugador.class.hashCode();
    }

    @Override
    public String toString() {
        return "Jugador[id=" + id + ", nom=" + nom + ", " + color + ", torn=" + ordreTorn + "]";
    }
}
