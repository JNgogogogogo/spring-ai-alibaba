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

package com.alibaba.cloud.ai.graph;

/**
 * Renders a graph definition into a textual representation.
 * <p>
 * Two families of implementations exist:
 * <ul>
 * <li>{@link DiagramGenerator} subclasses, which emit diagram-as-code sources (PlantUML,
 * Mermaid) by walking the graph through a set of hooks.</li>
 * <li>{@code AsciiLayoutGenerator}, which runs an actual layered layout algorithm and emits
 * a ready to read diagram.</li>
 * </ul>
 * A renderer receives the raw node and edge containers rather than a {@link StateGraph},
 * because {@link CompiledGraph} also renders its own processed topology.
 */
@FunctionalInterface
public interface GraphRenderer {

	/**
	 * Generates a textual representation of the given graph.
	 * @param nodes the nodes of the graph
	 * @param edges the edges of the graph
	 * @param title the title of the graph, may be null
	 * @param printConditionalEdges whether conditional edges should be part of the output
	 * @return the rendered representation, never null
	 */
	String generate(StateGraph.Nodes nodes, StateGraph.Edges edges, String title, boolean printConditionalEdges);

}
