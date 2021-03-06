package org.firstinspires.ftc.teamcode.Telebop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Utility.Bot;
import org.firstinspires.ftc.teamcode.Utility.EnumController;
import org.firstinspires.ftc.teamcode.Utility.GlyphClamps;
import org.firstinspires.ftc.teamcode.Utility.ReleasePosition;

/**
 * Created by Rob Aburustum on (4/12/18)
 */

@TeleOp(name = "SINGLE TELEBOOP", group = "Teleop")
public class Worlds_Telebop_Single extends OpMode {

    Bot robot = new Bot();
    EnumController<ReleasePosition> glyphController;
    EnumController<GlyphClamps.ClampPos> frontClampController, backClampController;
    final double liftScaledown = 1;
    final double liftScaleup = 1; //Previously .75

    boolean brakeToggle, pingyBrakeToggle, invert, isRelicMode, movingIntake, placing, manualClampBack, manualClampFront, farOut;
    int brakeCooldown, invertCooldown, modeSwapCooldown, clawCooldown, placingCooldown, clampFrontCooldown, clampBackCooldown, placerDownCooldown;
    double relicArmPos1, relicArmPos2, leftTrigger, rightTrigger;

    @Override
    public void init() {
        robot.init(hardwareMap);
        glyphController = new EnumController<>(ReleasePosition.MIDDLE);
        frontClampController = new EnumController<>(GlyphClamps.ClampPos.STANDARD);
        backClampController = new EnumController<>(GlyphClamps.ClampPos.STANDARD);

        robot.setDriveMotorModes(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        robot.intakeDrop.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.jewelColorBack.enableLed(false);
        robot.jewelColorForward.enableLed(false);

        brakeToggle = pingyBrakeToggle = invert = isRelicMode = movingIntake = placing = manualClampBack = manualClampFront = farOut = false;
        brakeCooldown = invertCooldown = modeSwapCooldown = clawCooldown = placingCooldown = clampBackCooldown = clampFrontCooldown = placerDownCooldown = 0;
        relicArmPos1 = relicArmPos2 = 1;

        telemetry.addLine("Ready.");
    }

    @Override
    public void init_loop() {
    }

    @Override
    public void start() {
        robot.setDriveMotorModes(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.intakeDrop.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void loop() {

        leftTrigger = gamepad1.left_trigger > .9 ? 1 : (float).5 * gamepad1.left_trigger;
        rightTrigger = gamepad1.right_trigger > .9 ? 1 : (float).5 * gamepad1.right_trigger;

        if (gamepad1.start && brakeCooldown <= 0) {
            brakeToggle = !brakeToggle;
            brakeCooldown = 40;
        }

        robot.tankDrive(gamepad1.left_stick_y, gamepad1.right_stick_y, leftTrigger, rightTrigger, invert, brakeToggle, false);

        if (robot.intakeDrop.getCurrentPosition() >= 300) {
            glyphController.addInstruction(ReleasePosition.DROP, 10);
            robot.backIntakeWallDown();
            backClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 12);
            frontClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 12);
        }

        if (Math.abs(robot.lift.getCurrentPosition()) >= 300) {
            robot.backIntakeWallDown();
        }

        if (!isRelicMode) /*Glyph mode*/ {

            if (gamepad1.b) {
                robot.jewelTeleop();
            } else {
                robot.jewelUpTeleop();
            }

            if (gamepad1.y && placingCooldown <= 0) {
                placing = !placing;
                placingCooldown = 35;
            }

            if (gamepad1.x && placing) {
                frontClampController.addInstruction(GlyphClamps.ClampPos.RELEASE, 10);
                backClampController.addInstruction(GlyphClamps.ClampPos.RELEASE, 10);
            }

            if (placing) {
                if (placingCooldown <= 0) {
                    glyphController.addInstruction(ReleasePosition.UP, 10);
                }
                frontClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 5);
                backClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 5);
                manualClampBack = false;
                manualClampFront = false;
                robot.backIntakeWallDown();
            } else if (placingCooldown <= 10 && !(robot .intakeDrop.getCurrentPosition() > 300) && !(Math.abs(robot.lift.getCurrentPosition()) >= 300)) {
                robot.backIntakeWallUp();
            }

            if (!placing && placingCooldown >= 0) {
                frontClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 10);
                backClampController.addInstruction(GlyphClamps.ClampPos.CLAMPED, 10);
                brakeToggle = false;
            }

            if (gamepad1.left_bumper) {
                //Specific to the teleop, we have 3 levels of priority
                //A regular change in the position is 1 - these are the standard change
                glyphController.addInstruction(ReleasePosition.DOWN, 1);
                robot.intake(.7);
            } else {
                if (gamepad1.right_bumper) {
                    glyphController.addInstruction(ReleasePosition.DOWN, 1);
                    robot.intake(-.8);
                } else {
                    //This line is not needed, as this specific addition to the controller object will never change the output. However, it is included to keep clarity as to what will happen
                    //The zero priority will not change the result of process, as priority is seeded at 0 - and is strictly increasing. This is equivalent to a blank statement, which we use to keep code clarity
                    glyphController.addInstruction(ReleasePosition.MIDDLE, 0);
                    robot.intake(0);
                }
            }

            if (gamepad1.dpad_up) {
                glyphController.addInstruction(ReleasePosition.MIDDLEUP, 1);
                robot.lift.setPower(liftScaleup);
            } else if (gamepad1.dpad_down) {
                glyphController.addInstruction(ReleasePosition.MIDDLEUP, 1);
                robot.lift.setPower(liftScaledown);
            } else {
                glyphController.addInstruction(ReleasePosition.MIDDLE, 0);
                robot.lift.setPower(0);
            }

        }

        //Decrement cooldown counters
        invertCooldown--;
        brakeCooldown--;
        modeSwapCooldown--;
        clawCooldown--;
        placingCooldown--;
        clampBackCooldown--;
        clampFrontCooldown--;

        //Move the glyph plate
        robot.releaseMove(glyphController.process());
        glyphController.reset();

        //Process the clamps
        robot.glyphClamps.clampFront(frontClampController.process());
        robot.glyphClamps.clampBack(backClampController.process());
        //telemetry.addData("front clamp", frontClampController.process());
        //telemetry.addData("back clamp", backClampController.process());
        frontClampController.reset();
        backClampController.reset();

        //Move the relic arm servos

        relicArmPos2 = Range.clip(relicArmPos2, 0, 1);
        robot.relicArmServo1.setPosition(relicArmPos1);
        robot.relicArmServo2.setPosition(relicArmPos2);

        telemetry.addData("Inverted?", invert);
        telemetry.addData("Braking?", brakeToggle);
        telemetry.addData("Relic Mode?", isRelicMode);
        //telemetry.addData("PingyBraking?", pingyBrakeToggle);
    }

    @Override
    public void stop() {

    }
}