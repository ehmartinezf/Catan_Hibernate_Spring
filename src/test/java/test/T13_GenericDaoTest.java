package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entities.Partida;

import enums.TipusRecurs;
import enums.TipusTerreny;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * PRIORITAT 13: CRUD genèric (GenericDao), SessionManager i la traducció terreny -> recurs del JugadorDao.
 */
class T13_GenericDaoTest extends BaseDbTest {

    @Test
    @DisplayName("saveOrUpdate desa una entitat nova i list() la torna")
    void desar() {
        partidaDao.saveOrUpdate(new Partida(5));

        assertEquals(1, partidaDao.list().size());
        assertEquals(5, partidaDao.list().get(0).getPuntsPerGuanyar());
    }

    @Test
    @DisplayName("saveOrUpdate actualitza una entitat existent (sense duplicar-la)")
    void actualitzar() {
        int id = Joc.nova2(partidaDao);
        Partida p = partidaDao.get(id);
        p.setRonda(7);

        partidaDao.saveOrUpdate(p);

        assertEquals(1, partidaDao.list().size());
        assertEquals(7, partidaDao.get(id).getRonda());
    }

    @Test
    @DisplayName("get torna null si l'id no existeix")
    void getInexistent() {
        assertNull(partidaDao.get(99999));
    }

    @Test
    @DisplayName("delete(id) i delete(entitat) eliminen; un id inexistent no falla")
    void esborrar() {
        partidaDao.saveOrUpdate(new Partida(5));
        partidaDao.saveOrUpdate(new Partida(6));
        int primer = partidaDao.list().get(0).getId();

        partidaDao.delete(primer);
        assertEquals(1, partidaDao.list().size());

        partidaDao.delete(99999); // només imprimeix un missatge
        assertEquals(1, partidaDao.list().size());

        partidaDao.delete(partidaDao.list().get(0));
        assertTrue(partidaDao.list().isEmpty());
    }

    @Test
    @DisplayName("SessionManager és un singleton i tancar() permet crear-ne una de nova")
    void sessionManager() {
        SessionFactory primera = SessionManager.getSessionFactory();
        assertSame(primera, SessionManager.getSessionFactory());

        SessionManager.tancar();
        assertNotSame(primera, SessionManager.getSessionFactory());
    }

    @Test
    @DisplayName("JugadorDao.recursDelTerreny coincideix amb TipusTerreny.getRecurs()")
    void recursDelTerreny() {
        for (TipusTerreny t : TipusTerreny.values()) {
            TipusRecurs esperat = t.getRecurs();
            assertEquals(esperat, jugadorDao.recursDelTerreny(t), "Terreny " + t);
        }
        assertNull(jugadorDao.recursDelTerreny(null));
    }
}
