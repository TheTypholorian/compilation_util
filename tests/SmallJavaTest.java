package net.typho.compilation_util.test;

import java.lang.System;

public class SmallJavaTest extends BaseClass implements I1, I2, I3 {
    public static final int CONSTANT = 0;

    public static void main(float arg) {
        System.out.println(arg + CONSTANT);
    }
}