package dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

/*
 * Classe base dels tests d'integració (els que fan servir els DAO).
 *
 * Abans de CADA test tanquem la SessionFactory: la següent que es crea fa hbm2ddl "create"
 * i, per tant, la base de dades H2 (en memòria) torna a estar buida i els ids tornen a començar a 1.
 * Així cap test depèn de l'estat que ha deixat un altre.
 */
abstract class BaseDbTest {

    protected PartidaDao partidaDao;
    protected JugadorDao jugadorDao;

    @BeforeEach
    void reiniciarBaseDeDades() {
        SessionManager.tancar();
        // Els DAO guarden la SessionFactory al constructor: s'han de crear DESPRÉS de tancar
        partidaDao = new PartidaDao();
        jugadorDao = new JugadorDao();
    }

    @AfterAll
    static void tancarSessionFactory() {
        SessionManager.tancar();
    }
}
