package org.tbk.bitcoin.example.payreq.lnd;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import lombok.NonNull;
import org.lightningj.lnd.wrapper.StatusException;
import org.lightningj.lnd.wrapper.SynchronousLndAPI;
import org.lightningj.lnd.wrapper.ValidationException;
import org.lightningj.lnd.wrapper.message.Chain;
import org.springframework.stereotype.Component;
import org.tbk.bitcoin.example.payreq.common.Network;

import java.util.Collections;
import java.util.List;

@Component
public class CachedLndInfos {

    private final Supplier<List<Chain>> chains;

    public CachedLndInfos(@NonNull SynchronousLndAPI lndApi) {
        this.chains = Suppliers.memoize(() -> {
            try {
                return Collections.unmodifiableList(lndApi.getInfo().getChains());
            } catch (StatusException | ValidationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Chain> chains() {
        return chains.get();
    }

    public boolean supportsNetwork(Network network) {
        return chains().stream()
                .filter(it -> "bitcoin".equals(it.getChain()))
                .anyMatch(it -> network.name().equals(it.getNetwork()));
    }
}
