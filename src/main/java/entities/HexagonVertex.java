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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "hexagon_vertex")
public class HexagonVertex extends Hexagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Min(value = 0, message = "La posicio minima es 0")
    @Max(value = 5, message = "La posicio maxima es 5")
    @Column(name = "posicio")
    private int posicio;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "vertex_id", nullable = false)
    private Vertex vertex;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hexagon_id", nullable = false)
    private Hexagon hexagon;

    public HexagonVertex() {
    }

    public HexagonVertex(Vertex vertex, int posicio) {
        this.posicio = posicio;
        this.vertex = vertex;
    }
    
    

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosicio() {
        return posicio;
    }

    public void setPosicio(int posicio) {
        this.posicio = posicio;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public Hexagon getHexagon() {
        return hexagon;
    }

    public void setHexagon(Hexagon hexagon) {
        this.hexagon = hexagon;
    }

}
