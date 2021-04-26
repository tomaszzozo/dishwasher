package edu.iis.mto.testreactor.dishwasher.engine;

import java.util.List;

public interface Engine {

    void runProgram(List<Integer> codes) throws EngineException;

}
