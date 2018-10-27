package piano;

import javax.sound.midi.MidiUnavailableException;

import java.util.Date;
import midi.Midi;
import music.Pitch;
import midi.Instrument;
import java.util.ArrayList;
import java.util.Arrays;
import music.NoteEvent;
import music.NoteEvent.Kind;

public class PianoMachine {
	
    public Instrument CURRENT_INSTRUMENT = Midi.DEFAULT_INSTRUMENT;
    
    // Array for every note with frequency from 0 to 10000
    private boolean [] currentNotes = new boolean[10000]; 
	private Midi midi;
    public int OCTAVE = 0;
    public boolean Recording = false;
    public ArrayList<NoteEvent> sequence = new ArrayList<>();
    public int lastEventTime;
    
    // Add wait duration to time in Midi.java
    public static long time;
	private int waitTime;

    //PianoMachine Constructor
    public PianoMachine() {
    	try {
            midi = Midi.getInstance();
        } catch (MidiUnavailableException e1) {
            System.err.println("Could not initialize midi device");
            e1.printStackTrace();
            return;
        }
    }
    
    // 
    public void beginNote(Pitch rawPitch) {
        // Get integer value of rawPitch
        int note = rawPitch.hashCode();
        
        // Check if note is not currently playing
        if (!currentNotes[note]){
            // Set note to be playing
            currentNotes[note] = true;
            
            // Play note
            midi.beginNote(new Pitch(note).toMidiFrequency() + OCTAVE,CURRENT_INSTRUMENT);
        } 
        
        // Check if we are Recording inputs
        if (Recording) {
           sequence.add(new NoteEvent(new Pitch(note + OCTAVE), time, CURRENT_INSTRUMENT, NoteEvent.Kind.start));
        }
    }
    
    public void endNote(Pitch rawPitch) {
        
        // Get integer value of rawPitch
        int note = rawPitch.hashCode();
        
        // Check if note is currently playing
        if (currentNotes[note]){
            midi.endNote(new Pitch(note).toMidiFrequency() + OCTAVE,CURRENT_INSTRUMENT);
            currentNotes[note] = false;
        }
        
        // Check if we are Recording inputs
        if (Recording) {
            sequence.add(new NoteEvent(new Pitch(note + OCTAVE), time, CURRENT_INSTRUMENT, NoteEvent.Kind.stop));
        }
    }
    
    public void changeInstrument() {
        CURRENT_INSTRUMENT = CURRENT_INSTRUMENT.next();
    }
    
    public void shiftUp() {
        if (OCTAVE < 24) {
    		OCTAVE += 12;
    	}
    }
    
    public void shiftDown() {
        if (OCTAVE > -24) {
    		OCTAVE -= 12;
    	}
    }
    
    public boolean toggleRecording() {
        
    	if (Recording == false) {
            // Reset the recording sequence each time we perform a new recording
            sequence.clear();
    		time = 0;
    		Recording = true;
    	} else {
    		Recording = false;
    	}
    	return Recording;
    }
    
    public void playback() {
        // Make sure that the sequence is not empty
    	if (sequence.size() > 0) {
            // For each NoteEvent...
    		for (int i = 0; i < sequence.size(); i++) {
    			if (i < sequence.size()-1) {
                    // Get waitTime between consecutive NoteEvents
                    // Cast long into int, getTime() returns long.
    				waitTime = (int) (sequence.get(i+1).getTime() - sequence.get(i).getTime());
    				if (sequence.get(i).getKind() == Kind.start) {
    					midi.beginNote(sequence.get(i).getPitch().toMidiFrequency(), sequence.get(i).getInstr());
    				} else {
    					midi.endNote(sequence.get(i).getPitch().toMidiFrequency(), sequence.get(i).getInstr());
    				}
    				midi.rest(waitTime);
    			} else {
    				if (sequence.get(i).getKind() == Kind.start) {
    					midi.beginNote(sequence.get(i).getPitch().toMidiFrequency(), sequence.get(i).getInstr());
    				} else {
    					midi.endNote(sequence.get(i).getPitch().toMidiFrequency(), sequence.get(i).getInstr());
    				}
    			}
    		}
    	}
    }
}
