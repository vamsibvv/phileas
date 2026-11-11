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

import ai.philterd.phileas.services.filters.regex.MbiFilter;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MBIGeneratorTest {

    @Test
    public void testGenerateMBI() {
        final MBIGenerator generator = new MBIGenerator(new SecureRandom());
        for (int i = 0; i < 1000; i++) {
            final String mbi = generator.random();
            assertNotNull(mbi, "MBI should not be null");
            assertEquals(11, mbi.length(), "MBI should have 11 characters but was " + mbi);
            assertTrue(MbiFilter.isValid(mbi), "MBI should be in the CMS format but was " + mbi);
            assertTrue(mbi.chars().noneMatch(c -> "SLOIBZ".indexOf(c) >= 0), "MBI should not use S, L, O, I, B, or Z but was " + mbi);
        }
    }

    @Test
    public void testPoolSize() {
        final MBIGenerator generator = new MBIGenerator(new SecureRandom());
        // 1-9, then letters (20) or letters and digits (30) or digits (10) by position.
        assertEquals(9L * 20 * 30 * 10 * 20 * 30 * 10 * 20 * 20 * 10 * 10, generator.poolSize());
    }

}
