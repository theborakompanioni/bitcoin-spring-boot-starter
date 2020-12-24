package org.tbk.electrum.model;

public interface RawTx {

    String getHex();

    boolean isComplete();

    boolean isFinalized();
}
