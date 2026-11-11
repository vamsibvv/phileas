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
package ai.philterd.phileas.services.filters.regex;

import ai.philterd.phileas.data.generators.DEANumberGenerator;
import ai.philterd.phileas.filters.FilterConfiguration;
import ai.philterd.phileas.filters.rules.regex.RegexFilter;
import ai.philterd.phileas.model.filtering.FilterPattern;
import ai.philterd.phileas.model.filtering.FilterType;
import ai.philterd.phileas.model.filtering.Filtered;
import ai.philterd.phileas.model.filtering.Span;
import ai.philterd.phileas.policy.Policy;
import ai.philterd.phileas.services.Analyzer;
import ai.philterd.phileas.services.context.ContextService;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects DEA registration numbers: two letters followed by seven digits, the last a check digit,
 * such as {@code AB1234563}. The second character may also be {@code 9}, as it is for a registrant
 * identified by a business name. Every match must pass the check digit.
 *
 * <p>A supervised prescriber's number may have a suffix after a hyphen, as in
 * {@code AB1234563-012ABC}. Only the registration number before the hyphen is detected.</p>
 */
public class DeaNumberFilter extends RegexFilter {

    public DeaNumberFilter(final FilterConfiguration filterConfiguration) {
        super(FilterType.DEA_NUMBER, filterConfiguration);

        // A match may not begin or end partway through a longer run of digits or letters.
        final Pattern deaPattern = Pattern.compile("(?<!\\w)[A-Z][A-Z9]\\d{7}(?!\\w)", Pattern.CASE_INSENSITIVE);
        final FilterPattern dea1 = new FilterPattern.FilterPatternBuilder(deaPattern, 0.90).build();

        this.contextualTerms = new HashSet<>();
        this.contextualTerms.add("dea");
        this.contextualTerms.add("registration");
        this.contextualTerms.add("prescriber");

        this.analyzer = new Analyzer(contextualTerms, dea1);

    }

    @Override
    public Filtered filter(ContextService contextService, Policy policy, String context, int piece, String input) throws Exception {

        final List<Span> spans = findSpans(contextService, policy, analyzer, input, context);

        spans.removeIf(span -> !isValid(span.getText()));

        return new Filtered(context, spans);

    }

    /**
     * Whether the text is two characters followed by seven digits whose last digit is the DEA check
     * digit of the other six.
     * @param text The matched text.
     * @return {@code true} if the check digit is valid.
     */
    public static boolean isValid(final String text) {

        if (text.length() != 9) {
            return false;
        }

        final int[] digits = new int[7];

        for (int i = 0; i < 7; i++) {
            final char c = text.charAt(i + 2);
            if (c < '0' || c > '9') {
                return false;
            }
            digits[i] = c - '0';
        }

        return DEANumberGenerator.checkDigit(digits) == digits[6];

    }

}
