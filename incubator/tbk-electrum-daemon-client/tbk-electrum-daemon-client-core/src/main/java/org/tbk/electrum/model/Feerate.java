package org.tbk.electrum.model;

public interface Feerate {
    String getPolicy();

    SatPerVbyte getSatPerVbyte();
}
