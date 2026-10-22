package enums;

public enum TipusTerreny {
    BOSC(TipusRecurs.FUSTA),
    TURONS(TipusRecurs.MAO),
    PASTURES(TipusRecurs.LLANA),
    CAMPS(TipusRecurs.BLAT),
    MUNTANYES(TipusRecurs.MINERAL),
    DESERT(null);

    private final TipusRecurs recurs;

    TipusTerreny(TipusRecurs recurs) {
        this.recurs = recurs;
    }

    // Recurs que produeix el terreny (null al desert). Un enum pot tenir atributs i mètodes:
    // Hibernate no els toca, només guarda el nom del valor (EnumType.STRING).
    public TipusRecurs getRecurs() {
        return recurs;
    }
}
