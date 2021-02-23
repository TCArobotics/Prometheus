package frc.robot;

import edu.wpi.first.wpilibj.XboxController;

public class DPadCalc
{
    XboxController myController;

    public DPadCalc (final XboxController _myController)
    {
        this.myController = _myController;
    }
    
    public int get()
    {
        double solution = 0;
        double xAxis = myController.getRawAxis(RobotMap.kDpadHorizontalPort) + 2;
        double yAxis = -myController.getRawAxis(RobotMap.kDpadVerticalPort) + 3;
        if(!((xAxis != 0) == (yAxis != 0)))
        {
            solution = xAxis == 0? yAxis: xAxis;
        }
        return (int)solution;
    }
}