
package com.github.hisahi.u020toolchain.hardware;

import com.github.hisahi.u020toolchain.cpu.StandardMemory;
import com.github.hisahi.u020toolchain.cpu.UCPU16;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.scene.input.KeyCode;
import static javafx.scene.input.KeyCode.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the implementation of the Generic Keyboard peripheral.
 * 
 * @author hisahi
 */
public class KeyboardTest {
    Keyboard kb;
    
    public KeyboardTest() {
    }
    
    @Before
    public void setUp() {
        kb = new Keyboard(new UCPU16(new StandardMemory()));
    }

    @Test
    public void keyDownTest() {
        assertFalse("key should not be down by default", kb.isKeyDown(0x20));
        kb.keyDown(SPACE, false, false);
        assertTrue("key isn't set to be down correctly", kb.isKeyDown(0x20));
    }

    @Test
    public void keyUpTest() {
        kb.keyDown(SPACE, false, false);
        kb.keyUp(SPACE, false, false);
        assertFalse("key isn't set to be up correctly", kb.isKeyDown(0x20));
    }

    @Test
    public void keyboardCodeConversionTest() throws IOException {
        final KeyCode[] keycodes = new KeyCode[] {DIGIT1, DIGIT0, SPACE, P, R, I, N, T, DIGIT4, DIGIT0, MINUS, DIGIT1, ENTER, DIGIT2, DIGIT0, SPACE, G, O, T, P, BACK_SPACE, O, DIGIT1, DIGIT0, ENTER, R, U, N, ENTER};
        for (int i = 0; i < keycodes.length; ++i) {
            kb.keyDown(keycodes[i], false, false);
            kb.keyUp(keycodes[i], false, false);
        }
        final int[] expectedKeyQueue = new int[] {0x31, 0x30, 0x20, 0x50, 0x52, 0x49, 0x4e, 0x54, 0x34, 0x30, 0x2d, 0x31, 0x11, 0x32, 0x30, 0x20, 0x47, 0x4f, 0x54, 0x50, 0x10, 0x4f, 0x31, 0x30, 0x11, 0x52, 0x55, 0x4e, 0x11};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        kb.saveState(dos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        baos.close();
        int c = dis.readInt();
        assertEquals("keyboard queue didn't accept all keys", expectedKeyQueue.length, c);
        for (int i = 0; i < c; ++i) {
            assertEquals("keyboard queue didn't convert all keys correctly", expectedKeyQueue[i], dis.readInt());
        }
        bais.close();
    }

    @Test
    public void keyQueueLengthTest() throws IOException {
        for (int i = 0; i < 50; ++i) {
            kb.keyDown(ENTER, false, false);
            kb.keyUp(ENTER, false, false);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        kb.saveState(dos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        baos.close();
        int c = dis.readInt();
        assertEquals("keyboard queue should be limited to 32 keys", 32, c);
        bais.close();
    }
    
    public boolean matchesKeyCode(int expected, KeyCode code, boolean shift, boolean ctrl) {
        kb.keyDown(code, shift, ctrl);
        boolean ok = kb.isKeyDown(expected);
        kb.keyUp(code, shift, ctrl);
        return ok;
    }

    @Test
    public void shiftResultsInLowercaseTest() throws IOException {
        final KeyCode[] ALPHABET_KEYS = {A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z};
        for (int i = 0; i < 26; ++i) {
            assertTrue(matchesKeyCode(0x41 + i, ALPHABET_KEYS[i], false, false));
            assertTrue(matchesKeyCode(0x61 + i, ALPHABET_KEYS[i], true, false));
        }
    }

    @Test
    public void specialKeysTest() throws IOException {
        assertTrue(matchesKeyCode(0x10, BACK_SPACE, false, false));
        assertTrue(matchesKeyCode(0x11, ENTER, false, false));
        assertTrue(matchesKeyCode(0x12, INSERT, false, false));
        assertTrue(matchesKeyCode(0x13, DELETE, false, false));
        assertTrue(matchesKeyCode(0x180, UP, false, false));
        assertTrue(matchesKeyCode(0x181, DOWN, false, false));
        assertTrue(matchesKeyCode(0x182, LEFT, false, false));
        assertTrue(matchesKeyCode(0x183, RIGHT, false, false));
        assertTrue(matchesKeyCode(0x190, SHIFT, false, false));
        assertTrue(matchesKeyCode(0x191, CONTROL, false, false));
        assertTrue(matchesKeyCode(0x192, HOME, false, false));
    }
}
