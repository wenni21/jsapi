/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $LastChangedDate $
 * Author:  $LastChangedBy$
 *
 * JSAPI - An independent reference implementation of JSR 113.
 *
 * Copyright (C) 2007-2012 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.jvoicexml.jsapi2.jse.recognition;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.speech.AudioException;
import javax.speech.EngineStateException;

import org.jvoicexml.jsapi2.JseBaseAudioManager;
import org.jvoicexml.jsapi2.protocols.JavaSoundParser;

/**
 * Supports the JSAPI 2.0 <code>AudioManager</code> interface. Actual JSAPI
 * implementations might want to extend or modify this implementation.
 */
public class BaseRecognizerAudioManager extends JseBaseAudioManager {
    /** Logger instance. */
    private final static Logger LOGGER =
        Logger.getLogger(BaseRecognizerAudioManager.class.getCanonicalName());


    /** The input stream for the recognizer. */
    private InputStream inputStream;

    /**
     * Constructs a new object.
     * @param format native engine audio format
     */
    public BaseRecognizerAudioManager(final AudioFormat format) {
        super(format);
    }

    /**
     * Retrieves a stream that matches the engine audio format.
     * @param stream the current stream
     * @return a converting stream
     */
    private AudioInputStream getConvertedStream(final AudioInputStream stream) {
        targetAudioFormat = stream.getFormat();
        return AudioSystem.getAudioInputStream(engineAudioFormat, stream);
    }

    /**
     * Opens the URL with the given locator
     * @param locator the URL to open
     * @return opened connection to the URL
     * @throws AudioException 
     *         error opening the stream
     */
    private InputStream openUrl(final String locator) throws AudioException {
        targetAudioFormat = null;
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "opening locator at: '" + locator + "'");
        }
        final URL url;
        try {
            url = new URL(locator);
            final AudioFileFormat format =
                AudioSystem.getAudioFileFormat(url);
            targetAudioFormat = format.getFormat();
        } catch (MalformedURLException e) {
            throw new AudioException(e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            throw new AudioException(e.getMessage());
        } catch (IOException e) {
            throw new AudioException(e.getMessage());
        }
        if (targetAudioFormat == null) {
            try {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, "AudioFormat is unknown. Trying to parse URI query...");
                
                targetAudioFormat = JavaSoundParser.parse(url);
                
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.log(Level.FINE, "... Got AudioFormat: {0}", targetAudioFormat.toString());
            } catch (URISyntaxException e) {
                throw new AudioException(e.getMessage());
            }
        }
        final URLConnection urlConnection;
        try {
            urlConnection = openURLConnection();
        } catch (IOException e) {
            throw new AudioException(e.getMessage());
        }

        try {
            final InputStream source  = urlConnection.getInputStream();
            return new BufferedInputStream(source);
        } catch (IOException ex) {
            throw new AudioException("Cannot get InputStream from URL: "
                    + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void handleAudioStart() throws AudioException {
        // Just convert samples if we already have the correct stream
        if (inputStream instanceof AudioInputStream) {
            final AudioInputStream stream = (AudioInputStream)inputStream;
            inputStream = getConvertedStream(stream);
            targetAudioFormat = stream.getFormat();
            return;
        }

        final String locator = getMediaLocator();
        final InputStream in;
        // Open URL described in locator
        if (locator == null || locator.isEmpty()) {
            // Use the microphone
            targetAudioFormat = getEngineAudioFormat();
//            final InputStream source = new LineInputStream(targetAudioFormat);
            /*******************************************************/
            InputStream source;
            try {
                TargetDataLine lineLocalMic =
                    AudioSystem.getTargetDataLine(targetAudioFormat);
                source = new AudioInputStream(lineLocalMic);
                lineLocalMic.open();
                lineLocalMic.start();
            } catch (LineUnavailableException e) {
                throw new AudioException(e.getMessage());
            }
            /*******************************************************/
            in = new BufferedInputStream(source);
        } else {
            in = openUrl(locator);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "using target audio format {0}",
                    targetAudioFormat);
        }
        final AudioInputStream stream = new AudioInputStream(in,
                    targetAudioFormat, AudioSystem.NOT_SPECIFIED);
        inputStream = AudioSystem.getAudioInputStream(engineAudioFormat, stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAudioStop() throws AudioException {
        if (inputStream == null) {
            return;
        }

        // Release IO
        try {
            inputStream.close();
        } catch (IOException ex) {
            throw new AudioException(ex.getMessage());
        } finally {
            inputStream = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setMediaLocator(final String locator, final InputStream stream)
            throws AudioException {
        super.setMediaLocator(locator);
        inputStream = stream;
    }

    /**
     * {@inheritDoc}
     *
     * Throws an {@link IllegalArgumentException} since output streams are not
     * supported.
     */
    public void setMediaLocator(String locator, OutputStream stream)
            throws AudioException, EngineStateException,
            IllegalArgumentException, SecurityException {
        throw new IllegalArgumentException("output streams are not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream() {
        throw new IllegalArgumentException("output streams are not supported");
    }
}