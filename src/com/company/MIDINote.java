package com.company;

public class MIDINote {
    private long startMS;
    private long endMS;
    private String noteName;
    private int octave;
    private int note;
    private int velocity;

    public MIDINote(long startMS, long endMS, String noteName, int octave, int note, int velocity) {
        this.startMS = startMS;
        this.endMS = endMS;
        this.noteName = noteName;
        this.octave = octave;
        this.note = note;
        this.velocity = velocity;
    }

    public long getStartMS() {
        return startMS;
    }

    public void setStartMS(long startMS) {
        this.startMS = startMS;
    }

    public long getEndMS() {
        return endMS;
    }

    public void setEndMS(long endMS) {
        this.endMS = endMS;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public int getOctave() {
        return octave;
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }

}
