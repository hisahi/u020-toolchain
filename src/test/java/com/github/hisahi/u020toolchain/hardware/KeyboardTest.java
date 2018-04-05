
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class KeyboardTest {
    Keyboard kb;
    
    public KeyboardTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        kb = new Keyboard(new UCPU16(new StandardMemory()));
    }
    
    @After
    public void tearDown() {
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
}
