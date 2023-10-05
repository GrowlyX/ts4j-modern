package com.xafero.ts4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xafero.ts4j.core.MemoryFS;
import com.xafero.ts4j.proxy.Container;
import com.xafero.ts4j.proxy.Node;
import org.apache.commons.io.IOUtils;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class TypeScriptCompiler {

	public static final Gson gson = new GsonBuilder()
		.setPrettyPrinting().create();

	public static final String tsconfig = "tsconfig.json";
	public static final String compiledjs = "compiled.js";
	public static final String rawTs = "raw.ts";

	public static final String libdts = "lib.d.ts";
	public static final String libcoredts = "lib.core.d.ts";

	public static final String TS_ASSETS_FOLDER = "com.xafero.ts4j/";
	private static final String TYPE_SCRIPT = "tsc.js";

	private final Invocable jsEngine;
	private final CompiledScript tscScript;

	public <T extends ScriptEngine & Compilable & Invocable> TypeScriptCompiler(T js) {
		try {
			jsEngine = js;
			Reader reader = readAsset(TYPE_SCRIPT);
			tscScript = js.compile(reader);
			js.put("exporter", this);
			tscScript.eval();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setExport(Object obj) throws ScriptException {
		ScriptEngine engine = ((ScriptEngine) jsEngine);
		engine.put("ts", obj);
	}

	public String compile(Reader reader) throws ScriptException, IOException {
		return compile(IOUtils.toString(reader));
	}

	@Nullable
	public String compile(String script) throws ScriptException, IOException {
		ScriptEngine engine = ((ScriptEngine) jsEngine);
		engine.put("parent", new Node());
		engine.put("container", new Container());
		engine.put("blockScopeContainer", new Container());
		engine.put("lastContainer", new Container());
		engine.put("currentReachabilityState", 1);
		engine.put("labelStack", 1);
		engine.put("labelIndexMap", 1);
		engine.put("implicitLabels", 1);
		engine.put("hasExplicitReturn", 1);
		engine.put("symbolCount", 1);
		// Apply system
		MemoryFS sys = new MemoryFS(this);
		engine.put("mysys", sys);
		engine.eval("ts.sys = mysys");
		// Inject files
		sys.push(libdts, readAsset(libdts));
		sys.push(libcoredts, readAsset(libcoredts));
		// Generate tsconfig and inject script
		sys.push(tsconfig, generateTsCfg());
		sys.push(rawTs, script);
		// Execute CMD
		engine.eval("ts.executeCommandLine([])");
		// Get compiled code
        return sys.getMemory().get(compiledjs);
	}

	public String generateTsCfg() {
		JsonObject json = new JsonObject();
		JsonObject opts = new JsonObject();
		opts.addProperty("outFile", compiledjs);
		opts.addProperty("sourceMap", true);
		opts.addProperty("listFiles", true);
		opts.addProperty("removeComments", true);
		opts.addProperty("module", "commonjs");
		opts.addProperty("target", "es3");
		opts.addProperty("noImplicitAny", false);
		opts.addProperty("noLib", true);
		json.add("compilerOptions", opts);
		JsonArray array = new JsonArray();
		array.add(rawTs);
		json.add("files", array);
        return gson.toJson(json);
	}

	private static final Path ASSETS_DATA_DIR =
		new File(AppUtil.ROBOT_DATA_DIR, "tsassets").toPath();

	@NotNull
	@Contract("_ -> new")
	private static Reader readAsset(@NotNull String path) throws IOException {
		return new InputStreamReader(
			new FileInputStream(
				new File(ASSETS_DATA_DIR.toFile(), path)
			),
			StandardCharsets.UTF_8
		);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	@Contract("_ -> new")
	public static <S extends ScriptEngine & Compilable & Invocable> TypeScriptCompiler create(@NotNull ScriptEngine engine) {
		return new TypeScriptCompiler((S) engine);
	}
}
