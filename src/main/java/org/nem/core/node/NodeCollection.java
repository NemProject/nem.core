package org.nem.core.node;

import org.nem.core.serialization.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a collection of nodes.
 */
public class NodeCollection implements SerializableEntity {
	private static final Collection<NodeStatus> NODE_STATUSES = Arrays.asList(
			NodeStatus.ACTIVE,
			NodeStatus.BUSY,
			NodeStatus.INACTIVE,
			NodeStatus.FAILURE);
	private static final Collection<NodeStatus> PRUNE_NODE_STATUSES = Arrays.asList(
			NodeStatus.BUSY, // if a node is busy for an entire prune cycle, there is probably something wrong with it
			NodeStatus.INACTIVE,
			NodeStatus.FAILURE);
	private static final Collection<NodeStatus> BLACKLIST_NODE_STATUSES = Arrays.asList(
			NodeStatus.INACTIVE,
			NodeStatus.FAILURE);

	private final Map<NodeStatus, Set<Node>> statusNodesMap = new HashMap<NodeStatus, Set<Node>>() {
		{
			for (final NodeStatus value : NODE_STATUSES) {
				this.put(value, createSet());
			}
		}
	};

	private final Set<Node> pruneCandidates = createSet();

	/**
	 * Creates a node collection.
	 */
	public NodeCollection() {
	}

	/**
	 * Deserializes a node collection.
	 *
	 * @param deserializer The deserializer.
	 */
	public NodeCollection(final Deserializer deserializer) {
		for (final NodeStatus value : NODE_STATUSES) {
			final String key = value.toString().toLowerCase();
			this.statusNodesMap.get(value).addAll(deserializer.readObjectArray(key, Node::new));
		}
	}

	private static Set<Node> createSet() {
		return Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	/**
	 * Gets all active nodes.
	 *
	 * @return A collection of active nodes.
	 */
	public Collection<Node> getActiveNodes() {
		return this.statusNodesMap.get(NodeStatus.ACTIVE);
	}

	/**
	 * Gets all busy nodes.
	 *
	 * @return A collection of busy nodes.
	 */
	public Collection<Node> getBusyNodes() {
		return this.statusNodesMap.get(NodeStatus.BUSY);
	}

	/**
	 * Gets a collection of all non-blacklisted nodes.
	 *
	 * @return A collection of all non-blacklisted nodes.
	 */
	public Collection<Node> getAllNodes() {
		final List<Node> allNodes = new ArrayList<>();
		allNodes.addAll(this.getActiveNodes());
		allNodes.addAll(this.getBusyNodes());
		return allNodes;
	}

	/**
	 * Gets a collection of nodes that have the specified status.
	 *
	 * @param status The status.
	 * @return All nodes with the specified status.
	 */
	public Collection<Node> getNodes(final NodeStatus status) {
		final Collection<Node> nodes = this.statusNodesMap.getOrDefault(status, null);
		if (null == nodes) {
			return new ArrayList<>();
		}

		return nodes;
	}

	/**
	 * Gets the total number of nodes in the collection.
	 *
	 * @return The total number of nodes
	 */
	public int size() {
		return this.statusNodesMap.entrySet().stream()
				.map(entry -> entry.getValue().size())
				.reduce(0, Integer::sum);
	}

	/**
	 * Finds a node with a given endpoint.
	 *
	 * @param endpoint The endpoint.
	 * @return The matching node or null if not found.
	 */
	public Node findNodeByEndpoint(final NodeEndpoint endpoint) {
		for (final Node node : this.getAllNodes()) {
			if (node.getEndpoint().equals(endpoint)) {
				return node;
			}
		}

		return null;
	}

	/**
	 * Finds a node with a given identity.
	 *
	 * @param identity The identity.
	 * @return The matching node or null if not found.
	 */
	public Node findNodeByIdentity(final NodeIdentity identity) {
		for (final Node node : this.getAllNodes()) {
			if (node.getIdentity().equals(identity)) {
				return node;
			}
		}

		return null;
	}

	/**
	 * Gets a value indicating whether or not the node is currently blacklisted.
	 *
	 * @param node The node.
	 * @return True if the node is blacklisted.
	 */
	public boolean isNodeBlacklisted(final Node node) {
		return this.isNodeStatusMatching(node, BLACKLIST_NODE_STATUSES);
	}

	private boolean isPruneCandidate(final Node node) {
		return this.isNodeStatusMatching(node, PRUNE_NODE_STATUSES);
	}

	private boolean isNodeStatusMatching(final Node node, final Collection<NodeStatus> statuses) {
		for (final NodeStatus status : statuses) {
			if (this.statusNodesMap.get(status).contains(node)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the status of the specified node.
	 *
	 * @param node The node.
	 * @return The node's status.
	 */
	public NodeStatus getNodeStatus(final Node node) {
		for (final Map.Entry<NodeStatus, Set<Node>> entry : this.statusNodesMap.entrySet()) {
			if (entry.getValue().contains(node)) {
				return entry.getKey();
			}
		}

		return NodeStatus.UNKNOWN;
	}

	/**
	 * Updates this collection to include the specified node with the associated status.
	 * The new node information will replace any previous node information.
	 *
	 * @param node The node.
	 * @param status The node status.
	 */
	public void update(final Node node, final NodeStatus status) {
    		if (node != null) {
     		 for (final Map.Entry<NodeStatus, Set<Node>> entry : this.statusNodesMap.entrySet()) {
        		entry.getValue().remove(node);
      		 }

      			final Set<Node> nodes = this.statusNodesMap.getOrDefault(status, null);
      				if (null != nodes) {
        		this.statusNodesMap.get(status).add(node);
      				 }

      		if (!this.isPruneCandidate(node)) {
        		this.pruneCandidates.remove(node);
      		}
    								}
  	}

	/**
	 * Takes a snapshot of all inactive nodes and drops the inactive nodes
	 * that have stayed inactive since the last time this function was called.
	 */
	public void prune() {
		this.pruneCandidates.stream()
				.filter(this::isPruneCandidate)
				.forEach(node -> this.update(node, NodeStatus.UNKNOWN));

		this.pruneCandidates.clear();
		for (final NodeStatus status : PRUNE_NODE_STATUSES) {
			this.pruneCandidates.addAll(this.statusNodesMap.get(status));
		}
	}

	@Override
	public void serialize(final Serializer serializer) {
		for (final NodeStatus value : NODE_STATUSES) {
			final String key = value.toString().toLowerCase();
			serializer.writeObjectArray(key, this.statusNodesMap.get(value));
		}
	}

	@Override
	public int hashCode() {
		return this.statusNodesMap.entrySet().stream()
				.map(e -> Objects.hash(e.getKey()) * Objects.hash(e.getValue()))
				.reduce(0, Integer::sum);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof NodeCollection)) {
			return false;
		}

		final NodeCollection rhs = (NodeCollection)obj;
		return this.statusNodesMap.equals(rhs.statusNodesMap);
	}
}
