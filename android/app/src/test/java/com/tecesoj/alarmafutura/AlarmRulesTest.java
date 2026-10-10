package com.tecesoj.alarmafutura;

import org.junit.Test;
import static org.junit.Assert.*;

public class AlarmRulesTest {
    @Test
    public void trimsAlarmNames() {
        assertEquals("Viaje", AlarmRules.normalizedTitle("  Viaje  "));
        assertEquals("", AlarmRules.normalizedTitle(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyNameIsRejected() {
        AlarmRules.validate("   ", 2_000_000L, 1_000_000L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void pastDateIsRejected() {
        AlarmRules.validate("Viaje", 10L, 20L);
    }

    @Test
    public void validFutureDatePasses() {
        AlarmRules.validate("Viaje", 1_100_000L, 1_000_000L);
    }
}
