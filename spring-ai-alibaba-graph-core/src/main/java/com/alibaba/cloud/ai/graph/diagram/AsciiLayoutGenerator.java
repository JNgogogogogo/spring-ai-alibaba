/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.graph.diagram;

import com.alibaba.cloud.ai.graph.GraphRenderer;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.internal.edge.Edge;
import com.alibaba.cloud.ai.graph.internal.edge.EdgeValue;
import com.alibaba.cloud.ai.graph.internal.node.Node;
import com.github.mdr.ascii.graph.Graph;
import com.github.mdr.ascii.layout.GraphLayout$;
import com.github.mdr.ascii.layout.coordAssign.VertexRenderingStrategy;
import com.github.mdr.ascii.layout.prefs.LayoutPrefsImpl;
import scala.Tuple2;
import scala.collection.JavaConverters$;
import scala.collection.immutable.List;
import scala.collection.immutable.Set;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Renders a graph as a layered (Sugiyama style) plain ASCII diagram, ready to be read from
 * console output without any renderer.
 *
 * <pre>
 *     +---------+
 *     |__START__|
 *     +---------+
 *          |
 *          v
 *      +-------+
 *      | grade |
 *      +-------+
 *       |  ^ |
 *       |  | -------
 *       |  ------  |
 *       v       |  |
 *   +------+    |  |
 *   |answer|    |  |
 *   +------+    |  |
 *      |        |  v
 *  +-------+ +-------+
 *  |__end__| |rewrite|
 *  +-------+ +-------+
 * </pre>
 *
 * Unlike the diagram-as-code generators, this renderer does not walk the graph through
 * hooks: it needs the complete node and edge set up front to compute layer assignment and
 * edge routing, so it implements {@link GraphRenderer} directly.
 * <p>
 * Notes:
 * <ul>
 * <li>Layout is delegated to the {@code com.github.mdr:ascii-graphs} library, which is a
 * Scala artifact, hence the {@code $MODULE$} accessors and the Scala collection
 * conversions below.</li>
 * <li>Conditional edges are always drawn, with their condition mappings expanded into
 * concrete destinations, because a laid out diagram cannot mark an edge as commented out.
 * The {@code printConditionalEdges} flag is therefore ignored.</li>
 * <li>Sub graph nodes are drawn as a single vertex; their inner nodes and edges are not
 * expanded into the parent diagram.</li>
 * <li>The output uses plain ASCII characters only. Switch {@code UNICODE_OUTPUT} to
 * {@code true} if box drawing characters are preferred.</li>
 * </ul>
 */
public class AsciiLayoutGenerator implements GraphRenderer {

	/** Whether to draw with Unicode box drawing characters instead of plain ASCII. */
	private static final boolean UNICODE_OUTPUT = false;

	@Override
	public String generate(StateGraph.Nodes nodes, StateGraph.Edges edges, String title, boolean printConditionalEdges) {
		java.util.Set<String> vertices = new LinkedHashSet<>();
		for (Node node : nodes.elements) {
			vertices.add(node.id());
		}

		java.util.List<Tuple2<String, String>> edgePairs = new ArrayList<>();
		for (Edge edge : edges.elements) {
			vertices.add(edge.sourceId());
			for (EdgeValue target : edge.targets()) {
				if (target.value() != null) {
					target.value().mappings().values().forEach(targetId -> {
						vertices.add(targetId);
						edgePairs.add(new Tuple2<>(edge.sourceId(), targetId));
					});
				}
				else if (target.id() != null) {
					vertices.add(target.id());
					edgePairs.add(new Tuple2<>(edge.sourceId(), target.id()));
				}
			}
		}

		String diagram = layout(vertices, edgePairs);
		return (title == null || title.isBlank()) ? diagram : "=== " + title + " ===\n" + diagram;
	}

	private String layout(java.util.Set<String> vertices, java.util.List<Tuple2<String, String>> edgePairs) {
		Set<String> scalaVertices = JavaConverters$.MODULE$.asScalaSetConverter(vertices).asScala().toSet();
		List<Tuple2<String, String>> scalaEdges = JavaConverters$.MODULE$.asScalaBufferConverter(edgePairs).asScala().toList();

		Graph<String> graph = new Graph<>(scalaVertices, scalaEdges);

		// (removeKinks, compactify, vertical, unicode, doubleVertices, rounded,
		// explicitAsciiBends)
		LayoutPrefsImpl prefs = new LayoutPrefsImpl(LayoutPrefsImpl.apply$default$1(), LayoutPrefsImpl.apply$default$2(),
				LayoutPrefsImpl.apply$default$3(), UNICODE_OUTPUT, LayoutPrefsImpl.apply$default$5(),
				LayoutPrefsImpl.apply$default$6(), LayoutPrefsImpl.apply$default$7());

		@SuppressWarnings({ "unchecked", "rawtypes" })
		VertexRenderingStrategy<String> strategy = (VertexRenderingStrategy) GraphLayout$.MODULE$.renderGraph$default$2();

		return GraphLayout$.MODULE$.renderGraph(graph, strategy, prefs);
	}

}
