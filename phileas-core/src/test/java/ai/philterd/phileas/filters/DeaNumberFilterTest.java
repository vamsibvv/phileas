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
package ai.philterd.phileas.filters;

import ai.philterd.phileas.model.filtering.FilterType;
import ai.philterd.phileas.model.filtering.Filtered;
import ai.philterd.phileas.services.anonymization.DeaNumberAnonymizationService;
import ai.philterd.phileas.services.filters.regex.DeaNumberFilter;
import ai.philterd.phileas.services.strategies.AbstractFilterStrategy;
import ai.philterd.phileas.services.strategies.rules.DeaNumberFilterStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

// All numbers in these tests are invented.
public class DeaNumberFilterTest extends AbstractFilterTest {

    private DeaNumberFilter filter(final AbstractFilterStrategy strategy) {

        final FilterConfiguration filterConfiguration = new FilterConfiguration.FilterConfigurationBuilder()
                .withStrategies(List.of(strategy))
                .withWindowSize(windowSize)
                .build();

        return new DeaNumberFilter(filterConfiguration);

    }

    private DeaNumberFilter filter() {
        return filter(new DeaNumberFilterStrategy());
    }

    @Test
    public void filterDeaNumber() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the dea number is AB1234563.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 18, 27, FilterType.DEA_NUMBER));
        Assertions.assertEquals("AB1234563", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterDeaNumbers() throws Exception {

        final DeaNumberFilter filter = filter();

        // Other letters, a 9 as the second character for a registrant named by a business, and lowercase.
        for (final String dea : List.of("BJ7654329", "MX2468139", "F91234563", "ab1234563")) {
            final Filtered filtered = filter.filter(contextService, getPolicy(), "context", PIECE, "DEA# " + dea + " on file");
            Assertions.assertEquals(1, filtered.getSpans().size(), dea);
            Assertions.assertEquals(dea, filtered.getSpans().get(0).getText());
        }

    }

    @Test
    public void filterDeaNumberInvalidCheckDigit() throws Exception {

        final DeaNumberFilter filter = filter();

        for (final String dea : List.of("AB1234562", "AB1234564", "BJ7654321")) {
            Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the dea number is " + dea + ".").getSpans().size(), dea);
        }

    }

    @Test
    public void filterDeaNumberWrongShape() throws Exception {

        final DeaNumberFilter filter = filter();

        // A digit first, a digit other than 9 second, and too few or too many digits.
        for (final String text : List.of("1B1234563", "A11234563", "AB123456", "AB12345634")) {
            Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is " + text + ".").getSpans().size(), text);
        }

    }

    @Test
    public void filterDeaNumberNotWithinLongerRun() throws Exception {

        final DeaNumberFilter filter = filter();

        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is XAB1234563.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is AB1234563X.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is 7AB1234563.").getSpans().size());

    }

    @Test
    public void filterDeaNumberWithSupervisedPrescriberSuffix() throws Exception {

        // Only the registration number before the hyphen is detected.
        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "DEA AB1234563-012ABC");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 4, 13, FilterType.DEA_NUMBER));

    }

    @Test
    public void filterDeaNumberLastFour() throws Exception {

        final DeaNumberFilterStrategy strategy = new DeaNumberFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.LAST_4);

        final Filtered filtered = filter(strategy).filter(contextService, getPolicy(), "context", PIECE, "the dea number is AB1234563.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("4563", filtered.getSpans().get(0).getReplacement());

    }

    @Test
    public void filterDeaNumberUsesDeaNumberAnonymization() throws Exception {

        final DeaNumberFilterStrategy strategy = new DeaNumberFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.RANDOM_REPLACE);

        filter(strategy);

        // RANDOM_REPLACE on a DEA number draws from the DEA number anonymization service, so a
        // realistic replacement is a valid DEA number. See DeaNumberFilterStrategyTest.
        Assertions.assertInstanceOf(DeaNumberAnonymizationService.class, strategy.getAnonymizationService());

    }

    @Test
    public void isValid() {

        Assertions.assertTrue(DeaNumberFilter.isValid("AB1234563"));
        Assertions.assertTrue(DeaNumberFilter.isValid("F91234563"));
        Assertions.assertFalse(DeaNumberFilter.isValid("AB1234562"));
        Assertions.assertFalse(DeaNumberFilter.isValid("AB123456"));
        Assertions.assertFalse(DeaNumberFilter.isValid("AB12345A3"));

    }

}
