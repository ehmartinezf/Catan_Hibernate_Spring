<<<<<<< HEAD
package entities;

import enums.TipusCarta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "carta_desenvolupament")
public class CartaDesenvolupament {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Enumerated(EnumType.STRING)
    private TipusCarta tipus;
    
    @Column(name = "usada")
    private boolean usada;
    
    @Column(name = "ronda_compra", nullable = false)
    private Integer rondaCompra;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;
    
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;
}
=======
package entities;

import enums.TipusCarta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "carta_desenvolupament")
public class CartaDesenvolupament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private TipusCarta tipus;

    @Column(name = "usada")
    private boolean usada;

    @Column(name = "ronda_compra", nullable = false)
    private Integer rondaCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;
}
>>>>>>> 493fdff2fd126b09a5a7f667a151fff7244b4c8e
