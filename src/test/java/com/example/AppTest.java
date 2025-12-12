package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void testGreet() {
        App app = new App();
        assertEquals("Hello World", app.greet());
    }
}

