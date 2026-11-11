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
 * Generates random National Provider Identifiers (NPI): ten digits beginning with 1 or 2, the last a
 * check digit, such as {@code 1234567893}.
 */
public class NPIGenerator implements DataGenerator.Generator<String> {

    private final SecureRandom random;

    /**
     * Creates a new NPI generator.
     * @param random The {@link SecureRandom} to use.
     */
    public NPIGenerator(final SecureRandom random) {
        this.random = random;
    }

    @Override
    public String random() {

        final int[] digits = new int[10];
        digits[0] = 1 + random.nextInt(2);

        for (int i = 1; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }

        digits[9] = checkDigit(digits);

        final StringBuilder sb = new StringBuilder();
        for (final int digit : digits) {
            sb.append(digit);
        }

        return sb.toString();

    }

    @Override
    public long poolSize() {
        // The first digit is 1 or 2 and the check digit is determined by the other nine.
        return 2L * 100000000L;
    }

    /**
     * The NPI check digit for the first nine digits: the Luhn check digit of the number with the
     * {@code 80840} prefix, which adds the constant 24 to the sum (CMS, "Requirements for National
     * Provider Identifier (NPI) and NPI Check Digit").
     * @param digits The digits. Only the first nine are read.
     * @return The check digit.
     */
    public static int checkDigit(final int[] digits) {

        int sum = 24;

        for (int i = 0; i < 9; i++) {
            int digit = digits[i];
            // Counting from the check digit, every second digit is doubled: positions 0, 2, 4, 6 and 8.
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (10 - (sum % 10)) % 10;

    }

}
