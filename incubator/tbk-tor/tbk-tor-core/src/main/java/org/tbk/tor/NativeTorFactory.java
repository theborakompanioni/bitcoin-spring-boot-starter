package org.tbk.tor;

import lombok.SneakyThrows;
import org.berndpruenster.netlayer.tor.NativeTor;

import java.io.File;

public class NativeTorFactory implements TorFactory<NativeTor> {

    @Override
    @SneakyThrows
    public NativeTor create() {
        File workingDirectory = new File("tor-working-dir");
        return new NativeTor(workingDirectory);
    }
}
