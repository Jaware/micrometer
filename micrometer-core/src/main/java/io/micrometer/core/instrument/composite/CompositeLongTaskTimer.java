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
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.noop.NoopLongTaskTimer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CompositeLongTaskTimer extends AbstractMeter implements LongTaskTimer, CompositeMeter {
    private final Map<MeterRegistry, LongTaskTimer> timers = Collections.synchronizedMap(new LinkedHashMap<>());

    CompositeLongTaskTimer(String name, Iterable<Tag> tags) {
        super(name, tags);
    }

    @Override
    public long start() {
        synchronized (timers) {
            return timers.values().stream()
                .map(LongTaskTimer::start)
                .reduce((t1, t2) -> t2)
                .orElse(NoopLongTaskTimer.INSTANCE.start());
        }
    }

    @Override
    public long stop(long task) {
        synchronized (timers) {
            return timers.values().stream()
                .map(ltt -> ltt.stop(task))
                .reduce((t1, t2) -> t2 == -1 ? t1 : t2)
                .orElse(NoopLongTaskTimer.INSTANCE.stop(task));
        }
    }

    @Override
    public long duration(long task) {
        synchronized (timers) {
            return timers.values().stream()
                .map(ltt -> ltt.duration(task))
                .reduce((t1, t2) -> t2 == -1 ? t1 : t2)
                .orElse(NoopLongTaskTimer.INSTANCE.duration(task));
        }
    }

    @Override
    public long duration() {
        synchronized (timers) {
            return timers.values().stream()
                .map(LongTaskTimer::duration)
                .reduce((t1, t2) -> t2)
                .orElse(NoopLongTaskTimer.INSTANCE.duration());
        }
    }

    @Override
    public int activeTasks() {
        synchronized (timers) {
            return timers.values().stream()
                .map(LongTaskTimer::activeTasks)
                .reduce((t1, t2) -> t2)
                .orElse(NoopLongTaskTimer.INSTANCE.activeTasks());
        }
    }

    @Override
    public void add(MeterRegistry registry) {
        synchronized (timers) {
            timers.put(registry, registry.more().longTaskTimer(getName(), getTags()));
        }
    }

    @Override
    public void remove(MeterRegistry registry) {
        synchronized (timers) {
            timers.remove(registry);
        }
    }
}
