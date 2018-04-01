
package com.github.hisahi.u020toolchain.hardware; 

import com.github.hisahi.u020toolchain.cpu.UCPU16;
import com.github.hisahi.u020toolchain.logic.ITickable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class UNAC810 extends Hardware implements ITickable { 
    private SourceDataLine sdl;
    private double volume;
    private boolean on;
    private boolean paused;
    private int tick;
    private int speed;
    private int sample;
    private byte[] buffer;
    private int bufptr;

    public UNAC810(UCPU16 cpu) {
        super(cpu);
        this.buffer = new byte[200];
        this.speed = 200;
        this.reset();
    }
    
    public void setVolume(double vol) {
        this.volume = vol;
        if (sdl.isControlSupported(FloatControl.Type.VOLUME)) {
            FloatControl volume = (FloatControl) sdl.getControl(FloatControl.Type.VOLUME);
            volume.setValue((float) vol);
        } else if (sdl.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volume = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
            float gain = (float) (20 * Math.log(vol));
            if (gain < volume.getMinimum()) {
                gain = volume.getMinimum();
            }
            volume.setValue(gain);
        }
    }

    @Override
    public long hardwareId() {
        return 0x2feaccd6;
    }

    @Override
    public int hardwareVersion() {
        return 0x2810;
    }

    @Override
    public long hardwareManufacturer() {
        return 0x2590a31c;
    }

    @Override
    public void hwi(UCPU16 cpu) {
        if (this.sdl == null) {
            return;
        }
        switch (cpu.readRegister(UCPU16.REG_A)) {
            case 0:
                on = cpu.readRegister(UCPU16.REG_B) != 0;
                if (!on && sdl.isRunning()) {
                    sdl.stop();
                }
                break;
            case 1: {
                int b = cpu.readRegister(UCPU16.REG_B) & 0xFF;
                sample = (b | (b << 8)) ^ 0x8000;
                break;
            }
        }
    }

    @Override
    public void reset() {
        this.volume = 1.0;
        this.tick = 0;
        this.sdl = null;
        this.sample = 0x0080;
        final AudioFormat audioFormat = new AudioFormat(10000, 16, 1, true, true);
        try {
            this.sdl = (SourceDataLine) AudioSystem.getSourceDataLine(audioFormat);
        } catch (LineUnavailableException ex) {
            Logger.getLogger(UNAC810.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.sdl.open();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(UNAC810.class.getName()).log(Level.SEVERE, null, ex);
            this.sdl = null;
        }
    }

    @Override
    public void pause() {
        paused = sdl.isRunning();
        sdl.stop();
    }

    @Override
    public void resume() {
        if (!paused) {
            sdl.flush();
            sdl.start();
        }
    }

    @Override
    public void saveState(DataOutputStream stream) throws IOException {
        stream.write(on ? 1 : 0);
        stream.writeInt(tick);
        stream.writeInt(bufptr);
        stream.writeInt(buffer.length);
        for (int i = 0; i < buffer.length; ++i) {
            stream.write(buffer[i]);
        }
    }

    @Override
    public void restoreState(DataInputStream stream) throws IOException {
        on = stream.read() != 0;
        tick = stream.readInt();
        bufptr = stream.readInt();
        int buflen = stream.readInt();
        for (int i = 0; i < buflen; ++i) {
            buffer[i] = (byte) stream.read();
        }
    }

    @Override
    public void tick() {
        if (this.sdl == null) {
            return;
        }
        if (!this.on) {
            return;
        }
        ++tick;
        while (tick >= this.speed) {
            tick -= this.speed;
            buffer[bufptr++] = (byte) ((sample >> 8) & 0xFF);
            buffer[bufptr++] = (byte) (sample & 0xFF);
        }
        if (bufptr == buffer.length) {
            if (!sdl.isRunning()) {
                sdl.start();
            }
            sdl.write(buffer, 0, buffer.length);
            bufptr = 0;
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
