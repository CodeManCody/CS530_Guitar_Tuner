import Tuner.*;
import GUI.*;

public class AllTuner
{
    public static void main(String[] args) throws Exception 
    {
        TunerFrame tunerFrame = new TunerFrame();
        while(true)
        {
            TunerCalculator tuner = new TunerCalculator();
            tunerFrame.updateFrequency(tuner.getFrequency());
            
            try { Thread.sleep(250); } 
            catch (Exception e) {}
        }
    }
}