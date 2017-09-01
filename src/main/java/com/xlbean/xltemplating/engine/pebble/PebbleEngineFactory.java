package com.xlbean.xltemplating.engine.pebble;

import com.xlbean.xltemplating.engine.TemplatingEngine;
import com.xlbean.xltemplating.engine.TemplatingEngineFactory;

public class PebbleEngineFactory extends TemplatingEngineFactory {

	@Override
	public TemplatingEngine createEngine() {
		return new PebbleEngine();
	}

}
