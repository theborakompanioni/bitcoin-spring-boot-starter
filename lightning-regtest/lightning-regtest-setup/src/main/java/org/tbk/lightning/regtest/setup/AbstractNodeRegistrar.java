package org.tbk.lightning.regtest.setup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;

@Slf4j
abstract class AbstractNodeRegistrar implements ImportBeanDefinitionRegistrar {

    protected BeanDefinitionCustomizer beanDefinitionCustomizer() {
        return bd -> {
            // empty on purpose
        };
    }

    abstract protected String beanNamePrefix();

    abstract protected String hostname();

    abstract protected Integer p2pPort();
}
