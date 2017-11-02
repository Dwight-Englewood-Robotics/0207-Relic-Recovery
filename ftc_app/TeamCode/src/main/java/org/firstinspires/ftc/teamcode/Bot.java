package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.Enums.BotActions;


import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.teamcode.Enums.MovementEnum;

/**
 * Created by aburur on 8/6/17.
 */

public class Bot
{
    public Bot(){}

    /**
     * Motor Declarations
     * FR, FL, BR, BL are drive train motors
     */
    DcMotor FR, FL, BR, BL, intakeOne, intakeTwo, intakeBrake;

    /**
     * Servo Declarations
     */
    private Servo servo;
    private Servo armNoSpringyServo;
    private Servo armTopExtendyServo;
    private Servo armBottomExtendyServo;
    /**
     * Sensor Declarations
     * BNO055IMU is the builtin gyro on the REV Module
     */
    BNO055IMU imu;
    ModernRoboticsI2cColorSensor colorSensor;

    /**
     * Variable Declarations
     */

    /**
     * Other Declarations
     * Orientation angles is used for the REv Module's gyro, to store the headings
     */
    private Orientation angles;
    private double temp, forward, right, clockwise, k, frontLeft, frontRight, rearLeft, rearRight, powerModifier, headingError, driveScale,
            leftPower, rightPower;


    /**
     * Initialization Function
     */
    public void init(HardwareMap hardwareMap, Telemetry telemetry) {
        this.initialize(hardwareMap, telemetry);
    }

    private void initialize(HardwareMap hardwareMap, Telemetry telemetry) {

        //BNO055IMU related initialization code
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        //imu.initialize(parameters);

        //Colorsensor init code

        colorSensor = hardwareMap.get(ModernRoboticsI2cColorSensor.class, "cs");

        //servo init code
        servo = hardwareMap.get(Servo.class, "servo");
        armNoSpringyServo       = hardwareMap.get(Servo.class, "anss"); //Servo which prevents arm from springing out aka move this to extend arm
        armBottomExtendyServo   = hardwareMap.get(Servo.class, "abes"); //Servo which controls the clamp on the arm
        armTopExtendyServo      = hardwareMap.get(Servo.class, "ates"); //Servo which controls the angle of the hand

        //getting the motors from the hardware map
        FL = hardwareMap.get(DcMotor.class, "fl");
        FR = hardwareMap.get(DcMotor.class, "fr");
        BL = hardwareMap.get(DcMotor.class, "bl");
        BR = hardwareMap.get(DcMotor.class, "br");

        intakeBrake = hardwareMap.get(DcMotor.class, "brake");
        intakeOne = hardwareMap.get(DcMotor.class, "int1");
        intakeTwo = hardwareMap.get(DcMotor.class, "int2");

        //setting runmode
        FL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        FR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        BR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeBrake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeOne.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeTwo.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //setting directions for drive
        FL.setDirection(DcMotorSimple.Direction.FORWARD);
        FR.setDirection(DcMotorSimple.Direction.REVERSE);
        BL.setDirection(DcMotorSimple.Direction.FORWARD);
        BR.setDirection(DcMotorSimple.Direction.REVERSE);
        intakeBrake.setDirection(DcMotorSimple.Direction.FORWARD);
        intakeOne.setDirection(DcMotorSimple.Direction.FORWARD);
        intakeTwo.setDirection(DcMotorSimple.Direction.FORWARD);

        // TODO: Test different zeropower behaviors (BRAKE, FLOAT, etc)
        intakeBrake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeOne.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeTwo.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        FL.setPower(0);
        FR.setPower(0);
        BL.setPower(0);
        BR.setPower(0);
        intakeBrake.setPower(0);
        intakeOne.setPower(0);
        intakeTwo.setPower(0);

        powerModifier = 0.0055; // 180 * .0055 ~= 1
    }

    /**
     * Movement Functions
     */
    //TODO: DIAGONALS
    public void drive(MovementEnum movement, double power) {
       switch (movement){
           case FORWARD:
               FL.setPower(power);
               FR.setPower(power);
               BL.setPower(power);
               BR.setPower(power);
               break;

           case BACKWARD:
               FL.setPower(-power);
               FR.setPower(-power);
               BL.setPower(-power);
               BR.setPower(-power);
               break;

           case LEFTSTRAFE:
               FL.setPower(-power);
               FR.setPower(power);
               BL.setPower(power);
               BR.setPower(-power);
               break;

           case RIGHTSTRAFE:
               FL.setPower(power);
               FR.setPower(-power);
               BL.setPower(-power);
               BR.setPower(power);
               break;

           case LEFTTURN:
               FL.setPower(-power);
               FR.setPower(power);
               BL.setPower(-power);
               BR.setPower(power);
               break;

           case RIGHTTURN:
               FL.setPower(power);
               FR.setPower(-power);
               BL.setPower(power);
               BR.setPower(-power);
               break;

           case STOP:
               FL.setPower(0);
               FR.setPower(0);
               BL.setPower(0);
               BR.setPower(0);
               break;
       }
    }

    //TODO: Test different k values.
    public void fieldCentricDrive(double lStickX, double lStickY, double rStickX) {
        // Get the controller values
        forward = (-1)*lStickY;
        right =  lStickX;
        clockwise = rStickX;

        // Apply the turn modifier k
        clockwise *= k;

        // Turn the output heading value to be based on counterclockwise turns
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        if (angles.firstAngle < 0) {
            angles.firstAngle += 360;
        }

        // Convert to Radians for Math.sin/cos
        angles.firstAngle = (float)(angles.firstAngle * (Math.PI / 180));

        // Do Math
        temp = forward * Math.cos(angles.firstAngle) - right * Math.sin(angles.firstAngle);
        right = forward * Math.sin(angles.firstAngle) + right * Math.cos(angles.firstAngle);
        forward = temp;

        // Set power values using Math
        frontLeft = forward + clockwise + right;
        frontRight = forward - clockwise - right;
        rearLeft = forward + clockwise - right;
        rearRight = forward - clockwise + right;

        // Clip power values to within acceptable ranges for the motors
        frontLeft = Range.clip(frontLeft, -1.0, 1.0);
        frontRight = Range.clip(frontRight, -1.0, 1.0);
        rearLeft = Range.clip(rearLeft, -1.0, 1.0);
        rearRight = Range.clip(rearRight, -1.0, 1.0);

        // Send power values to motors
        FL.setPower(frontLeft);
        BL.setPower(rearLeft);
        FR.setPower(frontRight);
        BR.setPower(rearRight);
    }

    public void adjustHeading(int targetHeading) {
        headingError = targetHeading - imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;;
        driveScale = headingError * powerModifier;

        leftPower = 0 + driveScale;
        rightPower = 0 - driveScale;

        Range.clip(leftPower, -1, 1);
        Range.clip(rightPower, -1, 1);

        FL.setPower(leftPower);
        BL.setPower(leftPower);
        FR.setPower(rightPower);
        BR.setPower(rightPower);
    }

    /**
     * Functions for controlling servos on the relic arm
     */
    /**
     * Releases the arm
     */
    public void releaseTheKraken() {
        armNoSpringyServo.setPosition(.3);
        //hopefully this is in right direcrion
    }

    /**
     * Releases the arf iff the other one doesnt
     */
    public void backupCuzDontWantRecompile() {
        armNoSpringyServo.setPosition(.7);
        //one of those should work, just figure out which one
    }

    /**
     * moves the angle of hnad servo to a given position
     * @param position
     */
    public void armTopServoPos(double position) {
        armTopExtendyServo.setPosition(position);
    }

    /**
     *clamps or unclamps shit
     * @param position
     */
    public void armBotServoPos(double position) {
        armBottomExtendyServo.setPosition(position);
    }

    /**
     * let go of relic
     * TODO
     */
    public void ripTHICCBoi() {
        //TODO: make it so this lets go of the relic
    }

    /**
     * Action Functions
     * */
    public void servoDown() {
        servo.setPosition(0.4);
    }

    public void servoUp() {
        servo.setPosition(0.8);
    }

    public void intake(double power){
        intakeOne.setPower(power);
        intakeTwo.setPower(power);
    }

    /**
     * Sensor-Related Functions
     */

    /**
     * Helper Functions
     */
        public int distanceToRevs(double distance){
            final double wheelCirc = 31.9185813;

            final double gearMotorTickThing = .5 * 1120; //neverrest 40 = 1120,

            return (int)(gearMotorTickThing * (distance / wheelCirc));
        }
}
