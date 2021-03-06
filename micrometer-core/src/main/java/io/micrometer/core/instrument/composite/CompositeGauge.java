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
package io.micrometer.core.instrument.composite;

import io.micrometer.core.instrument.AbstractMeter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.noop.NoopGauge;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class CompositeGauge<T> extends AbstractMeter implements Gauge, CompositeMeter {
    private final WeakReference<T> ref;
    private final ToDoubleFunction<T> f;

    private final Map<MeterRegistry, Gauge> gauges = Collections.synchronizedMap(new LinkedHashMap<>());

    CompositeGauge(String name, Iterable<Tag> tags, T obj, ToDoubleFunction<T> f) {
        super(name, tags);
        this.ref = new WeakReference<>(obj);
        this.f = f;
    }

    @Override
    public double value() {
        synchronized (gauges) {
            return gauges.values().stream().findFirst().orElse(NoopGauge.INSTANCE).value();
        }
    }

    @Override
    public void add(MeterRegistry registry) {
        T obj = ref.get();
        if(obj != null) {
            synchronized (gauges) {
                gauges.put(registry, registry.gaugeBuilder(getName(), obj, f).tags(getTags()).create());
            }
        }
    }

    @Override
    public void remove(MeterRegistry registry) {
        synchronized (gauges) {
            gauges.remove(registry);
        }
    }
}
