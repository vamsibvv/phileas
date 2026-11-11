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

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects Medicare Beneficiary Identifiers (MBI) in the format CMS publishes ("Understanding the
 * Medicare Beneficiary Identifier (MBI) Format"): eleven characters, where the first is a digit
 * from 1 to 9, the 2nd, 5th, 8th, and 9th are letters, the 3rd and 6th are a letter or a digit,
 * and the rest are digits, such as {@code 1EG4TE5MK73}. The letters S, L, O, I, B, and Z are never
 * used. The MBI has no check digit, so the format is its only validation.
 *
 * <p>An MBI may be written in three groups (4, 3, and 4 characters) separated by hyphens, as on the
 * Medicare card ({@code 1EG4-TE5-MK73}), or by single spaces, with the same separators and line-wrap
 * handling as the SSN filter. Lowercase letters are accepted, since CMS converts them to
 * uppercase.</p>
 */
public class MbiFilter extends RegexFilter {

    /** A letter an MBI uses: A to Z without S, L, O, I, B, and Z. */
    private static final String LETTER = "[AC-HJKMNP-RT-Y]";

    /** A letter an MBI uses, or a digit. */
    private static final String LETTER_OR_DIGIT = "[AC-HJKMNP-RT-Y0-9]";

    private static final Pattern MBI_FORMAT = Pattern.compile("[1-9]" + LETTER + LETTER_OR_DIGIT + "[0-9]"
            + LETTER + LETTER_OR_DIGIT + "[0-9]" + LETTER + LETTER + "[0-9]{2}", Pattern.CASE_INSENSITIVE);

    public MbiFilter(final FilterConfiguration filterConfiguration) {
        super(FilterType.MBI, filterConfiguration);

        // The card's grouping, 1EG4-TE5-MK73, with the same separators as the SSN filter: a hyphen
        // (or a substitute for one), optionally wrapped across a line break after it, one horizontal
        // space, or nothing, the same for both. A match may not begin or end partway through a
        // longer run of digits or letters.
        final String first = "[1-9]" + LETTER + LETTER_OR_DIGIT + "[0-9]";
        final String second = LETTER + LETTER_OR_DIGIT + "[0-9]";
        final String third = LETTER + LETTER + "[0-9]{2}";

        final Pattern mbiPattern = Pattern.compile("(?<!\\w)" + IdentifierSeparators.join(first, second, third) + "(?!\\w)", Pattern.CASE_INSENSITIVE);
        final FilterPattern mbi1 = new FilterPattern.FilterPatternBuilder(mbiPattern, 0.90).build();

        this.contextualTerms = new HashSet<>();
        this.contextualTerms.add("mbi");
        this.contextualTerms.add("medicare");
        this.contextualTerms.add("beneficiary");

        this.analyzer = new Analyzer(contextualTerms, mbi1);

    }

    @Override
    public Filtered filter(ContextService contextService, Policy policy, String context, int piece, String input) throws Exception {

        final List<Span> spans = findSpans(contextService, policy, analyzer, input, context);

        return new Filtered(context, spans);

    }

    /**
     * Whether the text, without separators, is in the MBI format.
     * @param text The text, in any accepted form.
     * @return {@code true} if its letters and digits form an MBI.
     */
    public static boolean isValid(final String text) {

        final String characters = text.replaceAll("[^A-Za-z0-9]", "");

        return MBI_FORMAT.matcher(characters).matches();

    }

}
