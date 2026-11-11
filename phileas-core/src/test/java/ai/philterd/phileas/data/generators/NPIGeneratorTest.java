/*
 *     Copyright 2026 Philterd, LLC @ https://www.philterd.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.philterd.phileas.data.generators;

import ai.philterd.phileas.services.filters.regex.NpiFilter;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NPIGeneratorTest {

    @Test
    public void testGenerateNPI() {
        final NPIGenerator generator = new NPIGenerator(new SecureRandom());
        for (int i = 0; i < 1000; i++) {
            final String npi = generator.random();
            assertNotNull(npi, "NPI should not be null");
            assertTrue(npi.matches("[12]\\d{9}"), "NPI should be ten digits beginning with 1 or 2 but was " + npi);
            assertTrue(NpiFilter.isValid(npi), "NPI should pass the NPI check digit but was " + npi);
        }
    }

    @Test
    public void testCheckDigit() {
        // The example in the CMS check digit document: 123456789 has the check digit 3.
        assertEquals(3, NPIGenerator.checkDigit(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
    }

    @Test
    public void testPoolSize() {
        final NPIGenerator generator = new NPIGenerator(new SecureRandom());
        assertEquals(2L * 100000000L, generator.poolSize());
    }

}
