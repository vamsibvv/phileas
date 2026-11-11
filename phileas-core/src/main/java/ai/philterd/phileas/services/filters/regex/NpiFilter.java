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

import ai.philterd.phileas.filters.FilterConfiguration;
import ai.philterd.phileas.filters.rules.regex.RegexFilter;
import ai.philterd.phileas.model.filtering.FilterPattern;
import ai.philterd.phileas.model.filtering.FilterType;
import ai.philterd.phileas.model.filtering.Filtered;
import ai.philterd.phileas.model.filtering.Span;
import ai.philterd.phileas.policy.Policy;
import ai.philterd.phileas.services.Analyzer;
import ai.philterd.phileas.services.context.ContextService;
import ai.philterd.phileas.services.validators.LuhnValidator;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects National Provider Identifiers (NPI): ten digits beginning with 1 or 2, such as
 * {@code 1234567893}. Every match must pass the NPI check digit, which is the Luhn check digit of
 * the number with the {@code 80840} prefix (CMS, "Requirements for National Provider Identifier
 * (NPI) and NPI Check Digit"). CMS issues NPIs beginning with 1 or 2.
 */
public class NpiFilter extends RegexFilter {

    /** The card issuer prefix the NPI check digit is calculated with: 80 for health, 840 for the U.S. */
    private static final String CARD_ISSUER_PREFIX = "80840";

    public NpiFilter(final FilterConfiguration filterConfiguration) {
        super(FilterType.NPI, filterConfiguration);

        // A match may not begin or end partway through a longer run of digits or letters.
        final Pattern npiPattern = Pattern.compile("(?<!\\w)[12]\\d{9}(?!\\w)");
        final FilterPattern npi1 = new FilterPattern.FilterPatternBuilder(npiPattern, 0.90).build();

        this.contextualTerms = new HashSet<>();
        this.contextualTerms.add("npi");
        this.contextualTerms.add("provider");
        this.contextualTerms.add("prescriber");

        this.analyzer = new Analyzer(contextualTerms, npi1);

    }

    @Override
    public Filtered filter(ContextService contextService, Policy policy, String context, int piece, String input) throws Exception {

        final List<Span> spans = findSpans(contextService, policy, analyzer, input, context);

        spans.removeIf(span -> !isValid(span.getText()));

        return new Filtered(context, spans);

    }

    /**
     * Whether the text is ten digits that pass the NPI check digit.
     * @param text The matched text.
     * @return {@code true} if the text is ten digits and its check digit is valid.
     */
    public static boolean isValid(final String text) {

        final String digits = text.replaceAll("\\D", "");

        return digits.length() == 10 && LuhnValidator.isValid(CARD_ISSUER_PREFIX + digits);

    }

}
