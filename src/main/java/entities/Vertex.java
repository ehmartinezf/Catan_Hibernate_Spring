package entities;

import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vertex")
public class Vertex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "codi", nullable = false)
    private int codi;

    @OneToOne(mappedBy = "vertex", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Assentament assentament;

    @OneToMany(mappedBy = "hexagonVertex", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<HexagonVertex> hexagonVertex;

    @OneToMany(mappedBy = "vertex", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Partida partida;
}
