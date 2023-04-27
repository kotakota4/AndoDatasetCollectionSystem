package com.example.andodatasetcollectionsystem;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

public class RPMCommand2 extends ObdCommand {

    private int rpm = -1;

    public RPMCommand2() {
        super("01 0C 1");
    }

    @Override
    protected void performCalculations() {
        rpm = (buffer.get(2) * 256 + buffer.get(3)) / 4;
    }

    @Override
    public String getFormattedResult() {
        return String.format("%d%s", rpm, getResultUnit());
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(rpm);
    }

    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_RPM.getValue();
    }

    public int getRPM(){
        return rpm;

    }
}
