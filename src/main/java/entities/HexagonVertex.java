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

    @OneToMany(mappedBy = "hexagonVertex", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;
}
