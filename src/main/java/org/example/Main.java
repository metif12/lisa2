package org.example;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("IR PROJECT: LISA Corpus");

        MyAnalyzer analyzer = new MyAnalyzer();

        Engine engine = new Engine(analyzer);

        engine.start();

        var measurer = new Measurer(engine);

        measurer.run();
    }
}
