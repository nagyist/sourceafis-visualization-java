// Part of SourceAFIS Visualization: https://sourceafis.machinezoo.com/transparency/
package com.machinezoo.sourceafis.visualization.keys;

import java.util.*;
import com.machinezoo.sourceafis.transparency.*;
import com.machinezoo.sourceafis.transparency.keys.*;
import com.machinezoo.sourceafis.visualization.common.*;
import com.machinezoo.sourceafis.visualization.markers.*;
import com.machinezoo.sourceafis.visualization.utils.*;

public record ContrastImage() implements VectorVisualizer {
	@Override
	public ContrastKey key() {
		return new ContrastKey();
	}
	@Override
	public Set<TransparencyKey<?>> dependencies(TransparentOperation operation) {
		return Set.of(key(), new BlocksKey(), new InputImageKey(), new InputGrayscaleKey());
	}
	@Override
	public VectorVisualization render(TransparencyArchive archive) {
		var blocks = archive.deserialize(new BlocksKey()).orElseThrow();
		var matrix = archive.deserialize(key()).orElseThrow();
		var buffer = new VectorBuffer(blocks.pixels())
			.embed(archive);
		for (var at : IntPoints.stream(blocks.primary().blocks()))
			buffer.add(new ContrastMarker(blocks.primary().block(at), matrix.get(at)));
		return buffer.render();
	}
}
