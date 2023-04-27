package com.example.andodatasetcollectionsystem;

import com.github.pires.obd.commands.PercentageObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

public class ThrottlePositionCommand2 extends PercentageObdCommand {

    public ThrottlePositionCommand2() {
        super("01 11 1");
    }

    @Override
    public String getName() {
        return AvailableCommandNames.THROTTLE_POS.getValue();
    }
}
