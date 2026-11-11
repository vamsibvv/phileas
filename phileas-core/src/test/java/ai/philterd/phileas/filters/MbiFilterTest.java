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
import ai.philterd.phileas.services.anonymization.MbiAnonymizationService;
import ai.philterd.phileas.services.filters.regex.MbiFilter;
import ai.philterd.phileas.services.strategies.AbstractFilterStrategy;
import ai.philterd.phileas.services.strategies.rules.MbiFilterStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

// All identifiers in these tests are invented, except 1EG4-TE5-MK73, the example in the CMS MBI format document.
public class MbiFilterTest extends AbstractFilterTest {

    private MbiFilter filter(final AbstractFilterStrategy strategy) {

        final FilterConfiguration filterConfiguration = new FilterConfiguration.FilterConfigurationBuilder()
                .withStrategies(List.of(strategy))
                .withWindowSize(windowSize)
                .build();

        return new MbiFilter(filterConfiguration);

    }

    private MbiFilter filter() {
        return filter(new MbiFilterStrategy());
    }

    @Test
    public void filterMbiUnformatted() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4TE5MK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 11, 22, FilterType.MBI));
        Assertions.assertEquals("1EG4TE5MK73", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterMbiHyphenated() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4-TE5-MK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 11, 24, FilterType.MBI));
        Assertions.assertEquals("1EG4-TE5-MK73", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterMbiSpaceSeparated() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4 TE5 MK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("1EG4 TE5 MK73", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterMbiUnicodeHyphen() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4‑TE5‑MK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertTrue(checkSpan(filtered.getSpans().get(0), 11, 24, FilterType.MBI));

    }

    @Test
    public void filterMbiWrappedAfterHyphen() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4-TE5-\nMK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("1EG4-TE5-\nMK73", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterMbiLowercase() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1eg4te5mk73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("1eg4te5mk73", filtered.getSpans().get(0).getText());

    }

    @Test
    public void filterMbiOtherIdentifiers() throws Exception {

        final MbiFilter filter = filter();

        // Digits in the 3rd and 6th positions, and the highest allowed characters in each position.
        for (final String mbi : List.of("2C04D06EF89", "9YY9YY9YY99", "5AA0AA0AA00")) {
            final Filtered filtered = filter.filter(contextService, getPolicy(), "context", PIECE, "medicare " + mbi + " active");
            Assertions.assertEquals(1, filtered.getSpans().size(), mbi);
            Assertions.assertEquals(mbi, filtered.getSpans().get(0).getText());
        }

    }

    @Test
    public void filterMbiNotInFormat() throws Exception {

        final MbiFilter filter = filter();

        final List<String> notMbis = List.of(
                "0EG4TE5MK73",   // the first character is 0
                "1BG4TE5MK73",   // B is never used
                "1EG4TS5MK73",   // S is never used
                "1EG4TE5ML73",   // L is never used
                "1EG4TE5OK73",   // O is never used
                "1IG4TE5MK73",   // I is never used
                "1EG4TE5MZ73",   // Z is never used
                "1EGATE5MK73",   // the 4th character is a letter
                "1EG4TE5MK7A",   // the 11th character is a letter
                "1EG4TE5M473",   // the 9th character is a digit
                "1EG4TE5MK7",    // ten characters
                "1EG4-TE5 MK73", // mixed separators
                "1EG4-TE5MK73"); // only one separator

        for (final String text : notMbis) {
            Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is " + text + ".").getSpans().size(), text);
        }

    }

    @Test
    public void filterMbiNotWithinLongerRun() throws Exception {

        final MbiFilter filter = filter();

        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is 11EG4TE5MK73.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is 1EG4TE5MK734.").getSpans().size());
        Assertions.assertEquals(0, filter.filter(contextService, getPolicy(), "context", PIECE, "the code is X1EG4TE5MK73.").getSpans().size());

    }

    @Test
    public void filterMbiNotJoinedAcrossLinesWithoutHyphen() throws Exception {

        final Filtered filtered = filter().filter(contextService, getPolicy(), "context", PIECE, "1EG4\nTE5\nMK73");
        Assertions.assertEquals(0, filtered.getSpans().size());

    }

    @Test
    public void filterMbiLastFour() throws Exception {

        final MbiFilterStrategy strategy = new MbiFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.LAST_4);

        final Filtered filtered = filter(strategy).filter(contextService, getPolicy(), "context", PIECE, "the mbi is 1EG4TE5MK73.");
        Assertions.assertEquals(1, filtered.getSpans().size());
        Assertions.assertEquals("MK73", filtered.getSpans().get(0).getReplacement());

    }

    @Test
    public void filterMbiUsesMbiAnonymization() throws Exception {

        final MbiFilterStrategy strategy = new MbiFilterStrategy();
        strategy.setStrategy(AbstractFilterStrategy.RANDOM_REPLACE);

        filter(strategy);

        // RANDOM_REPLACE on an MBI draws from the MBI anonymization service, so a realistic
        // replacement is in the MBI format. See MbiFilterStrategyTest.
        Assertions.assertInstanceOf(MbiAnonymizationService.class, strategy.getAnonymizationService());

    }

    @Test
    public void isValid() {

        Assertions.assertTrue(MbiFilter.isValid("1EG4TE5MK73"));
        Assertions.assertTrue(MbiFilter.isValid("1EG4-TE5-MK73"));
        Assertions.assertTrue(MbiFilter.isValid("1eg4te5mk73"));
        Assertions.assertFalse(MbiFilter.isValid("1BG4TE5MK73"));
        Assertions.assertFalse(MbiFilter.isValid("1EG4TE5MK7"));

    }

}
