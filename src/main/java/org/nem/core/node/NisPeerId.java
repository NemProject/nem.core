package org.nem.core.node;

/**
 * NIS REST P2P paths.
 */
@SuppressWarnings("unused")
public enum NisPeerId implements ApiId {

	//region block/*

	/**
	 * The block/at API.
	 */
	REST_BLOCK_AT("/block/at/public"),

	//endregion

	//region chain/*

	/**
	 * The chain/blocks-after API.
	 */
	REST_CHAIN_BLOCKS_AFTER("/chain/blocks-after"),

	/**
	 * The chain/hashes-from API
	 */
	REST_CHAIN_HASHES_FROM("/chain/hashes-from"),

	/**
	 * The chain/last-block API.
	 */
	REST_CHAIN_LAST_BLOCK("/chain/last-block"),

	/**
	 * The chain/score API.
	 */
	REST_CHAIN_SCORE("/chain/score"),

	/**
	 * The chain/height API.
	 */
	REST_CHAIN_HEIGHT("/chain/height"),

	//endregion

	//region node/*

	/**
	 * The node/cysm API.
	 */
	REST_NODE_CAN_YOU_SEE_ME("/node/cysm"),

	/**
	 * The node/extended-info API.
	 */
	REST_NODE_EXTENDED_INFO("/node/extended-info"),

	/**
	 * The node/info API.
	 */
	REST_NODE_INFO("/node/info"),

	/**
	 * The node/peer-list/all API.
	 */
	REST_NODE_PEER_LIST("/node/peer-list/all"),

	/**
	 * The node/peer-list/reachable API.
	 */
	REST_NODE_PEER_LIST_REACHABLE("/node/peer-list/reachable"),

	/**
	 * The node/peer-list/active API.
	 */
	REST_NODE_PEER_LIST_ACTIVE("/node/peer-list/active"),

	/**
	 * The node/ping API.
	 */
	REST_NODE_PING("/node/ping"),

	//endregion

	//region push/*

	/**
	 * The push/block API.
	 */
	REST_PUSH_BLOCK("/push/block"),

	/**
	 * The push/transaction API.
	 */
	REST_PUSH_TRANSACTION("/push/transaction"),

	/**
	 * The push/transactions API.
	 */
	REST_PUSH_TRANSACTIONS("/push/transactions"),

	//endregion

	//region transactions/*

	/**
	 * The transactions/unconfirmed API.
	 */
	REST_TRANSACTIONS_UNCONFIRMED("/transactions/unconfirmed"),

	//endregion

	//region time sync/*

	/**
	 * The time-sync/network-time API
	 */
	REST_TIME_SYNC_NETWORK_TIME("time-sync/network-time");

	//endregion

	private final String value;

	/**
	 * Creates a NIS API id.
	 *
	 * @param value The string representation.
	 */
	NisPeerId(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}
}
