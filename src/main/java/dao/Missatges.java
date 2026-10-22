package dao;

/*
 * Missatges de resultat de totes les operacions dels DAO.
 * Centralitzats aquí perquè siguin coherents i fàcils de comprovar (patró de Missatges.java de la teoria).
 */
public class Missatges {

    private Missatges() {
    }

    // ---- errors generals ----
    public static final String ERROR_BD = "Error de base de dades: s'ha desfet l'operació";
    public static final String PARTIDA_NO_EXISTEIX = "La partida no existeix";
    public static final String JUGADOR_NO_EXISTEIX = "El jugador no existeix";
    public static final String ARESTA_NO_EXISTEIX = "L'aresta no existeix";
    public static final String VERTEX_NO_EXISTEIX = "El vèrtex no existeix";
    public static final String HEXAGON_NO_EXISTEIX = "L'hexàgon no existeix";
    public static final String CARTA_NO_EXISTEIX = "La carta no existeix";
    public static final String ALTRA_PARTIDA = "Aquest element pertany a una altra partida";

    // ---- torn i fase ----
    public static final String PARTIDA_FINALITZADA = "La partida està finalitzada: no admet cap acció";
    public static final String NO_ES_EL_TEU_TORN = "No és el teu torn";
    public static final String NOMES_EN_CURS = "Aquesta acció només es pot fer amb la partida EN_CURS";
    public static final String CAL_TIRAR_DAUS = "Primer has de tirar els daus";
    public static final String DAUS_JA_TIRATS = "Ja has tirat els daus en aquest torn";
    public static final String LLADRE_PENDENT = "Cal moure el lladre abans de fer cap altra acció";
    public static final String LLADRE_NO_PENDENT = "No hi ha cap moviment de lladre pendent";

    // ---- construcció ----
    public static final String ARESTA_OCUPADA = "Ja hi ha una carretera en aquesta aresta";
    public static final String VERTEX_OCUPAT = "Ja hi ha un assentament en aquest vèrtex";
    public static final String REGLA_DISTANCIA = "Regla de la distància: hi ha un assentament en un vèrtex adjacent";
    public static final String NO_CONNECTAT = "La posició no està connectada a cap construcció o carretera teva";
    public static final String SENSE_RECURSOS = "No tens prou recursos";
    public static final String LIMIT_CARRETERES = "Has arribat al límit de 15 carreteres";
    public static final String LIMIT_POBLATS = "Has arribat al límit de 5 poblats";
    public static final String LIMIT_CIUTATS = "Has arribat al límit de 4 ciutats";
    public static final String NO_HI_HA_POBLAT = "No tens cap poblat en aquest vèrtex per millorar";
    public static final String PREP_POBLAT_PRIMER = "En PREPARACIO primer has de col·locar el poblat d'aquesta ronda";
    public static final String PREP_POBLAT_JA_COLOCAT = "Ja has col·locat el poblat d'aquesta ronda";
    public static final String PREP_CARRETERA_JA_COLOCADA = "Ja has col·locat la carretera d'aquesta ronda";
    public static final String PREP_CARRETERA_PRIMER = "En PREPARACIO primer has de col·locar la carretera del poblat anterior";
    public static final String PREP_CARRETERA_POBLAT = "En PREPARACIO la carretera ha de tocar el poblat que acabes de col·locar";
    public static final String PREP_CAL_COMPLETAR = "En PREPARACIO has de col·locar el poblat i la carretera abans de passar torn";

    public static final String CARRETERA_CONSTRUIDA = "Carretera construïda";
    public static final String POBLAT_CONSTRUIT = "Poblat construït";
    public static final String CIUTAT_CONSTRUIDA = "Poblat millorat a ciutat";

    // ---- comerç i cartes ----
    public static final String COMERC_IGUALS = "Has de donar i rebre tipus de recurs diferents";
    public static final String COMERC_FET = "Comerç fet: 4 recursos canviats per 1";
    public static final String PILA_BUIDA = "La pila de cartes és buida";
    public static final String CARTA_NO_ES_TEVA = "Aquesta carta no és a la teva mà";
    public static final String CARTA_JA_USADA = "Aquesta carta ja s'ha usat";
    public static final String CARTA_MATEIXA_RONDA = "No pots jugar una carta a la mateixa ronda en què l'has comprat";
    public static final String CARTA_PUNT_VICTORIA = "Les cartes de PUNT_VICTORIA no es juguen: compten soles";
    public static final String CARTA_MONOPOLI_RECURS = "Per jugar el MONOPOLI has d'indicar un tipus de recurs";

    // ---- lladre ----
    public static final String LLADRE_MATEIX_HEXAGON = "Has de moure el lladre a un hexàgon diferent de l'actual";
    public static final String VICTIMA_INVALIDA = "La víctima ha de ser un altre jugador amb un assentament en aquest hexàgon i amb recursos";
    public static final String VICTIMA_OBLIGATORIA = "Hi ha jugadors a qui es pot robar: has d'indicar una víctima";

    // ---- torn ----
    public static final String TORN_PASSAT = "Torn passat";
    public static final String PREPARACIO_ACABADA = "Torn passat. La fase de PREPARACIO ha acabat: la partida passa a EN_CURS";
}
