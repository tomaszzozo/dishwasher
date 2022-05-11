package edu.iis.mto.testreactor.dishwasher;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DishWasherTest {

    @Mock WaterPump waterPump;
    @Mock Engine engine;
    @Mock DirtFilter dirtFilter;
    @Mock Door door;

    private DishWasher dishWasher;

    @BeforeEach
    void setUp() {
        dishWasher = new DishWasher(waterPump, engine, dirtFilter, door);
    }

    @Test
    void constructorNullParameters() {
        NullPointerException waterPumpException = assertThrows(NullPointerException.class,
                () -> new DishWasher(null, engine, dirtFilter, door));
        NullPointerException engineException = assertThrows(NullPointerException.class,
                () -> new DishWasher(waterPump, null, dirtFilter, door));
        NullPointerException dirtFilterException = assertThrows(NullPointerException.class,
                () -> new DishWasher(waterPump, engine, null, door));
        NullPointerException doorException = assertThrows(NullPointerException.class,
                () -> new DishWasher(waterPump, engine, dirtFilter, null));

        assertEquals("waterPump = null", waterPumpException.getMessage());
        assertEquals("engine == null", engineException.getMessage());
        assertEquals("dirtFilter == null", dirtFilterException.getMessage());
        assertEquals("door == null", doorException.getMessage());
    }
}
