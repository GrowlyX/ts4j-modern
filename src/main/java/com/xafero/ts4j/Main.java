package com.xafero.ts4j;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * @author GrowlyX
 * @since 10/4/2023
 */
public class Main {
    public static void main(String[] args) throws ScriptException {
        final ScriptEngine engine = new ScriptEngineManager().getEngineByName("TypeScript");
        engine.eval("print(\"hey\")");
    }
}
