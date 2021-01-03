package org.tbk.spring.testcontainer.tor;

import com.google.common.base.Strings;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerState;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class HiddenServiceHostnames implements HiddenServiceHostnameResolver {
    private final String hiddenServiceHome;
    private final TorContainer<?> container;

    public HiddenServiceHostnames(TorContainer<?> container, String hiddenServiceHome) {
        this.container = requireNonNull(container);
        this.hiddenServiceHome = requireNonNull(hiddenServiceHome);
    }

    @Override
    public Optional<String> findHiddenServiceUrl(String serviceName) {
        return findHostnameFileByServiceName(serviceName)
                .map(it -> readContentOfFileInContainerOrThrow(this.container, it));
    }

    public Map<String, String> findAllHiddenServiceUrls() {
        List<String> hostnameFiles = findAllHostnameFiles();

        Map<String, String> fileToContents = hostnameFiles.stream()
                .collect(Collectors.toMap(val -> val, val -> readContentOfFileInContainerOrThrow(this.container, val)));

        return fileToContents;
    }

    private Optional<String> findHostnameFileByServiceName(String serviceName) {
        String[] command = {"find", hiddenServiceHome + "/" + serviceName, "-type", "f", "-name", "hostname"};
        Container.ExecResult execResult = execOrThrow(command);

        return Arrays.stream(execResult.getStdout().split("\n"))
                .filter(val -> !val.isBlank())
                .findFirst();
    }

    private List<String> findAllHostnameFiles() {
        String[] command = {"find", hiddenServiceHome, "-type", "f", "-name", "hostname"};
        Container.ExecResult execResult = execOrThrow(command);

        return Arrays.stream(execResult.getStdout().split("\n"))
                .filter(val -> !val.isBlank())
                .collect(Collectors.toList());
    }

    private Container.ExecResult execOrThrow(String[] command) {
        try {
            return this.container.execInContainer(command);
        } catch (IOException | InterruptedException e) {
            String errorMessage = String.format("Error while executing command '%s' in container %s",
                    String.join(",", command), container.getContainerId());

            throw new RuntimeException(errorMessage, e);
        }
    }

    private String readContentOfFileInContainerOrThrow(ContainerState containerState, String file) {
        String[] command = {"cat", file};

        Container.ExecResult execResult = execOrThrow(command);

        boolean success = execResult.getExitCode() == 0;
        boolean containsErrorMessage = !Strings.isNullOrEmpty(execResult.getStderr());

        if (!success || containsErrorMessage) {
            String errorMessage = String.format("Error while reading file %s in container %s: %s",
                    file, containerState.getContainerId(), execResult.getStderr());

            throw new RuntimeException(errorMessage);
        }

        String stdout = execResult.getStdout();

        boolean endsWithNewLine = stdout.endsWith("\n");
        if (endsWithNewLine) {
            return stdout.substring(0, stdout.lastIndexOf("\n"));
        }

        return stdout;
    }
}
