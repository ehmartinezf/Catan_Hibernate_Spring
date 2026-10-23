package entities;

import enums.TipusTerreny;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "hexagon")
@Inheritance(strategy = InheritanceType.JOINED)
public class Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codi", nullable = false, unique = true, length = 10)
    private int codi;

    @Enumerated(EnumType.STRING)
    @Column(name = "terreny", nullable = false, length = 20)
    private TipusTerreny terreny;

    @Column(name = "numero", nullable = true)
    private Integer numero;

    @Column(name = "q", nullable = false)
    private int q;

    @Column(name = "r", nullable = false)
    private int r;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "partida_id", nullable = false, unique = true)
    private Partida partida;

    @OneToMany(mappedBy = "hexagon")
    private Set<HexagonVertex> hexagonVertexes;

    //N:M a Vertex a traves de HexagonVertex -> Ref: Videojoc, Llençament, Plataforma
}
