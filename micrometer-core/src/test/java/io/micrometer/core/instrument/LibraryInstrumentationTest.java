/**
 * Copyright 2017 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

import static io.micrometer.core.instrument.Meters.lazyCounter;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jon Schneider
 */
class LibraryInstrumentationTest {
    @Test
    void injectWithSpring() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);
        MyComponent component = ctx.getBean(MyComponent.class);
        component.performanceCriticalFeature();
        assertThat(component.registry)
                .isInstanceOf(PrometheusMeterRegistry.class)
                .matches(r -> r.find("feature.counter").counter().isPresent());
    }

    @Test
    void injectWithDagger() {
        DagConfiguration conf = DaggerDagConfiguration.create();
        MyComponent component = conf.component();
        component.performanceCriticalFeature();
        assertThat(component.registry)
                .isInstanceOf(PrometheusMeterRegistry.class)
                .matches(r -> r.find("feature.counter").counter().isPresent());
    }

    @Test
    void injectWithGuice() {
        Injector injector = Guice.createInjector(new GuiceConfiguration());
        MyComponent component = injector.getInstance(MyComponent.class);
        component.performanceCriticalFeature();
        assertThat(component.registry)
                .isInstanceOf(PrometheusMeterRegistry.class)
                .matches(r -> r.find("feature.counter").counter().isPresent());
    }

    @Test
    void noInjection() {
        MyComponent component = new MyComponent();
        component.performanceCriticalFeature();
        assertThat(component.registry)
                .isInstanceOf(CompositeMeterRegistry.class)
                .matches(r -> r.find("feature.counter").counter().isPresent());
    }
}

@Component(modules = DagConfiguration.RegistryConf.class)
interface DagConfiguration {
    MyComponent component();

    @Module
    class RegistryConf {
        @Provides
        static MeterRegistry registry() {
            return new PrometheusMeterRegistry(new CollectorRegistry());
        }
    }
}

@Configuration
class SpringConfiguration {
    @Bean
    PrometheusMeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(new CollectorRegistry());
    }

    @Bean
    MyComponent component() {
        return new MyComponent();
    }
}

class GuiceConfiguration extends AbstractModule {
    @Override
    protected void configure() {
        bind(MeterRegistry.class).to(PrometheusMeterRegistry.class);
    }
}

class MyComponent {
    @Inject MeterRegistry registry = MeterRegistry.globalRegistry;

    // for performance-critical uses, it is best to store a meter in a field
    Counter counter = lazyCounter(() -> registry.counter("feature.counter"));

    void performanceCriticalFeature() {
        counter.increment();
    }

    void notPerformanceCriticalFeature() {
        // in code blocks that are not performance-critical, it is acceptable to inline
        // the retrieval of the counter
        registry.counter("infrequent.counter").increment();
    }

    @Inject MyComponent() {}
}
