package com.hitstudio.apps.netbook.domain.model;

import java.util.Objects;

public final class HouseholdService {
    private final String householdName;
    private final String host;
    private final int port;

    public HouseholdService(String householdName, String host, int port) {
        this.householdName = householdName;
        this.host = host;
        this.port = port;
    }

    public String getHouseholdName() {
        return householdName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HouseholdService that = (HouseholdService) o;
        return port == that.port && Objects.equals(householdName, that.householdName) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(householdName, host, port);
    }

    @Override
    public String toString() {
        return "HouseholdService{" +
                "householdName='" + householdName + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
