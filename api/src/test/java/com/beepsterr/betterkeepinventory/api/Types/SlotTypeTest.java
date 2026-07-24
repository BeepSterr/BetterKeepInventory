package com.beepsterr.betterkeepinventory.api.Types;

import com.beepsterr.betterkeepinventory.api.Exceptions.TypeError;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure logic — SlotType maps slot preset names / numbers to slot ids. No server needed.
 */
class SlotTypeTest {

    private static List<Integer> slots(String... tokens) {
        return new SlotType(List.of(tokens)).getSlotIds();
    }

    private static List<Integer> range(int startInclusive, int endInclusive) {
        return IntStream.rangeClosed(startInclusive, endInclusive).boxed().collect(Collectors.toList());
    }

    @Test
    void armorPreset() {
        assertEquals(List.of(36, 37, 38, 39), slots("ARMOR"));
    }

    @Test
    void hotbarPreset() {
        assertEquals(range(0, 8), slots("HOTBAR"));
    }

    @Test
    void inventoryPreset() {
        assertEquals(range(9, 35), slots("INVENTORY"));
    }

    @Test
    void offhandPreset() {
        assertEquals(List.of(40), slots("OFFHAND"));
    }

    @Test
    void allAndStarCoverEverySlot() {
        assertEquals(range(0, 40), slots("ALL"));
        assertEquals(range(0, 40), slots("*"));
    }

    @Test
    void presetsAreCaseInsensitive() {
        assertEquals(List.of(36, 37, 38, 39), slots("armor"));
    }

    @Test
    void numericSlotsAreParsedAndTrimmed() {
        assertEquals(List.of(5), slots("5"));
        assertEquals(List.of(5), slots(" 5 "));
        assertEquals(List.of(0, 40), slots("0", "40"));
    }

    @Test
    void presetsAndNumbersCombine() {
        assertEquals(List.of(36, 37, 38, 39, 0), slots("ARMOR", "0"));
    }

    @Test
    void emptyInputDefaultsToEverySlot() {
        assertEquals(range(0, 40), new SlotType(List.of()).getSlotIds());
    }

    @Test
    void outOfRangeNumberIsIgnoredAndThenDefaultsToEverySlot() {
        // 41 is out of the valid 0..40 range, so it's dropped; with nothing left,
        // SlotType falls back to selecting all slots.
        assertEquals(range(0, 40), slots("41"));
    }

    @Test
    void invalidPresetThrows() {
        assertThrows(TypeError.class, () -> slots("BANANA"));
    }
}
