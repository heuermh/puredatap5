package com.noisepages.nettoyeur;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.jack.PdJackProcessor;

import processing.core.PApplet;

import com.noisepages.nettoyeur.jack.JackException;
import com.noisepages.nettoyeur.jack.JackNativeClient;


public class PureDataP5 implements PdReceiver {

	private final PApplet parent;
	private final PdJackProcessor processor;
	private final Method printCallback;
	private final Method receiveBangCallback;
	private final Method receiveFloatCallback;
	private final Method receiveSymbolCallback;
	private final Method receiveListCallback;
	private final Method receiveMessageCallback;
	private JackNativeClient client = null;

	public PureDataP5(PApplet parent, int nIn, int nOut) {
		this.parent = parent;
		parent.registerDispose(this);
		Map<String, Method> methods = extractMethods(parent.getClass());
		printCallback = methods.get("pdPrint");
		receiveBangCallback = methods.get("receiveBang");
		receiveFloatCallback = methods.get("receiveFloat");
		receiveSymbolCallback = methods.get("receiveSymbol");
		receiveListCallback = methods.get("receiveList");
		receiveMessageCallback = methods.get("receiveMessage");
		PdJackProcessor proc = null;
		try {
			proc = new PdJackProcessor(nIn, nOut);
		} catch (JackException e) {
			System.err.println("WARNING: unable to create pd processor");
			e.printStackTrace();
		}
		processor = proc;
	}
	
	public void dispose() {
		stop();
	}

	public void start() throws Exception {
		start(null, null);
	}

	public void start(String source, String sink) {
		stop();
		try {
			client = processor.createClient("processing");
			if (source != null) {
				client.connectInputPorts(source);
			}
			if (sink != null) {
				client.connectOutputPorts(sink);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	private Map<String, Method> extractMethods(Class<? extends PApplet> clazz) {
		Map<String, Method> result = new HashMap<String, Method>();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method: methods) {
			result.put(method.getName(), method);
		}
		PdBase.setReceiver(this);
		return result;
	}
	
	public int subscribe(String sym) {
		return PdBase.subscribe(sym);
	}
	
	public void unsubscribe(String sym) {
		PdBase.unsubscribe(sym);
	}

	@Override
	public void print(String s) {
		if (printCallback != null) {
			try {
				printCallback.invoke(parent, s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveBang(String source) {
		if (receiveBangCallback != null) {
			try {
				receiveBangCallback.invoke(parent, source);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveFloat(String source, float x) {
		if (receiveFloatCallback != null) {
			try {
				receiveFloatCallback.invoke(parent, source, x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveSymbol(String source, String symbol) {
		if (receiveSymbolCallback != null) {
			try {
				receiveSymbolCallback.invoke(parent, source, symbol);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveList(String source, Object... args) {
		if (receiveListCallback != null) {
			try {
				receiveListCallback.invoke(parent, source, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receiveMessage(String source, String symbol, Object... args) {
		if (receiveMessageCallback != null) {
			try {
				receiveMessageCallback.invoke(parent, source, symbol, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int openPatch(String patch) {
		File file = new File(parent.dataPath(patch));
		try {
			return PdBase.openPatch(file);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void closePatch(int handle) {
		PdBase.closePatch(handle);
	}

	public void sendBang(String recv) {
		PdBase.sendBang(recv);
	}

	public void sendFloat(String recv, float x) {
		PdBase.sendFloat(recv, x);
	}

	public void sendSymbol(String recv, String sym) {
		PdBase.sendSymbol(recv, sym);
	}

	public void sendList(String recv, Object... args) {
		PdBase.sendList(recv, args);
	}

	public void sendMessage(String recv, String mesg, Object... args) {
		PdBase.sendMessage(recv, mesg, args);
	}

	public static void main(String[] args) {
		// Do nothing.
	}
}

