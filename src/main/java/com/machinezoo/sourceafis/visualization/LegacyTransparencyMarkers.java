// Part of SourceAFIS Visualization: https://sourceafis.machinezoo.com/transparency/
package com.machinezoo.sourceafis.visualization;

import static java.util.stream.Collectors.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import com.machinezoo.pushmode.dom.*;
import com.machinezoo.sourceafis.transparency.types.*;
import com.machinezoo.sourceafis.visualization.types.*;
import one.util.streamex.*;

public class LegacyTransparencyMarkers {
	/*
	 * Method naming:
	 * - mark*() - discrete SVG markers with lots of transparency around them
	 * - paint*() - opaque image (usually pixmap), useful only as a base layer
	 * - overlay*() - semi-transparent image (usually pixmap)
	 * - embed*() - pixmap as an SVG image element
	 * - *Diff() - visual diff relative to previous (or other) stage
	 * - *ModelName*() - renders ModelName (e.g. BooleanMatrix)
	 * - *KeyName*() - appropriately renders data from particular transparency key (e.g. EqualizedImage, may be abbreviated)
	 * 
	 * Methods are sorted in the same order in which corresponding data is produced by the algorithm.
	 * All generic and helper methods are defined just before the first method that uses them.
	 * The first parameter is the object to be visualized.
	 * Following parameters contain additional context needed for visualization
	 * in the same order in which they are generated by the algorithm.
	 */
	public static DomContent embedPng(LegacyTransparencyPixmap pixmap) {
		return Svg.image()
			.width(pixmap.width)
			.height(pixmap.height)
			.href("data:image/png;base64," + Base64.getEncoder().encodeToString(pixmap.png()));
	}
	public static DomContent embedJpeg(LegacyTransparencyPixmap pixmap) {
		return Svg.image()
			.width(pixmap.width)
			.height(pixmap.height)
			.href("data:image/jpeg;base64," + Base64.getEncoder().encodeToString(pixmap.jpeg()));
	}
	public static DomContent markMinutiaPosition(MinutiaPoint minutia) {
		DoublePoint at = MinutiaPoints.center(minutia);
		return Svg.circle()
			.cx(at.x())
			.cy(at.y())
			.r(2.5)
			.fill("red");
	}
	private static String colorEdgeShape(double length, double angle) {
		double stretch = Math.min(1, Math.log1p(length) / Math.log1p(300));
		int color = Color.HSBtoRGB((float)(angle / DoubleAnglesEx.PI2), 1.0f, (float)(1 - 0.5 * stretch));
		return String.format("#%06x", color & 0xffffff);
	}
	private static DomContent markEdgeShape(EdgeShape shape, MinutiaPoint reference, MinutiaPoint neighbor, double width) {
		DoublePoint referencePos = MinutiaPoints.center(reference);
		DoublePoint neighborPos = MinutiaPoints.center(neighbor);
		DoublePoint middle = DoublePoints.sum(DoublePoints.multiply(0.5, DoublePoints.difference(neighborPos, referencePos)), referencePos);
		return new DomFragment()
			.add(Svg.line()
				.x1(referencePos.x())
				.y1(referencePos.y())
				.x2(middle.x())
				.y2(middle.y())
				.stroke(colorEdgeShape(shape.length(), shape.referenceAngle()))
				.strokeWidth(width))
			.add(Svg.line()
				.x1(neighborPos.x())
				.y1(neighborPos.y())
				.x2(middle.x())
				.y2(middle.y())
				.stroke(colorEdgeShape(shape.length(), shape.neighborAngle()))
				.strokeWidth(width));
	}
	private static DomElement markPairingEdge(EdgePair edge, MatchSide side, Template template) {
		DoublePoint reference = MinutiaPoints.center(template.minutiae()[edge.from().side(side)]);
		DoublePoint neighbor = MinutiaPoints.center(template.minutiae()[edge.to().side(side)]);
		return Svg.line()
			.x1(reference.x())
			.y1(reference.y())
			.x2(neighbor.x())
			.y2(neighbor.y());
	}
	public static DomContent markPairingTreeEdge(EdgePair edge, MatchSide side, Template template) {
		return markPairingEdge(edge, side, template)
			.strokeWidth(2)
			.stroke("green");
	}
	public static DomContent markPairingSupportEdge(EdgePair edge, MatchSide side, Template template) {
		return markPairingEdge(edge, side, template)
			.stroke("yellow");
	}
	public static DomContent markIndexedEdge(IndexedEdge edge, Template template) {
		return markEdgeShape(edge, template.minutiae()[edge.reference()], template.minutiae()[edge.neighbor()], 0.6);
	}
	public static DomContent markHash(EdgeHashEntry[] hash, Template template) {
		DomFragment markers = new DomFragment();
		List<IndexedEdge> edges = StreamEx.of(hash)
			.flatArray(e -> e.edges())
			.sorted(Comparator.comparing(e -> -e.length()))
			.collect(toList());
		for (IndexedEdge edge : edges)
			if (edge.reference() < edge.neighbor())
				markers.add(markIndexedEdge(edge, template));
		for (var minutia : template.minutiae())
			markers.add(markMinutiaPosition(minutia));
		return markers;
	}
	public static DomContent markMinutiaPositions(Template template) {
		DomFragment markers = new DomFragment();
		for (var minutia : template.minutiae())
			markers.add(markMinutiaPosition(minutia));
		return markers;
	}
	public static DomContent markRoots(MinutiaPair[] roots, Template probe, Template candidate) {
		LegacyTransparencySplit split = new LegacyTransparencySplit(probe.size(), candidate.size());
		for (MinutiaPair pair : roots) {
			DoublePoint probePos = MinutiaPoints.center(probe.minutiae()[pair.probe()]);
			DoublePoint candidatePos = MinutiaPoints.center(candidate.minutiae()[pair.candidate()]);
			split.add(Svg.line()
				.x1(split.leftX(probePos.x()))
				.y1(split.leftY(probePos.y()))
				.x2(split.rightX(candidatePos.x()))
				.y2(split.rightY(candidatePos.y()))
				.stroke("green")
				.strokeWidth(0.4));
		}
		return split.content();
	}
	public static DomContent markRoot(MinutiaPoint minutia) {
		DoublePoint at = MinutiaPoints.center(minutia);
		return Svg.circle()
			.cx(at.x())
			.cy(at.y())
			.r(3.5)
			.fill("blue");
	}
	public static DomContent markPairing(PairingGraph pairing, MatchSide side, Template template) {
		DomFragment markers = new DomFragment();
		for (var edge : pairing.support())
			markers.add(markPairingSupportEdge(edge, side, template));
		for (var edge : pairing.tree())
			markers.add(markPairingTreeEdge(edge, side, template));
		for (var minutia : template.minutiae())
			markers.add(markMinutiaPosition(minutia));
		var root = template.minutiae()[pairing.root().side(side)];
		markers.add(markRoot(root));
		return markers;
	}
}
