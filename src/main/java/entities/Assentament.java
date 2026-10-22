package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * Classe abstracta INTERMÈDIA: Poblat i Ciutat tenen en comú el vèrtex.
 * Sense @Id ni @Inheritance: ja venen de Construccio. Sí que porta @Entity (no s'hereta) i el seu @Table.
 */
@Entity
@Table(name = "assentament")
@NamedQuery(name = "Assentament.perVertex", query = "select a from Assentament a where a.vertex.id = :idVertex")
@NamedQuery(name = "Assentament.perPartida", query = """
        select a from Assentament a join fetch a.propietari
        where a.vertex.partida.id = :idPartida
        """)
@NamedQuery(name = "Assentament.perHexagon", query = """
        select a from Assentament a join fetch a.propietari
        where a.vertex.id in (select hv.vertex.id from HexagonVertex hv where hv.hexagon.id = :idHexagon)
        """)
// Regla de la distància: assentaments als vèrtexs veïns (adjacents per una aresta) del vèrtex indicat
@NamedQuery(name = "Assentament.veinsOcupats", query = """
        select count(a) from Assentament a
        where a.vertex.id in (select ar.vertexB.id from Aresta ar where ar.vertexA.id = :idVertex)
           or a.vertex.id in (select ar.vertexA.id from Aresta ar where ar.vertexB.id = :idVertex)
        """)
public abstract class Assentament extends Construccio {

    /*
     * 1:1 UNIDIRECCIONAL (el diagrama només té fletxa d'Assentament cap a Vertex): la FK vertex_id és aquí.
     * unique = true: "un vèrtex només pot tenir un assentament". nullable = false: sempre està en un vèrtex.
     * EAGER: s'usa sempre (producció, tauler) i Vertex és petit i no arrossega res.
     */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vertex_id", nullable = false, unique = true)
    private Vertex vertex;

    protected Assentament() {
    }

    protected Assentament(Jugador propietari, Vertex vertex) {
        super(propietari);
        this.vertex = vertex;
    }

    // Polimorfisme: cada subclasse diu quants punts val (un MÈTODE, no un atribut: Hibernate l'ignora)
    public abstract int getPunts();

    // Recursos que produeix quan surt el número del seu hexàgon
    public abstract int getProduccio();

    public Vertex getVertex() { return vertex; }
}
