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
package io.micrometer.core.instrument.prometheus;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PrometheusTagFormatterTest {
    private PrometheusTagFormatter formatter = new PrometheusTagFormatter();

    @Test
    void formatName() {
        assertThat(formatter.formatName("123abc/{:id}水")).isEqualTo("m_123abc__:id__");
    }

    @Test
    void formatTagKey() {
        assertThat(formatter.formatTagKey("123abc/{:id}水")).isEqualTo("m_123abc___id__");
    }
}
