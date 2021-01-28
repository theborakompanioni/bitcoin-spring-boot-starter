package org.tbk.bitcoin.tool.fee;

import com.google.common.base.MoreObjects;
import lombok.*;

public interface ProviderInfo {

    String getName();

    String getDescription();

    @Value
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class SimpleProviderInfo implements ProviderInfo {
        @NonNull
        String name;

        @NonNull
        String description;

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .addValue(name)
                    .toString();
        }
    }

}
