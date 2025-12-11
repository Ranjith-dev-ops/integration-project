package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class AppTest {

    @Test
    public void testAdd() {
        App a = new App();
        assertEquals(5, a.add(2, 3));
    }
}

