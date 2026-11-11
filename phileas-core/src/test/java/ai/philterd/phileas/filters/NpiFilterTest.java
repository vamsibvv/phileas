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
import ai.philterd.phileas.services.anonymization.NpiAnonymizationService;
import ai.philterd.phileas.services.filters.regex.NpiFilter;
import ai.philterd.phileas.services.strategies.AbstractFilterStrategy;
import ai.philterd.phileas.services.strategies.rules.NpiFilterStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

// All numbers in these tests are invented, except 1234567893, the example in the CMS check digit document.
public class NpiFilterTest extends AbstractFilterTest {

    private NpiFilter filter(final AbstractFilterStrategy strategy) {

        final FilterConfiguration filterConfiguration = new FilterConfiguration.FilterConfigurationBuilder()
                .withStrategies(List.of(strategy))
                .withWindowSize(windowSize)
                .build();

        return new NpiFilter(filterConfiguration);

    }

    private NpiFilter filter() {
        return filter(new NpiFilterStrategy());
    }

    @Test
    public void filterNpi() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the npi is 1234567893.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 11, 21, FilterType.NPI));
        Assertions.assertEquals("1234567893", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterNpiBeginningWith2() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "NPI: 2234567891");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 5, 15, FilterType.NPI));

    }

    @Test
    public void filterNpiInvalidCheckDigit() throws Exception {

        final NpiFilter filter = filter();

        for (final String npi : List.of("1234567890", "1234567894", "2234567890")) {
            Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the npi is " + npi + ".").getSpans().size(), npi);
        }

    }

    @Test
    public void filterNpiCheckDigitUsesTheCardIssuerPrefix() throws Exception {

        // 1234567897 passes the plain Luhn check, but not the NPI check, which includes the 80840 prefix.
        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the npi is 1234567897.");
        Assertions.assertEquals(0, filtered.getSpans().size());

    }

    @Test
    public void filterNpiOnlyBeginningWith1Or2() throws Exception {

        final NpiFilter filter = filter();

        // These pass the NPI check digit but begin with a digit CMS does not issue NPIs with.
        for (final String npi : List.of("3123456782", "9123456789")) {
            Assertions.assertTrue(NpiFilter.isValid(npi), npi);
            Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the number is " + npi + ".").getSpans().size(), npi);
        }

    }

    @Test
    public void filterNpiNotWithinLongerRun() throws Exception {

        final NpiFilter filter = filter();

        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "account 11234567893 closed.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "account 12345678931 closed.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is A1234567893.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is 1234567893B.").getSpans().size());

    }

    @Test
    public void filterNpiNotWithSeparators() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the number is 123-456-7893.");
        Assertions.assertEquals(0, filtered.getSpans().size());

    }

    @Test
    public void filterNpiLastFour() throws Exception {

        final NpiFilterStrategy strategy = new NpiFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.LAST_4);

        final Filtered filtered = filter(strategy).filter(contextService, getPolicy(), "context", PIECE, "the npi is 1234567893.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("7893", filtered.getSpans().get(0).getReplacement());

    }

    @Test
    public void filterNpiMask() throws Exception {

        final NpiFilterStrategy strategy = new NpiFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.MASK);

        final Filtered filtered = filter(strategy).filter(contextService, getPolicy(), "context", PIECE, "the npi is 1234567893.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("**********", filtered.getSpans().get(0).getReplacement());

    }

    @Test
    public void filterNpiUsesNpiAnonymization() throws Exception {

        final NpiFilterStrategy strategy = new NpiFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.RANDOM_REPLACE);

        filter(strategy);

        // RANDOM_REPLACE on an NPI draws from the NPI anonymization service, so a realistic
        // replacement is a valid NPI. See NpiFilterStrategyTest.
        Assertions.assertInstanceOf(NpiAnonymizationService.class, strategy.getAnonymizationService());

    }

    @Test
    public void isValid() {

        Assertions.assertTrue(NpiFilter.isValid("1234567893"));
        Assertions.assertTrue(NpiFilter.isValid("2234567891"));
        Assertions.assertFalse(NpiFilter.isValid("1234567890"));
        Assertions.assertFalse(NpiFilter.isValid("1234567897"));
        Assertions.assertFalse(NpiFilter.isValid("123456789"));

    }

}
