# Colons de Catan amb Hibernate (M0486 - BA2)

Versió simplificada dels Colons de Catan feta amb **Hibernate (JPA)** i **MySQL**, amb el patró DAO.
Aquesta és l'**entrega Hibernate** del projecte grupal.

**Autors:** Hugo Martínez Fernández, Ruben Perea i Denis Pérez.

## 1. Què fa el projecte

Es poden crear partides de 2 a 4 jugadors i jugar-les per torns: col·locar els dos primers poblats i carreteres
(fase de preparació), tirar els daus i cobrar la producció, construir carreteres, poblats i ciutats, comerciar amb la
banca, comprar i jugar cartes de desenvolupament i moure el lladre. La partida acaba quan un jugador arriba als punts
de victòria necessaris (10 per defecte).

El programa **no té interfície**: tot es fa a través dels DAO (`PartidaDao` i `JugadorDao`). La classe `main.Main`
n'és una demostració executable que juga una partida completa.

## 2. Tecnologies

| | |
|---|---|
| Java | 21 |
| Hibernate ORM | 6.6.58.Final (Jakarta Persistence 3.1) |
| Validació | Hibernate Validator 8.0.5 + Expressly |
| Base de dades | MySQL 8 (execució) · H2 en memòria (tests) |
| Tests | JUnit Jupiter 5.12.2 |
| Gestor | Maven |

## 3. Com executar-lo

1. Tenir MySQL en marxa. Per defecte es fa servir `localhost:3306`, usuari `root` i contrasenya `super3`
   (canvieu-ho a `src/main/resources/hibernate.cfg.xml` si cal). La base de dades `catan` es crea sola.
2. Executar `main.Main` (des de l'IDE o amb Maven). Com que el cfg té `hbm2ddl.auto = create`, **cada execució
   esborra i torna a crear les taules**.
3. Els fitxers de topologia del tauler (`hexagons.csv`, `vertexs.csv`, `hexagon_vertex.csv` i `arestes.csv`) són a
   `src/main/resources/dades` i es carreguen a cada `iniciarPartida`.

### Tests

```
mvn test
```

Els tests fan servir **H2 en memòria** (`src/test/resources/hibernate.cfg.xml`, que té prioritat sobre el de
`src/main/resources`). Per això són ràpids (uns 20 segons) i **no toquen la base de dades MySQL**.

## 4. Estructura

```
src/main/java
├── entities/   Entitats JPA (model de dades)
├── enums/      ColorJugador, EstatPartida, TipusCarta, TipusRecurs, TipusTerreny
├── dao/        IGenericDao/GenericDao, IPartidaDao/PartidaDao, IJugadorDao/JugadorDao, SessionManager, Missatges
├── util/       Costos (preus i límits), CsvLector (càrrega del tauler)
└── main/       Main (demostració)
src/main/resources
├── hibernate.cfg.xml
└── dades/      CSV del tauler
src/test/java/test        Tests d'integració (JUnit)
src/test/resources        Configuració de Hibernate per als tests (H2)
```

## 5. Model de dades

| Entitat | Què representa | Relacions principals |
|---|---|---|
| `Partida` | Estat, ronda, punts per guanyar, daus tirats, última tirada, lladre pendent | `jugadorActiu`, `guanyador` i `hexagonLladre` (`@ManyToOne`); jugadors, hexàgons, vèrtexs, arestes i cartes (`@OneToMany`) |
| `Jugador` | Nom, color, ordre de torn, cavallers jugats | Pertany a una `Partida`; té recursos, construccions i cartes |
| `RecursJugador` | Inventari: una fila per jugador i tipus de recurs | `@ManyToOne` Jugador; `UniqueConstraint(jugador, tipus)` |
| `Hexagon` | Terreny, número, coordenades q i r | Pertany a una `Partida`; es relaciona amb els vèrtexs a través d'`HexagonVertex` |
| `Vertex` | Punt on es construeix | Pertany a una `Partida`; porta un `Assentament` (`@OneToOne`) |
| `Aresta` | Camí entre dos vèrtexs | `vertexA` i `vertexB`; porta una `Carretera` (`@OneToOne`) |
| `HexagonVertex` | Taula intermèdia hexàgon ↔ vèrtex, amb la posició (0-5) | `@ManyToOne` Hexagon i Vertex |
| `Construccio` | Classe abstracta: propietari de la construcció | `@ManyToOne` Jugador |
| `Assentament` → `Poblat` / `Ciutat` | Construccions sobre un vèrtex | `@OneToOne` Vertex (únic) |
| `Carretera` | Construcció sobre una aresta | `@OneToOne` Aresta (únic) |
| `CartaDesenvolupament` | CAVALLER, MONOPOLI o PUNT_VICTORIA | Pertany a una `Partida`; el `Jugador` és opcional (la pila no en té) |

### Decisions de disseny

- **Herència `JOINED`** a `Construccio` → `Assentament` → `Poblat`/`Ciutat`, i `Carretera`: cada subclasse té la
  seva taula i comparteixen la clau amb la del pare. Un poblat millorat a ciutat deixa de ser un `Poblat` i passa
  a ser una `Ciutat` al mateix vèrtex.
- **`Costos`** concentra els preus (carretera, poblat, ciutat, carta) i els límits (15 carreteres, 5 poblats,
  4 ciutats, comerç 4:1) perquè no hi hagi números màgics repartits pel codi.
- **`Missatges`** concentra tots els textos de resultat dels DAO, perquè siguin coherents i fàcils de comprovar.
- **Cada tauler és independent**: es crea a `iniciarPartida`. L'`id` de la BD no coincideix amb el `codi` dels
  CSV a partir de la segona partida, i tots els mètodes treballen amb l'`id` de la BD.
- **Claus autogenerades** (`IDENTITY`), sense claus compostes.
- **`fetch` escrit sempre**: `EAGER` quan l'altre extrem sempre es fa servir (per exemple els vèrtexs d'una aresta)
  i `LAZY` a les col·leccions.
- **`@Check`** a `Aresta.codi` (1-72) i a `Partida.ultimaTirada` (0 o 2-12).

## 6. DAO

Tots els DAO hereten de `GenericDao` (CRUD bàsic amb `getCurrentSession()`). Els mètodes públics obren i tanquen
la transacció i fan *rollback* si hi ha una excepció (`handleException`). Els resultats es retornen amb els
textos de `Missatges`.

### `PartidaDao`

| Mètode | Què fa |
|---|---|
| `iniciarPartida(noms)` / `iniciarPartida(noms, punts)` | Valida (2-4 jugadors, noms no buits, mínim 3 punts), crea la partida, el tauler des dels CSV, els jugadors amb el seu color i ordre, els 5 recursos a 0, la pila de 21 cartes (14 CAVALLER, 5 PUNT_VICTORIA i 2 MONOPOLI) i el lladre al desert. Retorna l'id o `-1`. |
| `tirarDaus(idPartida)` | Tira dos daus i fa la producció amb una consulta HQL: cada hexàgon amb el número, sense lladre, dóna 1 recurs per poblat i 2 per ciutat. Amb un 7 no hi ha producció i s'activa el lladre pendent. |
| `comprovarVictoria(idPartida)` | Només durant `EN_CURS`: si un jugador arriba als punts, la partida passa a `FINALITZADA` i es fixa el guanyador (el primer; no canvia més). |
| `mostrarTauler(idPartida)` | Mostra els hexàgons amb terreny, número, lladre i construccions adjacents. |
| `mostrarPartida(idPartida)` | Estat, ronda, jugador actiu i, per jugador, punts, poblats, ciutats, carreteres i recursos. |

### `JugadorDao`

| Mètode | Què fa |
|---|---|
| `construirPoblat` / `construirCarretera` / `millorarACiutat` | Comproven partida, torn, daus, lladre, posició lliure, regla de la distància, connectivitat, recursos i límits. A `PREPARACIO` són gratuïts (un poblat i una carretera per ronda). |
| `comerciarBanca(jugador, dona, rep)` | Canvia 4 recursos d'un tipus per 1 d'un altre. |
| `calcularPunts(jugador)` | Poblats + 2 × ciutats + cartes PUNT_VICTORIA + 2 si té l'exèrcit més gran (3 cavallers i més que ningú). |
| `passarTorn(jugador)` | Passa al següent jugador; després de l'últim torna al primer i puja la ronda. A `PREPARACIO` segueix l'ordre de serp (ronda 1 d'anada, ronda 2 de tornada) i després passa a `EN_CURS`. |
| `comprarCarta` / `jugarCarta` | Compra de la pila a l'atzar; el cavaller activa el moviment del lladre i el monopoli roba un tipus de recurs a tothom. |
| `moureLladre(jugador, hexagon, víctima)` | Mou el lladre a un altre hexàgon i roba 1 recurs a un rival que hi tingui un assentament. |
| `vertexsDisponibles(jugador)` | Vèrtexs on el jugador pot construir un poblat, amb una consulta HQL (no es recorren tots els vèrtexs en Java). |

### Consultes

Es fa servir HQL a quasi tots els mètodes de consulta. `RecursJugador` defineix dues `@NamedQuery`
(`RecursJugador.perJugador` i `RecursJugador.perJugadorITipus`).

### Transaccions i errors

- Un mètode públic = una transacció. `JugadorDao` té un petit mètode intern `transaccio(...)` que permet que
  una operació en cridi una altra sense obrir una segona transacció.
- Qualsevol error de regla (no és el teu torn, posició ocupada...) retorna un missatge i **no canvia res**; els
  errors de base de dades fan *rollback* complet.

## 7. Document de proves

El projecte inclou **111 mètodes de test i 126 execucions** (alguns són parametritzats), i tots passen. S'executen amb
`mvn test`. `BaseDbTest` tanca la `SessionFactory` abans de cada test; la següent torna a crear les taules, així cap
test depèn de l'estat que ha deixat un altre. `Joc` conté els ajudants que consulten o modifiquen la BD directament,
sense passar pels DAO.

| Classe | Tests | Què comprova |
|---|---:|---|
| `T01_IniciarPartidaTest` | 19 | Estat inicial; validació de noms, jugadors i punts; tauler (19 hexàgons, 54 vèrtexs, 72 arestes, 114 relacions); lladre al desert; torns, colors, recursos a 0 i pila de cartes |
| `T02_PreparacioTest` | 15 | Poblat i carretera gratuïts; un per ronda; ocupació; regla de la distància; elements d'una altra partida; no passar torn sense construir; recursos inicials del segon poblat |
| `T03_TornIFaseTest` | 8 | Daus abans d'actuar; una tirada per torn; torn i ronda; partida finalitzada |
| `T04_ConstruccioTest` | 18 | Carretera, poblat i ciutat: costos, connectivitat, regla de la distància, límits, errors i *rollback* |
| `T05_ProduccioTest` | 7 | Producció de poblats i ciutats, lladre, tirades de 2 a 12 i el 7 |
| `T06_PuntsIVictoriaTest` | 11 | Càlcul de punts, exèrcit més gran, victòria per construir o comprar, `comprovarVictoria` en cada estat |
| `T07_ComercBancaTest` | 6 | Comerç 4:1 i els seus errors |
| `T08_CartesILladreTest` | 18 | Compra i ús de cartes, cavaller, monopoli, moviment del lladre i robatori |
| `T09_VertexsDisponiblesTest` | 3 | Regla de la distància i connectivitat als vèrtexs disponibles |
| `T13_GenericDaoTest` | 6 | CRUD de `GenericDao` i `SessionManager` |

També s'ha jugat una partida completa amb `main.Main` sobre MySQL, amb els errors esperats i les accions
correctes.

## 8. Limitacions conegudes

- Amb una tirada de **7** s'activa el lladre pendent, però **no s'aplica el descart de la meitat dels recursos**
  (part avançada).
- No hi ha carretera més llarga, ports, comerç entre jugadors ni cartes de progrés (simplificacions de l'enunciat).
- La part **Spring** (endpoints REST) correspon a una altra entrega i encara no està feta.
