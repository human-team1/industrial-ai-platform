package com.example.factoryguard.application.port.out.file;

public interface CheckFileAccessPort {

    boolean canAccess(Long fileId, Long organizationId, boolean siteAdmin);
}
