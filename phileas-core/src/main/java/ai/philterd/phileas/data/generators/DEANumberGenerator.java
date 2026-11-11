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

import ai.philterd.phileas.data.DataGenerator;

import java.security.SecureRandom;

/**
 * Generates random DEA registration numbers: a registrant type letter, a second letter, and seven
 * digits, the last a check digit, such as {@code AB1234563}.
 */
public class DEANumberGenerator implements DataGenerator.Generator<String> {

    /** Letters used as the first letter (the registrant type) of DEA numbers. */
    private static final String REGISTRANT_TYPES = "ABCDEFGHJKLMPRSTUX";

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final SecureRandom random;

    /**
     * Creates a new DEA number generator.
     * @param random The {@link SecureRandom} to use.
     */
    public DEANumberGenerator(final SecureRandom random) {
        this.random = random;
    }

    @Override
    public String random() {
        return random(REGISTRANT_TYPES.charAt(random.nextInt(REGISTRANT_TYPES.length())),
                LETTERS.charAt(random.nextInt(LETTERS.length())));
    }

    /**
     * Generates a random DEA number with the given letters and random digits.
     * @param first The first letter.
     * @param second The second character, a letter or {@code 9}.
     * @return A DEA number with a valid check digit.
     */
    public String random(final char first, final char second) {

        final int[] digits = new int[7];

        for (int i = 0; i < 6; i++) {
            digits[i] = random.nextInt(10);
        }

        digits[6] = checkDigit(digits);

        final StringBuilder sb = new StringBuilder();
        sb.append(first).append(second);
        for (final int digit : digits) {
            sb.append(digit);
        }

        return sb.toString();

    }

    @Override
    public long poolSize() {
        // The check digit is determined by the other six digits.
        return (long) REGISTRANT_TYPES.length() * LETTERS.length() * 1000000L;
    }

    /**
     * The DEA check digit for the first six digits: the last digit of the sum of the first, third,
     * and fifth digits plus twice the sum of the second, fourth, and sixth digits.
     * @param digits The digits. Only the first six are read.
     * @return The check digit.
     */
    public static int checkDigit(final int[] digits) {

        final int sum = (digits[0] + digits[2] + digits[4]) + 2 * (digits[1] + digits[3] + digits[5]);

        return sum % 10;

    }

}
