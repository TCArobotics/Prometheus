package frc.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.XboxController;

public class ManipulatorControl
{
    private final DoubleSolenoid ballDoorSolenoid;
    private final Compressor m_pneumaticCompressor;
    
    private final Debouncer homeButton;
    private final Debouncer aButton;
    private final XboxController manipController;

    public ManipulatorControl()
    {
        manipController = new XboxController(RobotMap.kManipulatorControllerPort);
        ballDoorSolenoid = new DoubleSolenoid(1, 0);
        m_pneumaticCompressor = new Compressor();

        homeButton = new Debouncer(manipController, RobotMap.kManHomePort);
        aButton = new Debouncer(manipController, RobotMap.kManAPort);
    }

    public void calculate()
    {
        if(homeButton.get())
        {
            if(m_pneumaticCompressor.enabled())
            {
                m_pneumaticCompressor.stop();
            }
            else
            {
                m_pneumaticCompressor.start();
            }
        }

        if(aButton.get())
        {
            if(ballDoorSolenoid.get() == DoubleSolenoid.Value.kForward)
            {
                ballDoorSolenoid.set(DoubleSolenoid.Value.kReverse);
            }
            else
            {
                ballDoorSolenoid.set(DoubleSolenoid.Value.kForward);
            }
        }
    }
}