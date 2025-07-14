package org.tbk.electrum.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class SimpleSeed implements Seed {

    @Singular("addWord")
    List<String> words;

    @Override
    public String getPhrase() {
        return String.join(" ", words);
    }
}
