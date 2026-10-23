package entities;

import org.hibernate.annotations.Check;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "aresta")
@Check(constraints="codi >= 1 AND codi <= 72")
public class Aresta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "codi", nullable = false)
    private int codi;

    @OneToOne(mappedBy = "aresta", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "carretera_id", unique = true, nullable = false)
    private Carretera carretera;

    @OneToOne(mappedBy = "vertex", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vertex_a", nullable = false, unique = true)
    private Vertex vertexA;

    @OneToOne(mappedBy = "vertex", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vertex_B", nullable = false, unique = true)
    private Vertex vertexB;

    @ManyToOne
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

}
