package com.gservices.dto;

import lombok.Data;

import java.io.Serializable;

/** Agrégat des avis publiés d'une cible : moyenne, total et répartition par note. */
@Data
public class SyntheseAvisDto implements Serializable {
    private double moyenne;
    private long total;
    /** répartition[i] = nombre d'avis de note (i+1), i de 0 à 4. */
    private long[] repartition = new long[5];

    public long getNote1() { return repartition[0]; }
    public long getNote2() { return repartition[1]; }
    public long getNote3() { return repartition[2]; }
    public long getNote4() { return repartition[3]; }
    public long getNote5() { return repartition[4]; }

    public int getMoyenneArrondie() { return (int) Math.round(moyenne); }
}
