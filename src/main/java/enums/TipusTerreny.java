package enums;

public enum TipusTerreny {
    BOSC(TipusRecurs.FUSTA),
    TURONS(TipusRecurs.MAO),
    PASTURES(TipusRecurs.LLANA),
    CAMPS(TipusRecurs.BLAT),
    MUNTANYES(TipusRecurs.MINERAL),
    DESERT(null);

    private final TipusRecurs recurs;

    private TipusTerreny(TipusRecurs recurs) {
        this.recurs = recurs;
    }

    public TipusRecurs getRecurs() {
        return this.recurs;
    }
}
