package com.example.factoryguard.application.port.out.ai;

import com.example.factoryguard.application.dto.ai.GenerateMemoryBankCommand;
import com.example.factoryguard.application.dto.ai.GenerateMemoryBankResult;

import java.util.concurrent.TimeoutException;

public interface GenerateMemoryBankPort {

    GenerateMemoryBankResult generateMemoryBank(GenerateMemoryBankCommand command) throws TimeoutException;
}
