package org.jvoicexml.jsapi2.synthesis.freetts;

import org.jvoicexml.jsapi2.synthesis.BaseSynthesizer;
import com.sun.speech.freetts.audio.AudioPlayer;
import javax.speech.synthesis.SynthesizerMode;
import javax.speech.synthesis.Speakable;
import org.jvoicexml.jsapi2.BaseAudioManager;
import com.sun.speech.freetts.FreeTTSSpeakableImpl;
import javax.speech.AudioSegment;
import java.io.ByteArrayInputStream;

/**
 * Copyright 2003 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */


/**
 * Provides  partial support for a JSAPI 2.0 synthesizer for the
 * FreeTTS speech synthesis system.
 */
public class FreeTTSSynthesizer extends BaseSynthesizer {
    /**
     * Reference to output thread.
     */
 //   OutputHandler outputHandler;

    /**
     * The currently active voice for this synthesizer
     */
    private FreeTTSVoice curVoice;

    private AudioPlayer audioPlayer;

    //private FreeTTSAudioPlayer audioPlayer;

    /**
     * All voice output for this synthesizer goes through
     * this central utterance queue
     */
  //////////////////////////////////////////////////////////////////////  private OutputQueue outputQueue;

    /**
     * Creates a new Synthesizer in the DEALLOCATED state.
     *
     * @param desc describes the allowed mode of operations for this
     * 		synthesizer.
     */
    public FreeTTSSynthesizer(FreeTTSSynthesizerMode desc) {
        super(desc);
      /////////////////////////////////////  outputHandler = new OutputHandler();
      audioPlayer = null;
      super.setSynthesizerProperties(new FreeTTSEngineProperties(this));
    }

    /**
     * Starts the output thread. The output thread is responsible for
     * taking items off of the queue and sending them to the audio
     * player.
     *
     * @throws EngineException if an allocation error occurs
     */
    public boolean handleAllocate() {
        long states[];
	boolean ok = false;
        SynthesizerMode synthesizerMode = (SynthesizerMode)getEngineMode();

///////////////////////////////////////////////////////////////////////	outputQueue = com.sun.speech.freetts.Voice.createOutputThread();

	if (synthesizerMode.getVoices().length > 0) {
	    FreeTTSVoice freettsVoice = (FreeTTSVoice) synthesizerMode.getVoices()[0];
            ok = setCurrentVoice(freettsVoice);
	}

	if (ok) {


            audioPlayer = new FreeTTSAudioPlayer(((BaseAudioManager)getAudioManager()).getOutputStream());

            synchronized (engineStateLock) {
		long newState = ALLOCATED | RESUMED;
		newState |= (queueManager.isQueueEmpty()
			     ? QUEUE_EMPTY
			     : QUEUE_NOT_EMPTY);
		states = setEngineState(CLEAR_ALL_STATE, newState);
	    }
	    ////////////////////////////////outputHandler.start();
            //postEngineEvent(states[0], states[1], EngineEvent.ENGINE_ALLOCATED);
	} else {
           System.out.println("Can't allocate FreeTTS synthesizer");
           return false;
	}

        return true;
    }



    /**
     * Sets the given voice to be the current voice. If
     * the voice cannot be loaded, this call has no affect.
     *
     * @param voice the new voice.
     */
   protected boolean setCurrentVoice(FreeTTSVoice voice) {


	com.sun.speech.freetts.Voice freettsVoice = voice.getVoice();
	boolean ok = false;


	if (!freettsVoice.isLoaded()) {
///////////////////////////////////////////////////////////////////////////////	    freettsVoice.setOutputQueue(outputQueue);
	    freettsVoice.allocate();
            /*audio = freettsVoice.getAudioPlayer();
            if (audio == null) {
                audio = new com.sun.speech.freetts.audio.JavaClipAudioPlayer();
            }
            if (audio == null) {
                throw new EngineException("Can't get audio player");
            }*/
	    freettsVoice.setAudioPlayer(audioPlayer);
	}
        else {
	    curVoice = voice;
	    ok = true;
	    // notify the world of potential property changes
	    ///////////////////////FreeTTSSynthesizerProperties props =
            ///////////////////////(FreeTTSSynthesizerProperties) getSynthesizerProperties();
	    ///////////////////////props.checkForPropertyChanges();
	}
	return ok;
    }

    /**
     * Handles a deallocation request. Cancels all pending items,
     * terminates the output handler, and posts the state changes.
     *
     * @throws EngineException if a deallocation error occurs
     */
    public boolean handleDeallocate() {
        long[] states = setEngineState(CLEAR_ALL_STATE, DEALLOCATED);
        queueManager.cancelAllItems();
        queueManager.terminate();

	// Close the audio. This should flush out any queued audio data

	if (audioPlayer != null) {
	    audioPlayer.close();
	}

    /////////////////////////////////////////////////////////////////////    outputQueue.close();

        //postEngineEvent(states[0], states[1], EngineEvent.ENGINE_DEALLOCATED);

        return true;
    }

    /**
     * Factory method to create a BaseSynthesizerQueueItem.
     *
     * @return a queue item appropriate for this synthesizer
     */
 /*   protected BaseSynthesizerQueueItem createQueueItem() {
        return new FreeTTSSynthesizerQueueItem();
    }*/

    /**
     * Returns an enumeration of the queue.
     *
     * @return an enumeration of the contents of the queue. This
     * 		enumeration contains FreeTTSSynthesizerQueueItem objects
     *
     * @throws EngineStateError if the engine was not in the proper
     * 				state
     */
 /*   public Enumeration enumerateQueue() throws EngineStateException {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        return outputHandler.enumerateQueue();
    }*/

    /**
     * Places an item on the speaking queue and send the queue update event.
     *
     * @param item	the item to place  in the queue
     */
 /*   protected void appendQueue(BaseSynthesizerQueueItem item) {
        outputHandler.appendQueue((FreeTTSSynthesizerQueueItem) item);
    }*/

    /**
     * Cancels the item at the top of the queue.
     *
     * @throws EngineStateError if the synthesizer is not in the
     * 				proper state
     */
 /*   public void cancel() throws EngineStateException {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem();
    }*/

    /**
     * Cancels a specific object on the queue.
     *
     * @param source the object to cancel
     *
     * @throws IllegalArgumentException if the source object is not
     * 					currently in the queue
     * @throws EngineStateError		the synthesizer is not in the
     * 					proper state
     */
 /*   public void cancel(Object source)
        throws IllegalArgumentException, EngineStateException {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelItem(source);
    }*/

    /**
     * Cancels all items on the output queue.
     *
     * @throws EngineStateError
     */
   /* public void cancelAll() throws EngineStateException {
        checkEngineState(DEALLOCATED | DEALLOCATING_RESOURCES);
        outputHandler.cancelAllItems();
    }*/

    /**
     * Pauses the output
     */
    public boolean handlePause() {
	audioPlayer.pause();
        return true;
    }

    /**
     * Resumes the output
     */
    public boolean handleResume() {
	audioPlayer.resume();
        return true;
    }

    /**
     * Factory constructor for EngineProperties object.
     * Gets the default speaking voice from the SynthesizerModeDesc.
     * Takes the default prosody values (pitch, range, volume, rate)
     * from the default voice.
     * Override to set engine-specific defaults.
     */
 /*   protected BaseEngineProperties createEngineProperties() {
        SynthesizerMode desc = (SynthesizerMode)engineMode;
        FreeTTSVoice defaultVoice = (FreeTTSVoice)(desc.getVoices()[0]);
        return new FreeTTSSynthesizerProperties(defaultVoice,
			 defaultVoice.getPitch(),
			 defaultVoice.getPitchRange(),
			 defaultVoice.getSpeakingRate(),
			 defaultVoice.getVolume());
    }*/


    protected Speakable getSpeakable(String text) {
        return new FreeTTSSpeakable(text);
    }

    protected Speakable parseMarkup(String synthesisMarkup) {
        return null;
    }

    /**
     * Outputs the given queue item to the current voice
     *
     * @param item the item to output
     */
    protected void handleSpeak(int id, Speakable item) {
        com.sun.speech.freetts.Voice voice = curVoice.getVoice();
        voice.setVerbose(true);
        voice.setAudioPlayer(audioPlayer);
        voice.speak(new FreeTTSSpeakableImpl(item.getMarkupText()));
        System.out.println("*************************AGORA ACABOU");
        System.out.println("*************************esta disponivel" + ((FreeTTSAudioPlayer) voice.getAudioPlayer()).getAudioBytes().length);
        setAudioSegment(id,
                        new AudioSegment(new ByteArrayInputStream(((FreeTTSAudioPlayer) voice.getAudioPlayer()).getAudioBytes()),
                                         null,
                                         null));
        ((FreeTTSAudioPlayer) voice.getAudioPlayer()).clearAudioBytes();
    }


    protected void handleSpeak(int id, String text) {
        handleSpeak(id, new FreeTTSSpeakable(text));
    }

    protected boolean handleCancelAll(){
        return false;
    }

    protected boolean handleCancel(int id){
        return false;
    }

    protected boolean handleCancel(){
        return false;
    }




}