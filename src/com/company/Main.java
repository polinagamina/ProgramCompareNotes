package com.company;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class Main {
    public static final int NOTE_ON = 0x90;
    public static final int NOTE_OFF = 0x80;
    public static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final double COEFFICIENT_MAGENTA_A = 1.3;
    private static final double COEFFICIENT_MAGENTA_B = -5293570.29;
    private static final double COEFFICIENT_BAD_APPLE_A = 0.82;
    private static final double COEFFICIENT_BAD_APPLE_B = 1291068.71;
    private static final double COEFFICIENT_ELSA_MARIA_A = 0.97;
    private static final double COEFFICIENT_ELSA_MARIA_B = 565237.73;
    private static final double COEFFICIENT_MAGENTA_ADDITIONAL = 200000;


    public static void main(String[] args) throws IOException, InvalidMidiDataException {
        List<MIDINote> originalSong = new ArrayList<>();
        originalSong = makeNote("Piano.mid");
        originalSong = originalSong.stream().sorted(Comparator.comparingLong(MIDINote::getStartMS)).collect(Collectors.toList());
        for (MIDINote note : originalSong) {
            System.out.println(note.getStartMS() + " " + note.getNoteName() + note.getOctave());
        }
        System.out.println(originalSong);
        System.out.println("magenta song");
        List<MIDINote> magentaSong = new ArrayList<>();
        magentaSong = makeNote("Piano.midi");
        magentaSong = magentaSong.stream().sorted(Comparator.comparingLong(MIDINote::getStartMS)).collect(Collectors.toList());
        /*for (MIDINote note:magentaSong) {
            System.out.println(note.getStartMS()+" "+note.getNoteName()+note.getOctave());
        }*/
        for (MIDINote midiNote : magentaSong) {
            double timeStartToOriginal = COEFFICIENT_MAGENTA_A * midiNote.getStartMS() + COEFFICIENT_MAGENTA_B;
            midiNote.setStartMS((long) timeStartToOriginal);
            double timeEndToOriginal = COEFFICIENT_MAGENTA_A * midiNote.getEndMS() + COEFFICIENT_MAGENTA_B;
            midiNote.setEndMS((long) timeEndToOriginal);
        }
        int sum = 0;
        int sumfalse = 0;
        for (MIDINote note : originalSong) {
            List<MIDINote> sortNotes = new ArrayList<>();
            sortNotes = magentaSong.stream().filter(sortNote -> sortNote.getStartMS() >= note.getStartMS() - 200000 && sortNote.getStartMS() <= note.getStartMS() + 200000 && sortNote.getNoteName().equals(note.getNoteName())).collect(Collectors.toList());
            if (sortNotes.size() != 0) {
                System.out.println("True" + note.getNoteName() + note.getStartMS());
                sum += 1;
            } else {
                System.out.println("False");
                sumfalse += 1;
            }
        }
        System.out.println("sum" + sum);
        System.out.println("sumfalse" + sumfalse);

    }

    public static ArrayList<MIDINote> makeNote(String musicPath) throws IOException, InvalidMidiDataException {

        Sequence sequence = MidiSystem.getSequence(new File(musicPath));
        int mpq = 60000000 / 100;
        int seqres = sequence.getResolution();
        long lasttick = 0;
        long curtime = 0;
        float divtype = sequence.getDivisionType();

        ArrayList<MIDINote> notes = new ArrayList<>();
        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            trackNumber++;
            // System.out.println("Track " + trackNumber + ": size = " + track.size());

            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                //System.out.print("@" + event.getTick() + " ");
                if (divtype == Sequence.PPQ) {
                    curtime += ((event.getTick() - lasttick) * mpq) / seqres;
                } else {
                    curtime = (long) ((event.getTick() * 1000000.0 * divtype) / seqres);

                }
                lasttick = event.getTick();
                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    // System.out.print("Channel: " + sm.getChannel() + " ");
                    if (sm.getCommand() == NOTE_ON) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        MIDINote midiNote = new MIDINote(curtime, 0, noteName, octave, note, velocity);
                        if (velocity != 0) {
                            notes.add(midiNote);
                        } else {
                            List<MIDINote> sortNotes = new ArrayList<>();
                            sortNotes = notes.stream().filter(sortMidiNote -> sortMidiNote.getEndMS() == 0 && sortMidiNote.getNoteName().equals(noteName) && sortMidiNote.getOctave() == octave).collect(Collectors.toList());
                            sortNotes.get(0).setEndMS(curtime);
                        }
                        // msToNoteStart.put(curtime, noteName);
                        //System.out.println("Note on, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else if (sm.getCommand() == NOTE_OFF) {
                        int key = sm.getData1();
                        int octave = (key / 12) - 1;
                        int note = key % 12;
                        String noteName = NOTE_NAMES[note];
                        int velocity = sm.getData2();
                        if (notes != null) {
                            List<MIDINote> sortNotes = new ArrayList<>();
                            sortNotes = notes.stream().filter(midiNote -> midiNote.getEndMS() == 0 && midiNote.getNoteName().equals(noteName) && midiNote.getOctave() == octave).collect(Collectors.toList());
                            sortNotes.get(0).setEndMS(curtime);
                        }
                        // System.out.println("Note off, " + noteName + octave + " key=" + key + " velocity: " + velocity);
                    } else {
                        // System.out.println("Command:" + sm.getCommand());
                    }
                } else {
                    //System.out.println("Other message: " + message.getClass());
                }
            }


        }
        return notes;
    }
}

