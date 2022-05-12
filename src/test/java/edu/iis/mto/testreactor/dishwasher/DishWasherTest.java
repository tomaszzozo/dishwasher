package edu.iis.mto.testreactor.dishwasher;

import edu.iis.mto.testreactor.dishwasher.engine.Engine;
import edu.iis.mto.testreactor.dishwasher.engine.EngineException;
import edu.iis.mto.testreactor.dishwasher.pump.PumpException;
import edu.iis.mto.testreactor.dishwasher.pump.WaterPump;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishWasherTest {

    @Mock WaterPump waterPump;
    @Mock Engine engine;
    @Mock DirtFilter dirtFilter;
    @Mock Door door;

    private DishWasher dishWasher;
    private final FillLevel irrelevantFillLevel = FillLevel.HALF;
    private final WashingProgram irrelevantProgram = WashingProgram.ECO;
    private final ProgramConfiguration irrelevantConfig = ProgramConfiguration.builder()
            .withTabletsUsed(true)
            .withProgram(irrelevantProgram)
            .withFillLevel(irrelevantFillLevel)
            .build();

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

    @Test
    void startNullParameter() {
        NullPointerException result = assertThrows(NullPointerException.class, () -> dishWasher.start(null));
        assertEquals("program == null", result.getMessage());
    }

    @Test
    void doorOpen() {
        when(door.closed()).thenReturn(false);
        RunResult result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.DOOR_OPEN, result.getStatus());
        assertEquals(0, result.getRunMinutes());
    }

    @Test
    void dirtyFilter() {
        when(dirtFilter.capacity()).thenReturn(DishWasher.MAXIMAL_FILTER_CAPACITY-0.0001);
        when(door.closed()).thenReturn(true);

        RunResult result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.ERROR_FILTER, result.getStatus());
        assertEquals(0, result.getRunMinutes());
    }

    @Test
    void dirtFilterWithCapacityEqualToMax() {
        when(dirtFilter.capacity()).thenReturn(DishWasher.MAXIMAL_FILTER_CAPACITY);
        when(door.closed()).thenReturn(true);

        RunResult result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.ERROR_FILTER, result.getStatus());
        assertEquals(0, result.getRunMinutes());
    }

    @Test
    void noWashingTabletsUsed() {
        when(door.closed()).thenReturn(true);

        ProgramConfiguration noTabletsProgram = ProgramConfiguration.builder()
                .withProgram(irrelevantProgram)
                .withTabletsUsed(false)
                .withFillLevel(irrelevantFillLevel)
                .build();
        RunResult result = dishWasher.start(noTabletsProgram);
        assertEquals(Status.SUCCESS, result.getStatus());
    }

    @Test
    void engineException() throws EngineException {
        when(door.closed()).thenReturn(true);
        when(dirtFilter.capacity()).thenReturn(DishWasher.MAXIMAL_FILTER_CAPACITY+1);

        doThrow(EngineException.class).when(engine).runProgram(anyList());
        RunResult result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.ERROR_PROGRAM, result.getStatus());
    }

    @Test
    void pumpException() throws PumpException {
        when(door.closed()).thenReturn(true);
        when(dirtFilter.capacity()).thenReturn(DishWasher.MAXIMAL_FILTER_CAPACITY+1);

        doThrow(PumpException.class).when(waterPump).pour(any(FillLevel.class));
        RunResult result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.ERROR_PUMP, result.getStatus());

        doNothing().when(waterPump).pour(any(FillLevel.class));
        doThrow(PumpException.class).when(waterPump).drain();
        result = dishWasher.start(irrelevantConfig);
        assertEquals(Status.ERROR_PUMP, result.getStatus());
    }
}
