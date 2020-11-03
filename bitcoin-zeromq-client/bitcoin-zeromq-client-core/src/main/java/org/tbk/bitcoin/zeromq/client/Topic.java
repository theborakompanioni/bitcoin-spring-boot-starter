package org.tbk.bitcoin.zeromq.client;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

public interface Topic {

    String getName();

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleTopic implements Topic {
        public static SimpleTopic of(String name) {
            return new SimpleTopic(name);
        }

        @NonNull
        String name;
    }
}
