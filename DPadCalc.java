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
        double xAxis = myController.getRawAxis(RobotMap.kDpadHorizontalPort) + 2; // left = 1, up = 2, right = 3, down = 4
        double yAxis = -myController.getRawAxis(RobotMap.kDpadVerticalPort) + 3;
        if(!((xAxis != 2) == (yAxis != 3))) //(xAxis in center) xor (yAxis in center)
        {
            solution = xAxis == 2? yAxis: xAxis;
        }
        return (int)solution;
    }
}