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
package ai.philterd.phileas.services.strategies.rules;

import ai.philterd.phileas.model.filtering.Replacement;
import ai.philterd.phileas.policy.Crypto;
import ai.philterd.phileas.policy.FPE;
import ai.philterd.phileas.services.anonymization.AbstractAnonymizationService;
import ai.philterd.phileas.services.anonymization.AnonymizationService;
import ai.philterd.phileas.services.anonymization.DeaNumberAnonymizationService;
import ai.philterd.phileas.services.context.ContextService;
import ai.philterd.phileas.services.context.DefaultContextService;
import ai.philterd.phileas.services.filters.regex.DeaNumberFilter;
import ai.philterd.phileas.services.strategies.AbstractFilterStrategy;
import ai.philterd.phileas.services.strategies.AbstractFilterStrategyTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeaNumberFilterStrategyTest extends AbstractFilterStrategyTest {

    public AbstractFilterStrategy getFilterStrategy() {
        return new DeaNumberFilterStrategy();
    }

    public AbstractAnonymizationService getAnonymizationService() {
        return new DeaNumberAnonymizationService();
    }

    @Test
    public void lastFour() throws Exception {

        final AnonymizationService anonymizationService = getAnonymizationService();
        final ContextService contextService = new DefaultContextService();

        final AbstractFilterStrategy strategy = getFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.LAST_4);

        final Replacement replacement = strategy.getReplacement(contextService, "name", "context", "AB1234563", WINDOW, new Crypto(), new FPE(), anonymizationService, null);

        Assertions.assertEquals("4563", replacement.getReplacement());

    }

    @Test
    public void randomReplaceIsValidDeaNumber() throws Exception {

        final AnonymizationService anonymizationService = getAnonymizationService();
        final ContextService contextService = new DefaultContextService();

        final AbstractFilterStrategy strategy = getFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.RANDOM_REPLACE);

        // The first letter is kept, a 9 in the second position is kept, and the case follows the original.
        final String[][] cases = {{"AB1234563", "A[A-Z]\\d{7}"}, {"F91234563", "F9\\d{7}"}, {"ab1234563", "a[a-z]\\d{7}"}};

        for (final String[] c : cases) {
            for (int i = 0; i < 50; i++) {

                final String replacement = strategy.getReplacement(contextService, "name", "context" + i, c[0], WINDOW, new Crypto(), new FPE(), anonymizationService, null).getReplacement();

                Assertions.assertNotEquals(c[0], replacement);
                Assertions.assertTrue(replacement.matches(c[1]), replacement);
                Assertions.assertTrue(DeaNumberFilter.isValid(replacement), replacement);

            }
        }

    }

}
