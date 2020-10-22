package org.tbk.spring.jsr354;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.money.spi.ServiceProvider;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringContextAwareServiceProvider implements ApplicationContextAware, ServiceProvider {

    private final int priority;
    private final ServiceProvider delegateOrNull;

    private volatile ApplicationContext applicationContext;

    public SpringContextAwareServiceProvider() {
        this(0);
    }

    public SpringContextAwareServiceProvider(int priority) {
        this(priority, null);
    }


    public SpringContextAwareServiceProvider(int priority, ServiceProvider delegate) {
        this.priority = priority;
        this.delegateOrNull = delegate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public <T> List<T> getServices(Class<T> serviceType) {
        Collection<T> servicesFromSpring = getServicesFromSpringContext(serviceType);
        Collection<T> servicesFromDelegate = getServicesFromDelegate(serviceType);

        return Stream.concat(servicesFromSpring.stream(), servicesFromDelegate.stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        Collection<T> servicesFromSpring = getServicesFromSpringContext(serviceType);

        return servicesFromSpring.stream().findFirst()
                .or(() -> getServicesFromDelegate(serviceType).stream().findFirst())
                .orElse(null);
    }

    private <T> Collection<T> getServicesFromSpringContext(Class<T> serviceType) {
        return applicationContext.getBeansOfType(serviceType).values();
    }

    public <T> Collection<T> getServicesFromDelegate(Class<T> serviceType) {
        return delegate()
                .map(delegate -> delegate.getServices(serviceType))
                .orElseGet(Collections::emptyList);
    }

    private Optional<ServiceProvider> delegate() {
        return Optional.ofNullable(delegateOrNull);
    }

}
