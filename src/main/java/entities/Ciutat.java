package entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 * Una ciutat SUBSTITUEIX un poblat. Un objecte no pot canviar de subclasse:
 * cal esborrar el Poblat i crear una Ciutat nova (vegeu JugadorDao.millorarACiutat).
 */
@Entity
@Table(name = "ciutat")
public class Ciutat extends Assentament {

    public Ciutat() {
    }

    public Ciutat(Jugador propietari, Vertex vertex) {
        super(propietari, vertex);
    }

    @Override
    public int getPunts() {
        return 2;
    }

    @Override
    public int getProduccio() {
        return 2;
    }
}
