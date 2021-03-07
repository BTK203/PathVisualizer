// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.commands.InstantCommandSendPath;
import frc.robot.util.PVHost;
import frc.robot.util.Point2D;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final PVHost PATHVISUALIZER_HOST = new PVHost(3695);
  private final Point2D ROBOT_POSITION_PLACEHOLDER = new Point2D(3, 6, 95); //for testing purposes only. Should be replaced with a method that returns the current robot position on the field.

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Runs periodically at ~50hz
   */
  public void periodic() {
    PATHVISUALIZER_HOST.update(ROBOT_POSITION_PLACEHOLDER);
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    SmartDashboard.putData("Send Path", new InstantCommandSendPath(PATHVISUALIZER_HOST));
  }

  /**
   * Returns the auto command.
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return null;
  }
}
