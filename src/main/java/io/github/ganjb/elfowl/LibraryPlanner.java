package io.github.ganjb.elfowl;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LibraryPlanner {

    LibraryPlan plan(List<Path> libraries, LibraryMetadataParser parser, NativeLoadOptions options) {
        Map<Path, LibraryMetadata> metadataByPath = new LinkedHashMap<Path, LibraryMetadata>();
        Map<String, List<Path>> candidatesByName = new HashMap<String, List<Path>>();

        for (Path library : libraries) {
            LibraryMetadata metadata = parser.parse(library);
            metadataByPath.put(library, metadata);
            index(candidatesByName, metadata.getBasename(), library);
            if (metadata.getSoname() != null && !metadata.getSoname().isEmpty()) {
                index(candidatesByName, metadata.getSoname(), library);
            }
        }

        for (List<Path> paths : candidatesByName.values()) {
            Collections.sort(paths, Comparator.comparing(Path::toString));
        }

        Map<Path, Set<Path>> dependencies = new LinkedHashMap<Path, Set<Path>>();
        Map<Path, List<String>> unresolved = new LinkedHashMap<Path, List<String>>();
        for (Path library : libraries) {
            dependencies.put(library, new LinkedHashSet<Path>());
        }

        for (Map.Entry<Path, LibraryMetadata> entry : metadataByPath.entrySet()) {
            Path library = entry.getKey();
            Path parent = library.getParent();
            for (String needed : entry.getValue().getNeededLibraries()) {
                Path resolved = resolveCandidate(needed, candidatesByName, parent);
                if (resolved == null || resolved.equals(library)) {
                    unresolved.computeIfAbsent(library, key -> new ArrayList<String>()).add(needed);
                    continue;
                }
                dependencies.get(library).add(resolved);
            }
        }

        List<List<Path>> components = stronglyConnectedComponents(libraries, dependencies);
        List<List<Path>> cycles = extractCycles(components, dependencies);
        if (options.isFailOnCycle() && !cycles.isEmpty()) {
            throw new NativeLibraryLoadException("Detected circular native library dependencies: " + cycles);
        }
        if (options.isStrictDependencyResolution() && !unresolved.isEmpty()) {
            throw new NativeLibraryLoadException("Unresolved native library dependencies: " + unresolved);
        }

        List<Path> loadOrder = buildLoadOrder(components, dependencies);
        return new LibraryPlan(loadOrder, cycles, unresolved);
    }

    private static void index(Map<String, List<Path>> candidatesByName, String name, Path path) {
        candidatesByName.computeIfAbsent(name, key -> new ArrayList<Path>()).add(path);
    }

    private static Path resolveCandidate(String name, Map<String, List<Path>> candidatesByName, Path preferredDirectory) {
        List<Path> candidates = candidatesByName.get(name);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        if (preferredDirectory != null) {
            for (Path candidate : candidates) {
                if (preferredDirectory.equals(candidate.getParent())) {
                    return candidate;
                }
            }
        }
        return candidates.get(0);
    }

    private static List<List<Path>> extractCycles(List<List<Path>> components, Map<Path, Set<Path>> dependencies) {
        List<List<Path>> cycles = new ArrayList<List<Path>>();
        for (List<Path> component : components) {
            if (component.size() > 1) {
                cycles.add(component);
                continue;
            }
            Path only = component.get(0);
            if (dependencies.getOrDefault(only, Collections.<Path>emptySet()).contains(only)) {
                cycles.add(component);
            }
        }
        return cycles;
    }

    private static List<Path> buildLoadOrder(List<List<Path>> components, Map<Path, Set<Path>> dependencies) {
        Map<Path, Integer> componentByNode = new HashMap<Path, Integer>();
        for (int i = 0; i < components.size(); i++) {
            for (Path path : components.get(i)) {
                componentByNode.put(path, i);
            }
        }

        Map<Integer, Set<Integer>> usersByDependencyComponent = new HashMap<Integer, Set<Integer>>();
        Map<Integer, Integer> indegree = new HashMap<Integer, Integer>();
        for (int i = 0; i < components.size(); i++) {
            usersByDependencyComponent.put(i, new LinkedHashSet<Integer>());
            indegree.put(i, 0);
        }

        for (Map.Entry<Path, Set<Path>> entry : dependencies.entrySet()) {
            int userComponent = componentByNode.get(entry.getKey());
            for (Path dependency : entry.getValue()) {
                int dependencyComponent = componentByNode.get(dependency);
                if (dependencyComponent == userComponent) {
                    continue;
                }
                if (usersByDependencyComponent.get(dependencyComponent).add(userComponent)) {
                    indegree.put(userComponent, indegree.get(userComponent) + 1);
                }
            }
        }

        List<Integer> zeroIndegree = new ArrayList<Integer>();
        for (Map.Entry<Integer, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                zeroIndegree.add(entry.getKey());
            }
        }
        Collections.sort(zeroIndegree);

        Deque<Integer> queue = new ArrayDeque<Integer>(zeroIndegree);
        List<Path> order = new ArrayList<Path>();
        while (!queue.isEmpty()) {
            Integer componentId = queue.removeFirst();
            List<Path> component = new ArrayList<Path>(components.get(componentId));
            Collections.sort(component, Comparator.comparing(Path::toString));
            order.addAll(component);

            List<Integer> users = new ArrayList<Integer>(usersByDependencyComponent.get(componentId));
            Collections.sort(users);
            for (Integer user : users) {
                int next = indegree.get(user) - 1;
                indegree.put(user, next);
                if (next == 0) {
                    queue.addLast(user);
                }
            }
        }
        return order;
    }

    private static List<List<Path>> stronglyConnectedComponents(
        Collection<Path> nodes,
        Map<Path, Set<Path>> dependencies
    ) {
        Map<Path, Integer> indexMap = new HashMap<Path, Integer>();
        Map<Path, Integer> lowLinkMap = new HashMap<Path, Integer>();
        Deque<Path> stack = new ArrayDeque<Path>();
        Set<Path> onStack = new HashSet<Path>();
        List<List<Path>> components = new ArrayList<List<Path>>();
        int[] index = new int[] {0};

        List<Path> sorted = new ArrayList<Path>(nodes);
        Collections.sort(sorted, Comparator.comparing(Path::toString));
        for (Path node : sorted) {
            if (!indexMap.containsKey(node)) {
                connect(node, dependencies, indexMap, lowLinkMap, stack, onStack, components, index);
            }
        }

        Collections.reverse(components);
        for (List<Path> component : components) {
            Collections.sort(component, Comparator.comparing(Path::toString));
        }
        return components;
    }

    private static void connect(
        Path node,
        Map<Path, Set<Path>> dependencies,
        Map<Path, Integer> indexMap,
        Map<Path, Integer> lowLinkMap,
        Deque<Path> stack,
        Set<Path> onStack,
        List<List<Path>> components,
        int[] index
    ) {
        indexMap.put(node, index[0]);
        lowLinkMap.put(node, index[0]);
        index[0]++;
        stack.push(node);
        onStack.add(node);

        List<Path> sortedDependencies = new ArrayList<Path>(dependencies.getOrDefault(node, Collections.<Path>emptySet()));
        Collections.sort(sortedDependencies, Comparator.comparing(Path::toString));
        for (Path dependency : sortedDependencies) {
            if (!indexMap.containsKey(dependency)) {
                connect(dependency, dependencies, indexMap, lowLinkMap, stack, onStack, components, index);
                lowLinkMap.put(node, Math.min(lowLinkMap.get(node), lowLinkMap.get(dependency)));
            } else if (onStack.contains(dependency)) {
                lowLinkMap.put(node, Math.min(lowLinkMap.get(node), indexMap.get(dependency)));
            }
        }

        if (lowLinkMap.get(node).equals(indexMap.get(node))) {
            List<Path> component = new ArrayList<Path>();
            Path current;
            do {
                current = stack.pop();
                onStack.remove(current);
                component.add(current);
            } while (!node.equals(current));
            components.add(component);
        }
    }
}
