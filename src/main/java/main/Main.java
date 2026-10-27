package main;

import java.util.List;

import dao.PartidaDao;
import dao.SessionManager;

public class Main {

    public static void main(String[] args) {
        try {
            PartidaDao partidaDao = new PartidaDao();
            System.out.println("Con 1 jugador: " + partidaDao.iniciarPartida(List.of("Anna")));
            int id = partidaDao.iniciarPartida(List.of("Anna", "Bernat", "Carla"));
            System.out.println("Partida creada con id " + id);
        } finally {
            SessionManager.tancar();
        }
    }
}
