package image.turingPattern;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class SoundUtility
{
    public static void playSound(String soundName)
    {
        AudioClip audioClip = getClip(soundName);
        if (audioClip != null)
             audioClip.play();
     }
    //----------------------------------------------------------------------    
    private static URL codeBase;
    private static HashMap<URL, AudioClip> soundMap = new HashMap<URL, AudioClip>();
    static
    {
        try 
        {
            codeBase = new URL("file:" + System.getProperty("user.dir") + "/");
        } 
        catch (MalformedURLException e)      {    e.printStackTrace(); System.err.println(e.getMessage());    }        
    }
    
    //----------------------------------------------------------------------         
    private static AudioClip getClip(String soundName)
    {
        String extension = "wav";
        String soundDir = "sounds";
        String relativePath = soundDir + "/" + soundName + "." + extension;
        
        AudioClip sound = null;
        URL theURL;
        try 
        {              
            theURL = new URL(codeBase, relativePath);
            sound = soundMap.get(theURL);
            if (sound == null)
            {
                URL jarURL = ClassLoader.getSystemResource(relativePath);
               sound = Applet.newAudioClip((jarURL != null) ? jarURL : theURL);  
                if (sound != null)
                    soundMap.put(theURL, sound);  
                else
                    System.err.println("Missing sound file: " + soundName + ".  Could not find at: " + jarURL + ", or: " + theURL);
            }
        } 
        catch (MalformedURLException e)        {       e.printStackTrace(); System.err.println(e.getMessage());      }            
        return sound;
    }

    //----------------------------------------------------------------------    
     }
