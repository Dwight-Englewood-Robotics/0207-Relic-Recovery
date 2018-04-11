package org.firstinspires.ftc.teamcode.Utility;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class GlyphClamps {

    private Servo front, back;
    private HardwareMap hwMap;

    public GlyphClamps(HardwareMap hwMap) {
        this.hwMap = hwMap;
        this.init();
    }

    private void init(){
        back = this.hwMap.servo.get("clampb");
        front = this.hwMap.servo.get("clampf");
        back.scaleRange(.1,.9);
        front.scaleRange(.1,.9);
    }

    public void clampBack(boolean clamp) {
        back.setPosition(clamp ? 1 : 0);
    }

    public void clampFront(boolean clamp) {
        front.setPosition(clamp ? 1 : 0);
    }

}