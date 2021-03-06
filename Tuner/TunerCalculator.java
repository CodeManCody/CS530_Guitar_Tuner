
package Tuner;

import javax.sound.sampled.*;
import Tuner.*;

public class TunerCalculator 
{
    private double frequency = 0;
    private float sampleRate = 44100;
    private int sampleSizeInBits = 16;
    private int channels = 1;
    private boolean signed = true;
    private boolean bigEndian = false;
    
    AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
    TargetDataLine targetDataLine;
    TunerData data = new TunerData();
	
    public int getClosestNote(double Frequency)
    {
        double minDist = Double.MAX_VALUE;
        int minFreq = -1;
        
        for(int i = 0; i < data.getFrequencies().length; i++) 
        {
            double dist = Math.abs(data.getFrequencies()[i] - Frequency);
            if (dist < minDist) 
            {
                minDist=dist;
                minFreq=i;
            }
        }
        return minFreq;
    }

    public double getFrequency()
    {
    	frequency=0;
        try { targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo); } 
        catch (LineUnavailableException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }

        // read about a second at a time
        try { targetDataLine.open(format, (int)sampleRate); } 
        catch (LineUnavailableException e) 
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
        targetDataLine.start();

        byte[] buffer = new byte[2*1200];
        int[] a = new int[buffer.length/2];
        int n = -1;
        long pTimeout = System.currentTimeMillis();
        while ( (n = targetDataLine.read(buffer, 0, buffer.length)) > 0 )
        {
            for (int i = 0; i < n; i+= 2)
            {
                // convert two bytes into single value
                int value = (short)((buffer[i]&0xFF) | ((buffer[i+1]&0xFF) << 8));
                a[i >> 1] = value;
            }

            double prevDiff = 0;
            double prevDx = 0;
            double maxDiff = 0;
            int sampleLen = 0;
            int len = a.length/2;

            for (int i = 0; i < len; i++) 
            {
                double diff = 0;
                for (int j = 0; j < len; j++) 
                    diff += Math.abs(a[j]-a[i+j]);

                double dx = prevDiff-diff;

                // change of sign in dx
                if (dx < 0 && prevDx > 0) 
                {
                    // only look for troughs that drop to less than 10% of peak
                    if ( diff < (0.2*maxDiff) )     // ** changed to 20% **
                    { 
                        if ( sampleLen == 0 ) 
                            sampleLen=i-1;
                    }
                }

                prevDx = dx;
                prevDiff=diff;
                maxDiff=Math.max(diff,maxDiff);
            }
            
            if (sampleLen > 0) 
            {
                frequency = (format.getSampleRate()/sampleLen);
                targetDataLine.close();
                return frequency;
            }else if(System.currentTimeMillis()-pTimeout>2000){
                targetDataLine.close();
                frequency=0;
                return frequency;

            }
        }
        
        targetDataLine.close();
        return frequency;
    }
}