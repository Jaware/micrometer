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
package io.micrometer.core.instrument.spectator;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.util.MeterEquivalence;

public class SpectatorDistributionSummary implements DistributionSummary {
    private com.netflix.spectator.api.DistributionSummary distributionSummary;

    public SpectatorDistributionSummary(com.netflix.spectator.api.DistributionSummary distributionSummary) {
        this.distributionSummary = distributionSummary;
    }

    /**
     * @param amount Amount for an event being measured. For this implementation,
     *               amount is truncated to a long because the underlying Spectator
     *               implementation takes a long.
     */
    @Override
    public void record(double amount) {
        distributionSummary.record((long) amount);
    }

    @Override
    public long count() {
        return distributionSummary.count();
    }

    @Override
    public double totalAmount() {
        return distributionSummary.totalAmount();
    }

    @Override
    public String getName() {
        return distributionSummary.id().name();
    }

    @Override
    public Iterable<Tag> getTags() {
        return SpectatorUtils.tags(distributionSummary);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return MeterEquivalence.equals(this, o);
    }

    @Override
    public int hashCode() {
        return MeterEquivalence.hashCode(this);
    }
}
