package com.networknt.codegen.rest;

import com.networknt.codegen.rest.model.Stock;

import java.util.List;

/**
 * Benchmark for Rocker template engine by Fizzed.
 * 
 * https://github.com/fizzed/rocker
 * 
 * @author joelauer
 */
public class Rocker {

    static public void main(String[] args) {
        List<Stock> items = Stock.dummyItems();
        String output = templates.stocks.template(items)
                .render()
                .toString();
        System.out.println(output);
    }
}
