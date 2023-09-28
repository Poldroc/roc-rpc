package com.poldroc.rpc.framework.core.spi.jdk;
/**
 *
 * @author Poldroc
 * @date 2023/9/28
 */

public class DefaultSpiTest implements SpiTest{

    @Override
    public void doTest() {
        System.out.println("DefaultSpiTest doTest");
    }
}
