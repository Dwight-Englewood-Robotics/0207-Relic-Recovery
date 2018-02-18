package org.firstinspires.ftc.teamcode.Telebop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Utility.Bot;
import org.firstinspires.ftc.teamcode.Utility.EnumController;
import org.firstinspires.ftc.teamcode.Utility.MovementEnum;
import org.firstinspires.ftc.teamcode.Utility.ReleasePosition;

/**
 * Created by plotnw on 11/21/17.
 */

@TeleOp(name = "single user mode", group = "Teleop")
//@Disabled
public class TelebopOneController extends OpMode {
    Bot robot = new Bot();

    EnumController<ReleasePosition> controller;
    double liftScaledown = .7;
    double liftScaleup = .4;

    double relicArmPos1 = 1;
    double relicArmPos2 = 1;

    int cooldownServo1 = 0;
    int cooldownServo2 = 0;

    final int cooldown = 10;

    boolean brakeToggle = false;
    boolean glyphMode = true;
    boolean placing = false;
    int countdown = 0;
    int wallCountdown = 0;

    @Override
    public void init() {
        controller = new EnumController<>(ReleasePosition.MIDDLE);
        robot.init(hardwareMap);
        robot.setDriveMotorModes(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.colorSensor.enableLed(false);
        telemetry.addLine("Ready.");
        telemetry.update();
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        robot.jewelUp();
        robot.setDriveMotorModes(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void loop()
    {
        if (gamepad1.start && countdown <= 0) {
            glyphMode = !glyphMode;
            countdown = 30;
        }
        if (gamepad1.back && countdown <= 0) {
            brakeToggle = !brakeToggle;
            countdown = 30;
        }

        robot.tankDrive(gamepad1.left_stick_y, gamepad1.right_stick_y, gamepad1.left_trigger, gamepad1.right_trigger, false, brakeToggle);

        if (glyphMode) {

            if (gamepad1.y ) {
                placing = true;
            } else if (wallCountdown <= 0 ) {
                placing = false;
            }

            if (placing) {
                controller.addInstruction(ReleasePosition.UP, 5);
                robot.flipUp();
                robot.backIntakeWallDown();
            } else {
                controller.addInstruction(ReleasePosition.MIDDLE, 0);
                robot.backIntakeWallUp();
            }

            if (gamepad1.left_bumper) {
                controller.addInstruction(ReleasePosition.DOWN, 1);
                robot.intake(1);
            } else {
                if (gamepad1.right_bumper) {
                    controller.addInstruction(ReleasePosition.DOWN, 1);
                    robot.intake(-1);
                } else {
                    controller.addInstruction(ReleasePosition.MIDDLE, 0);
                    robot.intake(0);
                }
            }

            if (gamepad1.b) {
                robot.flipUp();
            } else if (!gamepad1.right_bumper) {
                robot.flipDown();
            }

            if (gamepad1.dpad_up) {
                controller.addInstruction(ReleasePosition.MIDDLEUP, 1);
                robot.lift.setPower(liftScaleup);
            } else if (gamepad1.dpad_down) {
                controller.addInstruction(ReleasePosition.MIDDLEUP, 1);
                robot.lift.setPower(liftScaledown);
            } else {
                controller.addInstruction(ReleasePosition.MIDDLE, 0);
                robot.lift.setPower(0);
            }
        } else {
            if (gamepad1.a) {
                robot.relicArmVexControl(.5, DcMotorSimple.Direction.REVERSE);
            } else if (gamepad1.y) {
                robot.relicArmVexControl(.5, DcMotorSimple.Direction.FORWARD);
            } else {
                robot.relicArmVexControl(0, DcMotorSimple.Direction.FORWARD);
            }

            if (cooldownServo1 <= 0) {
                if (gamepad1.right_bumper) {
                    relicArmPos1 += .02;
                    cooldownServo1 = cooldown;
                } else if (gamepad1.left_bumper) {
                    relicArmPos1 -= .02;
                    cooldownServo1 = cooldown;
                }
            }

            if (cooldownServo2 <= 0) {
                if (gamepad1.x) {
                    relicArmPos2 += .05;
                    cooldownServo2 = cooldown;

                } else if (gamepad1.b) {
                    relicArmPos2 -= .05;
                    cooldownServo2 = cooldown;
                }
            }
        }

        countdown--;
        cooldownServo1--;
        cooldownServo2--;
        wallCountdown--;

        if (cooldownServo1 == Integer.MIN_VALUE) {
            cooldownServo1 = 0;
        }
        if (cooldownServo2 == Integer.MIN_VALUE) {
            cooldownServo2 = 0;
        }
        if (countdown == Integer.MIN_VALUE) {
            countdown = 0;
        }
        if (wallCountdown == Integer.MIN_VALUE) {
            wallCountdown = 0;
        }

        relicArmPos1 = Range.clip(relicArmPos1, 0, 1);
        relicArmPos2 = Range.clip(relicArmPos2, 0, 1);
        robot.releaseMove(controller.process());
        robot.relicArmServo1.setPosition(relicArmPos1);
        robot.relicArmServo2.setPosition(relicArmPos2);
        controller.reset();

        //Telemetry things, generally booleans that could be important for drivers to be able to tell are active, as well as cooldowns
        telemetry.addData("Braking", brakeToggle);
        telemetry.addData("Alt Mode?", !glyphMode);
        telemetry.update();

    }

    @Override
    public void stop() {
        robot.drive(MovementEnum.STOP, 0);
    }

}