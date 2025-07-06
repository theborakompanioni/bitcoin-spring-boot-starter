package org.tbk.electrum.model;

public interface RawTx {

    String getHex();

    boolean isUnsigned();
}
