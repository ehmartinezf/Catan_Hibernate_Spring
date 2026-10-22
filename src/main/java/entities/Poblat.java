package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "poblat")
@NamedQuery(name = "Poblat.delJugadorAlVertex", query = "select p from Poblat p where p.propietari.id = :idJugador and p.vertex.id = :idVertex")
public class Poblat extends Assentament {

    public Poblat() {
    }

    public Poblat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

    @Override
    public int getPunts() {
        return 1;
    }

    @Override
    public int getProduccio() {
        return 1;
    }
}
