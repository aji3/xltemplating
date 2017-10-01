package org.xlbean.xltemplating.engine.pebble;

import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;

public class PebbleEngineFactory extends TemplatingEngineFactory {

	@Override
	public TemplatingEngine createEngine() {
		return new PebbleEngine();
	}

}
