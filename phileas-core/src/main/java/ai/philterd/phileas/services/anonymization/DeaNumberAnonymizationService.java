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
package ai.philterd.phileas.services.anonymization;

import ai.philterd.phileas.data.generators.DEANumberGenerator;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Replaces a DEA number with a random DEA number that has a valid check digit. The first letter,
 * which gives the registrant type, is kept. The second character is replaced with a random letter,
 * or kept when it is {@code 9}. The letters follow the case of the original.
 */
public class DeaNumberAnonymizationService extends AbstractAnonymizationService {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public DeaNumberAnonymizationService(final SecureRandom random, final AnonymizationMethod anonymizationMethod) {
        super(random, anonymizationMethod);
    }

    public DeaNumberAnonymizationService(final SecureRandom random, final List<String> candidates) {
        super(random, candidates);
    }

    public DeaNumberAnonymizationService(final SecureRandom random) {
        super(random);
    }

    public DeaNumberAnonymizationService() {
        super();
    }

    @Override
    public String anonymize(final String token) {

        if (anonymizationMethod == AnonymizationMethod.FROM_LIST) {

            return anonymizeFromList(token);

        } else if (anonymizationMethod == AnonymizationMethod.UUID) {

            return UUID.randomUUID().toString();

        } else {

            // REALISTIC_REPLACE
            return anonymizeRealistic(token, () -> getAnonymizedDeaNumber(token));

        }

    }

    private String getAnonymizedDeaNumber(final String token) {

        final DEANumberGenerator generator = new DEANumberGenerator(random);

        if (token.length() != 9 || !Character.isLetter(token.charAt(0))) {
            return generator.random();
        }

        final char first = token.charAt(0);
        final char second = token.charAt(1) == '9' ? '9' : LETTERS.charAt(random.nextInt(LETTERS.length()));

        final String anonymized = generator.random(Character.toUpperCase(first), second);

        return matchCase(token, anonymized);

    }

}
