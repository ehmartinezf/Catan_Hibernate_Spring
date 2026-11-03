package util;


public final class Missatges {

    private Missatges() {

    }
  
    public static final String ERROR = "ERROR: ";
    public static final String ERROR_INTERN = "ERROR intern: ";
    public static final String LIMIT_ASSOLIT = "Limit assolit: maxim ";
    public static final String SENSE_RECURSOS = "Sense recursos: necessites ";
    public static final String JUGADOR_NO_EXISTEIX = "El jugador no existeix";
    public static final String PARTIDA_FINALITZADA = "La partida ja ha finalitzat";
    public static final String PARTIDA_NO_EN_CURS = "La partida no esta en curs";
    public static final String NO_ES_EL_TEU_TORN = "No es el teu torn";
    public static final String DAUS_NO_TIRATS = "Primer has de tirar els daus";
    public static final String LLADRE_PENDENT = "Has de moure el lladre abans de fer res mes";
    public static final String ARESTA_ALTRA_PARTIDA = "L'aresta no pertany a la teva partida";
    public static final String VERTEX_ALTRA_PARTIDA = "El vertex no pertany a la teva partida";
    public static final String HEXAGON_ALTRA_PARTIDA = "L'hexagon no pertany a la teva partida";
    public static final String ARESTA_OCUPADA = "Posicio ocupada: l'aresta ja te una carretera";
    public static final String VERTEX_OCUPAT = "Posicio ocupada: el vertex ja te un assentament";
    public static final String PREP_CARRETERA_SENSE_POBLAT = "A la preparacio has de col·locar primer un poblat";
    public static final String PREP_CARRETERA_JA_COLLOCADA = "Ja has col·locat la carretera d'aquesta ronda";
    public static final String PREP_POBLAT_JA_COLLOCAT = "Ja has col·locat el poblat d'aquesta ronda";
    public static final String PREP_POBLAT_SENSE_CARRETERA = "Primer has de col·locar la carretera del poblat anterior";
    public static final String PREP_PASSAR_INCOMPLET = "Has de col·locar el poblat i la carretera d'aquest torn abans de passar";
    public static final String NO_TOCA_POBLAT_NOU = "No connectat: ha de tocar el poblat que acabes de col·locar";
    public static final String ARESTA_NO_CONNECTADA = "No connectat: l'aresta no toca cap construccio teva";
    public static final String VERTEX_NO_CONNECTAT = "No connectat: cap carretera teva arriba a aquest vertex";
    public static final String REGLA_DISTANCIA = "Regla de la distancia: hi ha un assentament en un vertex adjacent";
    public static final String NO_HI_HA_POBLAT_PER_MILLORAR = "No hi ha cap poblat teu per millorar en aquest vertex";
    public static final String CAL_DOS_TIPUS_RECURS = "Cal indicar els dos tipus de recurs";
    public static final String RECURSOS_IGUALS = "Els recursos que dones i reps han de ser diferents";
    public static final String LLADRE_NO_PENDENT = "No hi ha cap moviment de lladre pendent";
    public static final String LLADRE_MATEIX_HEXAGON = "El lladre s'ha de moure a un hexagon diferent de l'actual";
    public static final String VICTIMA_NO_VALIDA = "La victima ha de ser un altre jugador amb un assentament en aquest hexagon";
    public static final String VICTIMA_SENSE_RECURSOS = "La victima no te recursos";
    public static final String CAL_INDICAR_VICTIMA = "Has d'indicar una victima: hi ha jugadors amb recursos en aquest hexagon";
    public static final String PILA_BUIDA = "La pila de cartes esta buida";
    public static final String CARTA_NO_ES_TEVA = "Aquesta carta no es a la teva ma";
    public static final String CARTA_JA_USADA = "Aquesta carta ja ha estat jugada";
    public static final String CARTA_MATEIXA_RONDA = "No pots jugar una carta a la mateixa ronda en que l'has comprat";
    public static final String CAL_RECURS_MONOPOLI = "Cal indicar el tipus de recurs del monopoli";
    public static final String CARTA_PUNT_VICTORIA = "Les cartes de PUNT_VICTORIA no es juguen: compten automaticament";
}