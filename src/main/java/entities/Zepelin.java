package entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "construccio")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipus_construccio", discriminatorType = DiscriminatorType.STRING, length = 15)
public abstract class Construccio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
}

@Entity
@Table(name = "assentament")
@DiscriminatorValue("assentament")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "tipus_assentament")
public abstract class Assentament extends Construccio {

    @OneToOne
    @Column(name = "vertex_id", unique = true, nullable = false)
    private Vertex vertex;

}

@Entity
@Table(name = "carretara")
@DiscriminatorValue("carretera")
public class Carretera extends Construccio {

    @OneToOne(mappedBy = "")
    @Column(name = "aresta_id", unique = true, nullable = false)
    private Aresta aresta;

}

@Entity
@Table(name = "poblat")
@DiscriminatorValue("poblat")
public class Poblat extends Assentament {

}

@Entity
@Table(name = "ciutat")
@DiscriminatorValue("ciutat")
public class Ciutat extends Assentament {

}

public class Vertex {

    private int id;
    private int codi;

    public class Zepelin {

    }
}
