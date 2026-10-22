<<<<<<< HEAD
package entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@Entity
@Table(name = "hexagon_vertex")
public class HexagonVertex extends Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Min(value = 0, message = "La posicio minima es 0")
    @Max(value = 5, message = "La posicio maxima es 5")
    @Column(name = "posicio")
    private int posicio;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Set<Vertex> vertexes;
}
=======
package entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;

@Entity
@Table(name = "hexagon_vertex")
public class HexagonVertex extends Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Min(value = 0, message = "La posicio minima es 0")
    @Max(value = 5, message = "La posicio maxima es 5")
    @Column(name = "posicio")
    private int posicio;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Set<Vertex> vertexes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hexagon_id", nullable = false)
    private Hexagon hexagon;
}
>>>>>>> 493fdff2fd126b09a5a7f667a151fff7244b4c8e
