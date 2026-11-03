package main;

import dao.JugadorDao;
import enums.TipusRecurs;

public class Mian2paranodetrozarluegosepasa {

	public static void main(String[] args) {
		 JugadorDao dao = new JugadorDao();

	        int actiu = 1;
	        int altre = 2;
	        int hexagonAmbVictima = 1;
	        int hexagonBuit = 2;
	        int carta = 1;

	        System.out.println("moureLladre ");
	        System.out.println(dao.moureLladre(actiu, hexagonAmbVictima, null));      // ha de demanar victima o moure
	        System.out.println(dao.moureLladre(altre, hexagonAmbVictima, null));      // no es el seu torn
	        System.out.println(dao.moureLladre(actiu, hexagonAmbVictima, altre));     // roba 1 recurs
	        System.out.println(dao.moureLladre(actiu, hexagonBuit, null));            // lladre no pendent

	        System.out.println("comprarCarta");
	        System.out.println(dao.comprarCarta(actiu));
	        System.out.println(dao.comprarCarta(altre));                              // no es el seu torn

	        System.out.println("jugarCarta");
	        System.out.println(dao.jugarCarta(actiu, carta, null));
	        System.out.println(dao.jugarCarta(actiu, carta, TipusRecurs.FUSTA));      // monopoli

	        System.out.println("vertexsDisponibles");
	        System.out.println(dao.vertexsDisponibles(actiu).size() + " vertexs");

	        System.out.println("punts");
	        System.out.println(dao.calcularPunts(actiu));
	    }
	}

