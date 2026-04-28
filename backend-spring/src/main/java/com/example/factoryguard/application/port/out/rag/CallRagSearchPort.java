package com.example.factoryguard.application.port.out.rag;

import java.util.List;

public interface CallRagSearchPort {

    List<Long> searchChunkIds(Long organizationId, String query);
}
