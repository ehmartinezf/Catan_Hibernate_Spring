package dao;

import java.util.List;

import entities.Jugador;
import entities.Vertex;
import enums.TipusRecurs;

public interface IJugadorDao extends IGenericDao<Jugador, Integer> {

    String mostrarRecursos(int idJugador);

    String construirCarretera(int idJugador, int idAresta);

    String construirPoblat(int idJugador, int idVertex);

    String millorarACiutat(int idJugador, int idVertex);

    String comerciarBanca(int idJugador, TipusRecurs dona, TipusRecurs rep);

    // -1 si el jugador no existeix
    int calcularPunts(int idJugador);

    String passarTorn(int idJugador);

    // idJugadorVictima pot ser null (si no hi ha ningú a qui robar)
    String moureLladre(int idJugador, int idHexagon, Integer idJugadorVictima);

    String comprarCarta(int idJugador);

    // El paràmetre recurs només s'usa amb MONOPOLI
    String jugarCarta(int idJugador, int idCarta, TipusRecurs recurs);

    List<Vertex> vertexsDisponibles(int idJugador);
}
