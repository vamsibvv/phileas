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

import ai.philterd.phileas.data.generators.MBIGenerator;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Replaces an MBI with a random MBI in the CMS format, keeping the separators (hyphens, spaces, a
 * line break) of the original. The letters follow the case of the original.
 */
public class MbiAnonymizationService extends AbstractAnonymizationService {

    public MbiAnonymizationService(final SecureRandom random, final AnonymizationMethod anonymizationMethod) {
        super(random, anonymizationMethod);
    }

    public MbiAnonymizationService(final SecureRandom random, final List<String> candidates) {
        super(random, candidates);
    }

    public MbiAnonymizationService(final SecureRandom random) {
        super(random);
    }

    public MbiAnonymizationService() {
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
            return anonymizeRealistic(token, () -> getAnonymizedMbi(token));

        }

    }

    /**
     * A random MBI laid out like the token: each of the token's letters and digits is replaced in
     * order, and everything else is kept.
     */
    private String getAnonymizedMbi(final String token) {

        final String characters = matchCase(token, new MBIGenerator(random).random());

        if (token.chars().filter(Character::isLetterOrDigit).count() != characters.length()) {
            return characters;
        }

        final StringBuilder sb = new StringBuilder();
        int next = 0;

        for (int i = 0; i < token.length(); i++) {
            final char c = token.charAt(i);
            sb.append(Character.isLetterOrDigit(c) ? characters.charAt(next++) : c);
        }

        return sb.toString();

    }

}
