package dao;

import java.util.List;

import entities.Jugador;
import entities.Partida;

/*
 * Contracte de les operacions de la partida i del tauler.
 * Hereta el CRUD genèric (saveOrUpdate, get, delete, list) i hi afegeix les operacions del domini.
 */
public interface IPartidaDao extends IGenericDao<Partida, Integer> {

    // Torna l'id de la partida creada, o -1 si no s'ha pogut crear
    int iniciarPartida(List<String> nomsJugadors);

    // Igual, indicant els punts per guanyar (10 per defecte a la versió d'un sol paràmetre)
    int iniciarPartida(List<String> nomsJugadors, int puntsPerGuanyar);

    String mostrarTauler(int idPartida);

    String mostrarPartida(int idPartida);

    String tirarDaus(int idPartida);

    String comprovarVictoria(int idPartida);

    // Jugador amb l'exèrcit més gran (null si no n'hi ha cap)
    Jugador exercitMesGran(int idPartida);
}
