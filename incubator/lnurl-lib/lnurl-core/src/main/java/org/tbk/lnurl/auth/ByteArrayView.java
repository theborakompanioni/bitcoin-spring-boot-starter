package org.tbk.lnurl.auth;

import java.io.Serializable;

public interface ByteArrayView extends Serializable {

    byte[] toArray();

    String toHex();
}
