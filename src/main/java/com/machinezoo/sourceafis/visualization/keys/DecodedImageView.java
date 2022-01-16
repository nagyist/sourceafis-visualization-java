// Part of SourceAFIS Visualization: https://sourceafis.machinezoo.com/transparency/
package com.machinezoo.sourceafis.visualization.keys;

import com.machinezoo.sourceafis.transparency.*;
import com.machinezoo.sourceafis.transparency.keys.*;
import com.machinezoo.sourceafis.visualization.common.*;
import com.machinezoo.sourceafis.visualization.images.*;

public record DecodedImageView() implements GrayscaleVisualizer {
	@Override
	public TransparencyKey<?> key() {
		return new DecodedImageKey();
	}
	@Override
	public GrayscaleVisualization render(TransparencyArchive archive) {
		return new DoubleMatrixImage(archive.deserialize(new DecodedImageKey()).orElseThrow(), 0, 1).render();
	}
}
