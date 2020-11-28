package org.tbk.spring.neo4j.testcontainer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

@Data
@ConfigurationProperties(
        prefix = "org.tbk.spring.neo4j.testcontainer",
        ignoreUnknownFields = false
)
public class Neo4jContainerProperties implements Validator {
    private static final String imageNamespace = "neo4j";

    private static final String defaultDockerImageVersion = "4.1.3";

    private static final DockerImageName defaultDockerImageName = DockerImageName
            .parse(imageNamespace + ":" + defaultDockerImageVersion);

    private boolean enabled;

    private String image;

    public Optional<String> getImage() {
        return Optional.ofNullable(image);
    }

    public DockerImageName getDockerImageName() {
        return Optional.ofNullable(image)
                .map(DockerImageName::parse)
                .orElse(defaultDockerImageName);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Neo4jContainerProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Neo4jContainerProperties properties = (Neo4jContainerProperties) target;

        if (properties.getImage().isPresent()) {
            String imageName = properties.getImage().get();
            if (!imageName.startsWith(imageNamespace)) {
                String errorMessage = String.format("'image' value must start with '%s' - invalid value: %s", imageNamespace, imageName);
                errors.rejectValue("image", "image.invalid", errorMessage);
            }

            try {
                DockerImageName.parse(imageName).assertValid();
            } catch (IllegalArgumentException e) {
                errors.rejectValue("image", "image.invalid", e.getMessage());
            }
        }
    }
}

