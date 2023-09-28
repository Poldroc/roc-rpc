package com.poldroc.rpc.framework.core.spi.jdk;

import java.util.Iterator;
import java.util.ServiceLoader;

public class TestSpiDemo {

    public static void doTest(SpiTest SpiTest){
        System.out.println("begin");
        SpiTest.doTest();
        System.out.println("end");
    }

    public static void main(String[] args) {
        System.out.println("TestSpiDemo");
        ServiceLoader<SpiTest> serviceLoader = ServiceLoader.load(SpiTest.class);
        Iterator<SpiTest> spiTestIterator = serviceLoader.iterator();
        while (spiTestIterator.hasNext()){
            SpiTest spiTest = spiTestIterator.next();
            TestSpiDemo.doTest(spiTest);
        }
    }
}