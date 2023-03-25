package org.tbk.ldk.spring.test;

import org.junit.jupiter.api.Test;
import org.ldk.enums.ConfirmationTarget;
import org.ldk.structs.FeeEstimator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SampleTest {

    @Test
    void itShouldImplementFeeEstimatorExample() {
        FeeEstimator feeEstimator = FeeEstimator.new_impl(new YourFeeEstimator());

        int estimatedFees = feeEstimator.get_est_sat_per_1000_weight(ConfirmationTarget.LDKConfirmationTarget_Normal);
        assertThat(estimatedFees, is(253));

    }

    private static class YourFeeEstimator implements FeeEstimator.FeeEstimatorInterface {
        private static final int MIN_FEES = 253;

        @Override
        public int get_est_sat_per_1000_weight(ConfirmationTarget confTarget) {
            if (confTarget == ConfirmationTarget.LDKConfirmationTarget_Background) {
                // <insert code to retrieve a background feerate>
            } else if (confTarget ==
                    ConfirmationTarget.LDKConfirmationTarget_Normal) {
                // <insert code to retrieve a normal (i.e. within ~6 blocks) feerate>
            } else if (confTarget ==
                    ConfirmationTarget.LDKConfirmationTarget_HighPriority) {
                // <insert code to retrieve a high-priority feerate>
            }
            return MIN_FEES;
        }
    }


}