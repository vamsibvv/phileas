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

import ai.philterd.phileas.services.filters.regex.DeaNumberFilter;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DEANumberGeneratorTest {

    @Test
    public void testGenerateDEANumber() {
        final DEANumberGenerator generator = new DEANumberGenerator(new SecureRandom());
        for (int i = 0; i < 1000; i++) {
            final String dea = generator.random();
            assertNotNull(dea, "DEA number should not be null");
            assertTrue(dea.matches("[A-Z]{2}\\d{7}"), "DEA number should be two letters and seven digits but was " + dea);
            assertTrue(DeaNumberFilter.isValid(dea), "DEA number should pass the check digit but was " + dea);
        }
    }

    @Test
    public void testGenerateDEANumberWithLetters() {
        final DEANumberGenerator generator = new DEANumberGenerator(new SecureRandom());
        for (int i = 0; i < 100; i++) {
            final String dea = generator.random('F', '9');
            assertTrue(dea.matches("F9\\d{7}"), "DEA number should begin with F9 but was " + dea);
            assertTrue(DeaNumberFilter.isValid(dea), "DEA number should pass the check digit but was " + dea);
        }
    }

    @Test
    public void testCheckDigit() {
        // AB1234563: 1 + 3 + 5 = 9, and 2 * (2 + 4 + 6) = 24, so the check digit is the last digit of 33.
        assertEquals(3, DEANumberGenerator.checkDigit(new int[]{1, 2, 3, 4, 5, 6}));
    }

    @Test
    public void testPoolSize() {
        final DEANumberGenerator generator = new DEANumberGenerator(new SecureRandom());
        assertEquals(18L * 26L * 1000000L, generator.poolSize());
    }

}
